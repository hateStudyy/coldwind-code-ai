<template>
  <a-modal v-model:open="visible" :title="t('appDetail.title')" :footer="null" width="500px">
    <div class="app-detail-content">
      <!-- 应用基础信息 -->
      <div class="app-basic-info">
        <div class="info-item">
          <span class="info-label">{{ t('appDetail.creator') }}</span>
          <UserInfo :user="app?.user" size="small" />
        </div>
        <div class="info-item">
          <span class="info-label">{{ t('appDetail.createdAt') }}</span>
          <span>{{ formatTime(app?.createTime) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">{{ t('appDetail.codeGenType') }}</span>
          <a-tag v-if="app?.codeGenType" color="blue">
            {{ formatLocalizedCodeGenType(app.codeGenType) }}
          </a-tag>
          <span v-else>{{ t('codeGen.unknown') }}</span>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="showActions" class="app-actions">
        <a-space>
          <a-button type="primary" @click="handleEdit">
            <template #icon>
              <EditOutlined />
            </template>
            {{ t('appDetail.edit') }}
          </a-button>
          <a-popconfirm
            :title="t('appDetail.deleteConfirm')"
            @confirm="handleDelete"
            :ok-text="t('common.confirm')"
            :cancel-text="t('common.cancel')"
          >
            <a-button danger>
              <template #icon>
                <DeleteOutlined />
              </template>
              {{ t('appDetail.delete') }}
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'
import { CodeGenTypeEnum } from '@/utils/codeGenTypes'
import { useI18n } from '@/i18n'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()
const { t } = useI18n()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}

const formatLocalizedCodeGenType = (type: string | undefined): string => {
  if (!type) {
    return t('codeGen.unknown')
  }
  const labels: Record<string, string> = {
    [CodeGenTypeEnum.HTML]: t('codeGen.html'),
    [CodeGenTypeEnum.MULTI_FILE]: t('codeGen.multiFile'),
    [CodeGenTypeEnum.VUE_PROJECT]: t('codeGen.vueProject'),
  }
  return labels[type] ?? type
}
</script>

<style scoped>
.app-detail-content {
  padding: 8px 0;
}

.app-basic-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  width: 80px;
  color: #666;
  font-size: 14px;
  flex-shrink: 0;
}

.app-actions {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
