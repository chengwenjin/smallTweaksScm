<template>
  <div class="inquiry-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="询价单编号">
          <el-input v-model="searchForm.inquiryNo" placeholder="请输入询价单编号" clearable />
        </el-form-item>
        <el-form-item label="询价单名称">
          <el-input v-model="searchForm.inquiryName" placeholder="请输入询价单名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="报价中" :value="2" />
            <el-option label="报价结束" :value="3" />
            <el-option label="已比价" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增询价单
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="inquiryNo" label="询价单编号" width="150" />
        <el-table-column prop="inquiryName" label="询价单名称" width="180" show-overflow-tooltip />
        <el-table-column prop="deadline" label="报价截止日期" width="130" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handlePublish(row)" 
              :disabled="row.status !== 0"
            >
              发布
            </el-button>
            <el-button 
              link 
              type="warning" 
              size="small" 
              @click="handleComparison(row)" 
              :disabled="row.status !== 3"
            >
              比价
            </el-button>
            <el-button 
              link 
              type="info" 
              size="small" 
              @click="handleCancel(row)" 
              :disabled="row.status === 5 || row.status === 4"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      v-model="viewDialogVisible"
      title="询价单详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="询价单编号">{{ currentInquiry.inquiryNo }}</el-descriptions-item>
        <el-descriptions-item label="询价单名称">{{ currentInquiry.inquiryName }}</el-descriptions-item>
        <el-descriptions-item label="报价截止日期">{{ currentInquiry.deadline }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentInquiry.status)">
            {{ getStatusName(currentInquiry.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentInquiry.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentInquiry.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱" :span="2">{{ currentInquiry.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="询价说明" :span="2">{{ currentInquiry.description }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">报价供应商列表</el-divider>
      
      <el-table :data="supplierList" border stripe v-loading="supplierLoading">
        <el-table-column prop="supplierName" label="供应商名称" width="180" />
        <el-table-column prop="inviteStatus" label="邀请状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.inviteStatus === 1 ? 'success' : 'info'">
              {{ row.inviteStatus === 1 ? '已邀请' : row.inviteStatus === 2 ? '已拒绝' : '待邀请' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quoteStatus" label="报价状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.quoteStatus === 1 ? 'success' : 'info'">
              {{ row.quoteStatus === 1 ? '已报价' : row.quoteStatus === 2 ? '已撤回' : '未报价' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">
            <span v-if="row.unitPrice">¥{{ row.unitPrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="120">
          <template #default="{ row }">
            <span v-if="row.totalPrice">¥{{ row.totalPrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="交货日期" width="120" />
        <el-table-column prop="paymentTerms" label="付款条件" width="150" />
        <el-table-column prop="warranty" label="质保期" width="100" />
        <el-table-column prop="quoteTime" label="报价时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getInquiryList, publishInquiry, cancelInquiry, getInquirySuppliers } from '@/api/inquiry'

const router = useRouter()
const loading = ref(false)
const supplierLoading = ref(false)
const viewDialogVisible = ref(false)

const searchForm = reactive({
  inquiryNo: '',
  inquiryName: '',
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentInquiry = ref<any>({})
const supplierList = ref<any[]>([])

const statusMap: Record<number, string> = {
  0: '待发布',
  1: '已发布',
  2: '报价中',
  3: '报价结束',
  4: '已比价',
  5: '已取消'
}

const getStatusName = (status: number) => statusMap[status] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'primary'
    case 3: return 'warning'
    case 4: return 'success'
    case 5: return 'info'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getInquiryList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.inquiryNo = ''
  searchForm.inquiryName = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  router.push('/scm/requirement')
}

const handleView = async (row: any) => {
  currentInquiry.value = { ...row }
  viewDialogVisible.value = true
  await loadSuppliers(row.id)
}

const loadSuppliers = async (inquiryId: number) => {
  supplierLoading.value = true
  try {
    const res = await getInquirySuppliers(inquiryId)
    supplierList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    supplierLoading.value = false
  }
}

const handlePublish = (row: any) => {
  ElMessageBox.confirm('确定要发布该询价单吗？发布后供应商将可以查看并报价。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await publishInquiry(row.id)
      ElMessage.success('发布成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleComparison = (row: any) => {
  router.push({
    path: '/scm/comparison',
    query: { inquiryId: row.id }
  })
}

const handleCancel = (row: any) => {
  ElMessageBox.confirm('确定要取消该询价单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelInquiry(row.id)
      ElMessage.success('取消成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.inquiry-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
