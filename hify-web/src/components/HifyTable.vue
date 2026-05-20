<template>
  <div class="hify-table">
    <el-table
      v-loading="loading"
      :data="tableData"
      :header-cell-style="headerCellStyle"
      :cell-style="cellStyle"
      :row-style="props.rowStyle"
      stripe
    >
      <template v-for="column in columns" :key="column.prop">
        <el-table-column
          v-if="!column.slot && !column.render"
          :prop="String(column.prop)"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align"
          :fixed="column.fixed"
        />
        <el-table-column
          v-else
          :prop="String(column.prop)"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align"
          :fixed="column.fixed"
        >
          <template #default="{ row, $index }">
            <!-- 优先使用 slot -->
            <slot
              v-if="column.slot"
              :name="column.slot"
              :row="row"
              :index="$index"
            />
            <!-- 其次使用 render 函数 -->
            <template v-else-if="column.render">
              {{ column.render(row as T, $index) }}
            </template>
          </template>
        </el-table-column>
      </template>
    </el-table>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && tableData.length === 0"
      description="暂无数据"
      :image-size="80"
      class="table-empty"
    />

    <!-- 分页 -->
    <div v-if="showPagination" class="table-pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { ref, onMounted } from 'vue'
import type { TableColumn, ApiMethod, PageParams, PageResult } from '@/types'

/**
 * HifyTable 组件 Props
 */
interface Props {
  /** 列配置 */
  columns: TableColumn<T>[]
  /** 数据获取 API */
  api: ApiMethod<PageResult<T>, PageParams>
  /** 是否显示分页，默认 true */
  showPagination?: boolean
  /** 初始页码，默认 1 */
  initialPage?: number
  /** 初始每页条数，默认 20 */
  initialPageSize?: number
  /** 行样式函数 */
  rowStyle?: (data: { row: T; rowIndex: number }) => Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  showPagination: true,
  initialPage: 1,
  initialPageSize: 20,
})

// 表格数据
const tableData = ref<T[]>([])
const loading = ref(false)

// 分页参数
const pagination = ref({
  page: props.initialPage,
  pageSize: props.initialPageSize,
  total: 0,
})

// 表格样式
const headerCellStyle = {
  backgroundColor: 'var(--color-bg-secondary)',
  color: 'var(--color-text-secondary)',
  fontWeight: 600,
  fontSize: '14px',
}

const cellStyle = {
  color: 'var(--color-text-primary)',
  fontSize: '14px',
}

/**
 * 获取表格数据
 */
const fetchTableData = async () => {
  loading.value = true
  try {
    const params: PageParams = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
    }
    const result = await props.api(params)
    tableData.value = result.list || []
    pagination.value.total = result.total || 0
  } catch (error) {
    console.error('获取表格数据失败:', error)
    tableData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 刷新表格数据
 */
const refresh = () => {
  fetchTableData()
}

/**
 * 每页条数变化
 */
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  fetchTableData()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page: number) => {
  pagination.value.page = page
  fetchTableData()
}

// 暴露方法给父组件
defineExpose({
  refresh,
})

// 组件挂载时加载数据
onMounted(() => {
  fetchTableData()
})
</script>

<style scoped>
.hify-table {
  background-color: var(--color-bg-base);
  border-radius: var(--radius-md);
}

:deep(.el-table) {
  background-color: transparent;
}

:deep(.el-table__body-wrapper) {
  background-color: var(--color-bg-base);
  border-radius: 0 0 var(--radius-md) var(--radius-md);
}

:deep(.el-table__empty-block) {
  background-color: var(--color-bg-base);
}

.table-empty {
  padding: var(--spacing-8) 0;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  padding: var(--spacing-4) 0;
  padding-top: var(--spacing-4);
  border-top: 1px solid var(--color-border-subtle);
  margin-top: var(--spacing-4);
}

:deep(.el-pagination) {
  --el-pagination-text-color: var(--color-text-secondary);
  --el-pagination-bg-color: var(--color-bg-base);
  --el-pagination-button-bg-color: var(--color-bg-base);
  --el-pagination-button-color: var(--color-text-secondary);
  --el-pagination-border-radius: var(--radius-sm);
}

:deep(.el-pager li) {
  background-color: var(--color-bg-base);
  border-radius: var(--radius-sm);
}

:deep(.el-pager li.is-active) {
  background: var(--gradient-primary);
  color: white;
  border-radius: var(--radius-sm);
}

:deep(.el-pagination button) {
  border-radius: var(--radius-sm);
}

:deep(.el-pagination__sizes .el-select .el-input__wrapper) {
  border-radius: var(--radius-sm);
}

:deep(.el-pagination__jump .el-input__wrapper) {
  border-radius: var(--radius-sm);
}
</style>
