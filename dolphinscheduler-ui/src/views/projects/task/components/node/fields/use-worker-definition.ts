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

import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  queryListWorkerDefinition,
  queryRawScript
} from '@/service/modules/projects-worker-definition'
import type { IJsonItem } from '../types'

export function useWorkerDefinition(model: {
  [field: string]: any
}): IJsonItem {
  const { t } = useI18n()
  const configEditorSpan = computed(() => (model.useCustom ? 24 : 0))
  const options = ref([] as { label: string; value: string }[])
  const loading = ref(false)
  const getWorkerGroups = async () => {
    if (loading.value) return
    loading.value = true
    await queryListWorkerDefinition({
      pageSize: 100,
      pageNo: 1,
      searchName: '',
      userId: 2
    }).then((res: any) => {
      options.value = res.data.map((item: any) => ({
        label: item.name,
        value: item.id
      }))
    })
    loading.value = false
  }

  const refreshOptions = async () => {
    const parameters = {
      jobid: model.projectsWorkerDefinition,
      userId: 2
    } as TypeReq
    const res = await queryRawScript(parameters)
    // datasourceOptions.value = res.map((item: any) => ({
    // 	label: item.name,
    // 	value: item.id
    // }))
    // const sourceField = params.sourceField || 'datasource'
    // if (!res.length && model[sourceField]) model[sourceField] = null
    // if (res.length && model[sourceField]) {
    // 	const item = find(res, { id: model[sourceField] })
    // 	if (!item) {
    // 		model[sourceField] = null
    // 	}
    // }
  }

  const onChange = () => {
    refreshOptions()
  }

  onMounted(() => {
    getWorkerGroups()
  })
  return [
    {
      type: 'select',
      field: 'projectsWorkerDefinition',
      span: 24,
      name: t('project.node.worker_group_definition'),
      props: {
        loading: loading,
        'on-update:value': onChange
      },
      options: options,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.worker_group_definition_tips')
      }
    },
    {
      type: 'editor',
      field: 'rawScript',
      name: t('project.node.script'),
      span: configEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: model.useCustom,
        validator(validate: any, value: string) {
          if (model.useCustom && !value) {
            return new Error(t('project.node.script_tips'))
          }
        }
      }
    }
  ]
}
