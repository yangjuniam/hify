/**
 * 分页请求参数
 */
export interface PageParams {
  page: number
  pageSize: number
}

/**
 * 分页响应结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/**
 * 表格列配置
 */
export interface TableColumn<T = any> {
  /** 列标题 */
  label: string
  /** 对应数据属性 */
  prop: keyof T | string
  /** 列宽度 */
  width?: number | string
  /** 最小宽度 */
  minWidth?: number | string
  /** 是否对齐 */
  align?: 'left' | 'center' | 'right'
  /** 是否固定列 */
  fixed?: boolean | 'left' | 'right'
  /** 自定义插槽名（如果需要自定义渲染） */
  slot?: string
  /** 自定义渲染函数 */
  render?: (row: T, index: number) => any
}

/**
 * API 请求方法类型
 */
export type ApiMethod<T = any, P = any> = (params: P) => Promise<T>
