<template>
  <div class="login-log">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="登录状态">
          <el-select v-model="searchForm.loginStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录时间">
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
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="location" label="登录地点" />
        <el-table-column prop="browser" label="浏览器" width="150" />
        <el-table-column prop="os" label="操作系统" width="150" />
        <el-table-column prop="loginStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.loginStatus === 1 ? 'success' : 'danger'">
              {{ row.loginStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getLoginLogList, deleteLoginLog, batchDeleteLoginLogs, clearAllLoginLogs } from '@/api/loginLog'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref<number[]>([])
const dateRange = ref<[string, string] | null>(null)

const searchForm = reactive({
  username: '',
  loginStatus: null as number | null
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
    
    const res: any = await getLoginLogList(params)
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
  searchForm.username = ''
  searchForm.loginStatus = null
  dateRange.value = null
  handleSearch()
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedIds.value = rows.map((row: any) => row.id)
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteLoginLog(row.id)
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
    await batchDeleteLoginLogs(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    getList()
  })
}

// 清空所有
const handleClearAll = () => {
  ElMessageBox.confirm('确定要清空所有登录日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    await clearAllLoginLogs()
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
</style>
