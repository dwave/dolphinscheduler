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
  const options = ref([] as { value: number; label: string }[])
  const loading = ref(false)
  const getWorkerGroups = () => {
    if (loading.value) return
    loading.value = true
    queryListWorkerDefinition({
      pageSize: 100,
      pageNo: 1,
      searchName: '',
      userId: 2
    }).then((res) => {
      let resdata = JSON.parse(res)
      options.value = resdata.data.map((item: any) => ({
        value: item.id,
        label: item.name
      }))
      loading.value = false
    })
  }

  const refreshOptions = () => {
    const parameters = {
      jobDefineId: model.projectsWorkerDefinition,
      userID: 2
    } as TypeReq
    queryRawScript(parameters).then((res) => {
      model.rawScript = res
    })
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
      options: options
      // validate: {
      //   trigger: ['input', 'blur'],
      //   required: false,
      //   message: t('project.node.worker_group_definition_tips')
      // }
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
