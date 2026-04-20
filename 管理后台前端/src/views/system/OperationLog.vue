<template>
  <div class="operation-log">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人员">
          <el-input v-model="searchForm.operatorName" placeholder="请输入操作人员" clearable />
        </el-form-item>
        <el-form-item label="模块名称">
          <el-input v-model="searchForm.module" placeholder="请输入模块名称" clearable />
        </el-form-item>
        <el-form-item label="日志类型">
          <el-select v-model="searchForm.logType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="查询" :value="1" />
            <el-option label="新增" :value="2" />
            <el-option label="修改" :value="3" />
            <el-option label="删除" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="toolbar">
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button type="danger" @click="handleClearAll">
          <el-icon><Delete /></el-icon>
          清空所有
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table 
        :data="tableData" 
        border 
        stripe 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorName" label="操作人员" width="120" />
        <el-table-column prop="module" label="模块名称" width="150" />
        <el-table-column prop="description" label="操作描述" />
        <el-table-column prop="logType" label="日志类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLogTypeTag(row.logType)">
              {{ getLogTypeText(row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="operateTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="操作人员">{{ currentRow.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="模块名称">{{ currentRow.module }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentRow.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentRow.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentRow.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentRow.ip }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent">{{ currentRow.userAgent }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'">
            {{ currentRow.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentRow.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentRow.operateTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre>{{ currentRow.requestParams }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <pre>{{ currentRow.responseResult }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2" v-if="currentRow.errorMsg">
          <pre style="color: red">{{ currentRow.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getOperationLogList, deleteOperationLog, batchDeleteOperationLogs, clearAllOperationLogs } from '@/api/log'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref<number[]>([])
const detailVisible = ref(false)
const currentRow = ref<any>(null)
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  operatorName: '',
  module: '',
  logType: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const res: any = await getOperationLogList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

// 重置
const handleReset = () => {
  searchForm.operatorName = ''
  searchForm.module = ''
  searchForm.logType = null
  dateRange.value = null
  handleSearch()
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows.map((row: any) => row.id)
}

// 查看详情
const handleView = (row: any) => {
  currentRow.value = row
  detailVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteOperationLog(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await batchDeleteOperationLogs(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

// 清空所有
const handleClearAll = () => {
  ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    await clearAllOperationLogs()
    ElMessage.success('清空成功')
    getList()
  })
}

// 分页
const handleSizeChange = () => {
  getList()
}

const handleCurrentChange = () => {
  getList()
}

// 日志类型标签
const getLogTypeTag = (type: number) => {
  const map: Record<number, string> = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return map[type] || 'info'
}

// 日志类型文本
const getLogTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: '查询',
    2: '新增',
    3: '修改',
    4: '删除'
  }
  return map[type] || '未知'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

pre {
  max-height: 200px;
  overflow-y: auto;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}
</style>
