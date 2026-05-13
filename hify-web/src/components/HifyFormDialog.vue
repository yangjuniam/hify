<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-position="labelPosition"
      :label-width="labelWidth"
    >
      <slot :form-data="formData" />
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

/**
 * HifyFormDialog 组件 Props
 */
interface Props {
  /** 弹窗标题 */
  title: string
  /** 弹窗宽度，默认 '600px' */
  width?: string | number
  /** 表单验证规则 */
  rules?: FormRules
  /** 标签位置，默认 'top' */
  labelPosition?: 'left' | 'right' | 'top'
  /** 标签宽度，默认 '100px' */
  labelWidth?: string
  /** 弹窗显示/隐藏 */
  modelValue: boolean
  /** 编辑数据（传表示编辑模式） */
  editData?: T | null
}

const props = withDefaults(defineProps<Props>(), {
  width: '600px',
  labelPosition: 'top',
  labelWidth: '100px',
  editData: null,
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'submit': [data: T]
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref<Partial<T>>({})

// 提交状态
const submitting = ref(false)

// 表单验证规则
const formRules = computed(() => props.rules || {})

// 弹窗显示状态
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

/**
 * 打开弹窗
 * @param data 编辑数据（可选）
 */
const open = (data?: T | null) => {
  if (data) {
    // 编辑模式：复制数据到表单
    formData.value = { ...data }
  } else {
    // 新增模式：重置表单
    resetForm()
  }
  dialogVisible.value = true
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.value = {}
  formRef.value?.resetFields()
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  resetForm()
  dialogVisible.value = false
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    emit('submit', formData.value as T)
  } catch (error) {
    console.error('提交表单失败:', error)
  } finally {
    submitting.value = false
  }
}

// 监听弹窗关闭，重置表单
watch(dialogVisible, (visible) => {
  if (!visible) {
    resetForm()
  }
})

// 监听编辑数据变化
watch(
  () => props.editData,
  (data) => {
    if (data) {
      formData.value = { ...data }
    }
  },
  { deep: true }
)

// 暴露方法给父组件
defineExpose({
  open,
  close: handleClose,
  reset: resetForm,
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-3);
}

:deep(.el-form-item__label) {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  transition: var(--transition-all);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-400) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary-500) inset;
}

:deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  transition: var(--transition-all);
}

:deep(.el-textarea__inner:hover) {
  border-color: var(--color-primary-400);
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary-500);
}
</style>
