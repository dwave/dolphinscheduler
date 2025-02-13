package org.apache.dolphinscheduler.api.seatunnelweb;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class SeaTunnelWebClient {

    private final String baseUrl;
    private final int timeoutMillis;
    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    public SeaTunnelWebClient() {
        try (InputStream input = SeaTunnelWebClient.class.getClassLoader().getResourceAsStream("application.yaml")) {
            Yaml yaml = new Yaml();
            // 处理多文档 YAML
            Iterable<Object> documents = yaml.loadAll(input);
            Map<String, Object> seatunnelConfig = null;

            // 遍历所有文档，找到包含 seatunnel 配置的文档
            for (Object document : documents) {
                Map<String, Object> data = (Map<String, Object>) document;
                if (data.containsKey("seatunnel")) {
                    seatunnelConfig = (Map<String, Object>) data.get("seatunnel");
                    break;
                }
            }

            if (seatunnelConfig == null) {
                throw new RuntimeException("未找到 seatunnel 配置");
            }

            Map<String, Object> webConfig = (Map<String, Object>) seatunnelConfig.get("web");
            this.baseUrl = (String) webConfig.getOrDefault("base-url", "http://10.9.99.33:8801/");
            this.timeoutMillis = (Integer) webConfig.getOrDefault("timeout-millis", 30000);
        } catch (IOException e) {
            throw new RuntimeException("无法读取配置文件", e);
        }

        RequestConfig requestConfig =
                RequestConfig.custom()
                        .setConnectTimeout(timeoutMillis)
                        .setSocketTimeout(timeoutMillis)
                        .build();

        this.client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 同步获取作业配置
     *
     * @param userId 用户ID
     * @param jobDefineId 作业定义ID
     * @return 作业配置JSON字符串
     * @throws IOException 如果请求失败
     */
    public String getJobConfig(Integer userId, Long jobDefineId) throws IOException {
        String url = baseUrl + "/seatunnel/api/v1/job/executor/jobConfig?jobDefineId=" + jobDefineId;
        HttpGet request = new HttpGet(url);
        request.setHeader("userId", String.valueOf(userId));

        HttpResponse response = client.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        log.info("--------------------getJobConfig-------------------------");
        log.info(responseBody);
        log.info("--------------------getJobConfig-------------------------");

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("请求失败: HTTP " + statusCode);
        }

        // 解析响应获取 data 字段
        try {
            return objectMapper.readTree(responseBody).get("data").asText();
        } catch (JsonProcessingException e) {
            throw new IOException("解析响应数据失败", e);
        }
    }

    /**
     * 获取作业定义列表
     *
     * @param searchName 作业名称（可选）
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param jobMode 作业模式（可选）
     * @return 作业定义列表
     * @throws IOException 如果请求失败
     */
    public String getJobDefinitionList(String searchName, Integer pageNo, Integer pageSize,
                                       String jobMode) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("/seatunnel/api/v1/job/definition")
                .append("?pageNo=").append(pageNo)
                .append("&pageSize=").append(pageSize);

        if (searchName != null && !searchName.isEmpty()) {
            // 对中文参数进行 URL 编码
            urlBuilder.append("&searchName=").append(java.net.URLEncoder.encode(searchName, "UTF-8"));
        }
        if (jobMode != null && !jobMode.isEmpty()) {
            urlBuilder.append("&jobMode=").append(jobMode);
        }

        HttpGet request = new HttpGet(urlBuilder.toString());
        // 设置请求的字符编码
        request.setHeader("Accept-Charset", "UTF-8");
        request.setHeader("Content-Type", "application/json;charset=UTF-8");

        HttpResponse response = client.execute(request);
        // 指定响应的字符编码
        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("请求失败: HTTP " + statusCode);
        }

        log.info("--------------------getJobDefinitionList-------------------------");
        log.info(responseBody);
        log.info("--------------------getJobDefinitionList-------------------------");

        try {
            return objectMapper.readTree(responseBody).get("data").toString();
        } catch (JsonProcessingException e) {
            throw new IOException("解析响应数据失败", e);
        }
    }

}
