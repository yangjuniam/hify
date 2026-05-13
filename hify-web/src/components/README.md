# Hify 公共组件使用指南

## HifyTable - 通用表格组件

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| columns | `TableColumn<T>[]` | 必填 | 列配置 |
| api | `ApiMethod<PageResult<T>, PageParams>` | 必填 | 数据获取 API |
| showPagination | `boolean` | `true` | 是否显示分页 |
| initialPage | `number` | `1` | 初始页码 |
| initialPageSize | `number` | `20` | 初始每页条数 |

### Exposed Methods

| 方法 | 说明 |
|------|------|
| `refresh()` | 刷新表格数据 |

### 使用示例

```vue
<template>
  <HifyTable
    ref="tableRef"
    :columns="columns"
    :api="fetchProviderList"
  >
    <!-- 自定义操作列 -->
    <template #actions="{ row }">
      <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </HifyTable>
</template>

<script setup lang="ts">
import HifyTable from '@/components/HifyTable.vue'
import type { TableColumn } from '@/types'

const columns: TableColumn[] = [
  { label: '名称', prop: 'name', minWidth: 150 },
  { label: '提供商', prop: 'provider', width: 120 },
  { label: '状态', prop: 'status', width: 100 },
  { label: '操作', prop: 'actions', width: 180, slot: 'actions' },
]

const fetchProviderList = async (params: PageParams) => {
  // 调用 API
  return { list: [], total: 0, page: params.page, pageSize: params.pageSize }
}

const tableRef = ref()

const handleEdit = (row: any) => {
  console.log('编辑', row)
}

const handleDelete = (row: any) => {
  console.log('删除', row)
}
</script>
```

---

## HifyFormDialog - 通用表单弹窗

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | `string` | 必填 | 弹窗标题 |
| width | `string \| number` | `'600px'` | 弹窗宽度 |
| rules | `FormRules` | - | 表单验证规则 |
| labelWidth | `string` | `'100px'` | 标签宽度 |
| modelValue | `boolean` | 必填 | 弹窗显示/隐藏 |
| editData | `T \| null` | `null` | 编辑数据 |

### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| `update:modelValue` | `boolean` | 弹窗状态变化 |
| `submit` | `T` | 提交表单 |

### Exposed Methods

| 方法 | 说明 |
|------|------|
| `open(data?)` | 打开弹窗，传 data 为编辑模式 |
| `close()` | 关闭弹窗 |
| `reset()` | 重置表单 |

### 使用示例

```vue
<template>
  <HifyFormDialog
    v-model="dialogVisible"
    title="编辑提供商"
    :rules="formRules"
    @submit="handleSubmit"
  >
    <template #default="{ formData }">
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入名称" />
      </el-form-item>
      <el-form-item label="提供商" prop="provider">
        <el-select v-model="formData.provider" placeholder="请选择">
          <el-option label="OpenAI" value="openai" />
          <el-option label="Claude" value="claude" />
        </el-select>
      </el-form-item>
    </template>
  </HifyFormDialog>
</template>

<script setup lang="ts">
import HifyFormDialog from '@/components/HifyFormDialog.vue'
import type { FormRules } from 'element-plus'

const dialogVisible = ref(false)
const dialogRef = ref()

const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
}

const handleSubmit = async (data: any) => {
  console.log('提交数据:', data)
  // 调用 API
  dialogVisible.value = false
}

const openDialog = () => {
  dialogRef.value?.open()
}
</script>
```

---

## useConfirm - 删除确认

### 用法

```ts
import { useConfirm } from '@/composables/useConfirm'

const confirmDelete = useConfirm('确定要删除此项吗？', deleteProvider, '删除成功')

const handleDelete = async (id: number) => {
  const confirmed = await confirmDelete(id)
  if (confirmed) {
    // 删除成功，刷新列表
    tableRef.value?.refresh()
  }
}
```

---

## useRequest - 请求状态管理

### 用法

```ts
import { useRequest } from '@/composables/useRequest'

// 手动触发
const { data, loading, error, execute } = useRequest(getProviderList)

const fetchData = async () => {
  await execute({ page: 1, pageSize: 20 })
}

// 立即执行
const { data, loading } = useRequest(getProviderList, true)
```

---

## notify - 统一通知

### 用法

```ts
import { notifySuccess, notifyError, notifyWarning } from '@/utils/notify'

notifySuccess('操作成功')
notifyError('操作失败')
notifyWarning('请检查输入')

// 自定义配置
notifySuccess('保存成功', { duration: 5000 })
```
