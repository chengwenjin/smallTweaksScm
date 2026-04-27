<template>
  <div class="oa-approval-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="审批单编号">
          <el-input v-model="searchForm.approvalNo" placeholder="请输入审批单编号" clearable />
        </el-form-item>
        <el-form-item label="审批标题">
          <el-input v-model="searchForm.approvalTitle" placeholder="请输入审批标题" clearable />
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="searchForm.sourceType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="采购申请" :value="1" />
            <el-option label="采购计划" :value="2" />
            <el-option label="采购订单" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="searchForm.approvalStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="审批中" value="APPROVING" />
            <el-option label="审批通过" value="APPROVED" />
            <el-option label="审批拒绝" value="REJECTED" />
            <el-option label="已撤回" value="WITHDRAWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前审批人">
          <el-input v-model="searchForm.currentApproverName" placeholder="请输入审批人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="500">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="approvalNo" label="审批单编号" width="150" />
        <el-table-column prop="approvalTitle" label="审批标题" width="200" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源类型" width="100">
          <template #default="{ row }">
            {{ getSourceTypeName(row.sourceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceNo" label="来源单据编号" width="150" />
        <el-table-column prop="currentApproverName" label="当前审批人" width="100" />
        <el-table-column prop="approvalStatus" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)">
              {{ getApprovalStatusName(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="approvalTime" label="审批时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleApprove(row)" 
              v-if="row.approvalStatus === 'APPROVING' || row.approvalStatus === 'PENDING'"
            >
              通过
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleReject(row)" 
              v-if="row.approvalStatus === 'APPROVING' || row.approvalStatus === 'PENDING'"
            >
              拒绝
            </el-button>
            <el-button 
              link 
              type="warning" 
              size="small" 
              @click="handleWithdraw(row)" 
              v-if="row.approvalStatus === 'APPROVING'"
            >
              撤回
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
      title="查看审批详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="审批单编号">{{ viewData.approvalNo }}</el-descriptions-item>
        <el-descriptions-item label="审批标题">{{ viewData.approvalTitle }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">{{ getSourceTypeName(viewData.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="来源单据编号">{{ viewData.sourceNo }}</el-descriptions-item>
        <el-descriptions-item label="当前审批人">{{ viewData.currentApproverName }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getApprovalStatusType(viewData.approvalStatus)">
            {{ getApprovalStatusName(viewData.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ viewData.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ viewData.approvalTime }}</el-descriptions-item>
        <el-descriptions-item label="审批备注" :span="2">{{ viewData.approvalRemark }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider v-if="viewData.approvalHistory" content-position="left">审批历史</el-divider>
      <el-timeline v-if="viewData.approvalHistory">
        <el-timeline-item
          v-for="(item, index) in parseApprovalHistory(viewData.approvalHistory)"
          :key="index"
          :timestamp="item.time"
          placement="top"
        >
          <el-card>
            <h4>{{ item.action }}</h4>
            <p>{{ item.approver }} - {{ item.remark }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog
      v-model="processDialogVisible"
      :title="processDialogTitle"
      width="500px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input 
            v-model="processForm.approvalRemark" 
            type="textarea" 
            :rows="3" 
            :placeholder="processAction === 'approve' ? '请输入审批通过意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button :type="processAction === 'approve' ? 'success' : 'danger'" @click="handleConfirmProcess" :loading="submitLoading">
          {{ processAction === 'approve' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getOaApprovalList, 
  getOaApprovalDetail,
  processApproval,
  withdrawApproval
} from '@/api/oaApproval'

const loading = ref(false)
const submitLoading = ref(false)

const viewDialogVisible = ref(false)
const viewData = ref<any>({})

const processDialogVisible = ref(false)
const processDialogTitle = ref('')
const processAction = ref('')
const currentApprovalId = ref<number | null>(null)

const searchForm = reactive({
  approvalNo: '',
  approvalTitle: '',
  sourceType: null as number | null,
  approvalStatus: '',
  currentApproverName: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const processForm = reactive({
  approvalRemark: ''
})

const sourceTypeMap: Record<number, string> = {
  1: '采购申请',
  2: '采购计划',
  3: '采购订单'
}

const approvalStatusMap: Record<string, string> = {
  PENDING: '待审批',
  APPROVING: '审批中',
  APPROVED: '审批通过',
  REJECTED: '审批拒绝',
  WITHDRAWN: '已撤回'
}

const getSourceTypeName = (type: number) => sourceTypeMap[type] || '未知'
const getApprovalStatusName = (status: string) => approvalStatusMap[status] || '未知'

const getApprovalStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'success'
    case 'APPROVING':
      return 'warning'
    case 'REJECTED':
    case 'WITHDRAWN':
      return 'danger'
    default:
      return 'info'
  }
}

const parseApprovalHistory = (history: string) => {
  try {
    const parsed = JSON.parse(history)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
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
    const res = await getOaApprovalList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
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
  searchForm.approvalNo = ''
  searchForm.approvalTitle = ''
  searchForm.sourceType = null
  searchForm.approvalStatus = ''
  searchForm.currentApproverName = ''
  handleSearch()
}

const handleView = async (row: any) => {
  try {
    const res = await getOaApprovalDetail(row.id)
    viewData.value = res.data || {}
    viewDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleApprove = (row: any) => {
  processDialogTitle.value = '审批通过'
  processAction.value = 'approve'
  processForm.approvalRemark = ''
  currentApprovalId.value = row.id
  processDialogVisible.value = true
}

const handleReject = (row: any) => {
  processDialogTitle.value = '审批拒绝'
  processAction.value = 'reject'
  processForm.approvalRemark = ''
  currentApprovalId.value = row.id
  processDialogVisible.value = true
}

const handleWithdraw = (row: any) => {
  ElMessageBox.confirm('确定要撤回该审批吗？撤回后审批流程将终止。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await withdrawApproval(row.id)
      ElMessage.success('撤回成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleConfirmProcess = async () => {
  if (!currentApprovalId.value) return
  
  if (processAction.value === 'reject' && !processForm.approvalRemark) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  submitLoading.value = true
  try {
    const data: any = {
      action: processAction.value,
      approvalRemark: processForm.approvalRemark
    }
    
    await processApproval(currentApprovalId.value, data)
    ElMessage.success(processAction.value === 'approve' ? '审批通过成功' : '审批拒绝成功')
    processDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.oa-approval-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
