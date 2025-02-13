/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_DEFINITION_LIST_PAGING_ERROR;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.seatunnelweb.SeaTunnelWebClient;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Seatunnel Controller
 */
@Tag(name = "SEATUNNEL_WEB_TAG")
@RestController
@RequestMapping("/seatunnleweb")
@Slf4j
public class SeatunnelController extends BaseController {

    private SeaTunnelWebClient seaTunnelWebClient;

    private static final Logger logger = LoggerFactory.getLogger(SeatunnelController.class);

    /**
     * 获取 SeaTunnel 作业配置
     */
    @Parameters({
            @Parameter(name = "userID", description = "userID", required = true, schema = @Schema(implementation = Integer.class)),
            @Parameter(name = "jobDefineId", description = "jobDefineId", required = true, schema = @Schema(implementation = Long.class))
    })
    @GetMapping(value = "/jobConfig")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_TASK_DEFINITION_ERROR)
    public Result getSeaTunnelJobConfig(@RequestParam("userID") Integer userID,
                                        @RequestParam("jobDefineId") Long jobDefineId) {
        try {
            if (seaTunnelWebClient == null) {
                seaTunnelWebClient = new SeaTunnelWebClient();
            }

            String jobConfig = seaTunnelWebClient.getJobConfig(userID, jobDefineId);
            Map<String, Object> result = new HashMap<>();
            result.put("data", jobConfig);
            return returnDataList(result);
        } catch (IOException e) {
            logger.error("获取 SeaTunnel 作业配置失败", e);
            return error(Status.SEATUNNEL_REQUEST_FAILED.getCode(), "获取作业配置失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("获取 SeaTunnel 作业配置时发生未知错误", e);
            return error(Status.SEATUNNEL_UNKNOWN_ERROR.getCode(), "系统错误: " + e.getMessage());
        }
    }

    /**
     * 获取 SeaTunnel 作业定义列表
     */
    @Parameters({
            @Parameter(name = "searchName", description = "searchName", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/jobDefinitions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_DEFINITION_LIST_PAGING_ERROR)
    public Result getSeaTunnelJobDefinitionList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @RequestParam(value = "searchName", required = false) String searchName,
                                                @RequestParam("pageNo") Integer pageNo,
                                                @RequestParam("pageSize") Integer pageSize,
                                                @RequestParam(value = "jobMode", required = false) String jobMode) {
        try {
            checkPageParams(pageNo, pageSize);
            if (seaTunnelWebClient == null) {
                seaTunnelWebClient = new SeaTunnelWebClient();
            }

            String jobDefinitions = seaTunnelWebClient.getJobDefinitionList(searchName, pageNo, pageSize, jobMode);
            Map<String, Object> result = new HashMap<>();
            result.put("data", jobDefinitions);
            return returnDataList(result);
        } catch (IllegalArgumentException e) {
            logger.error("分页参数验证失败", e);
            return error(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), "分页参数错误: " + e.getMessage());
        } catch (IOException e) {
            logger.error("获取 SeaTunnel 作业定义列表失败", e);
            return error(Status.SEATUNNEL_REQUEST_FAILED.getCode(), "获取作业定义列表失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("获取 SeaTunnel 作业定义列表时发生未知错误", e);
            return error(Status.SEATUNNEL_UNKNOWN_ERROR.getCode(), "系统错误: " + e.getMessage());
        }
    }
}
