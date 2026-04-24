<template>
  <div class="tender-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="招标单编号">
          <el-input v-model="searchForm.tenderNo" placeholder="请输入招标单编号" clearable />
        </el-form-item>
        <el-form-item label="招标单名称">
          <el-input v-model="searchForm.tenderName" placeholder="请输入招标单名称" clearable />
        </el-form-item>
        <el-form-item label="招标类型">
          <el-select v-model="searchForm.tenderType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="公开招标" :value="1" />
            <el-option label="邀请招标" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待发布" :value="0" />
            <el-option label="招标中" :value="1" />
            <el-option label="投标中" :value="2" />
            <el-option label="已开标" :value="3" />
            <el-option label="评标中" :value="4" />
            <el-option label="已定标" :value="5" />
            <el-option label="已取消" :value="6" />
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
          新增招标单
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tenderNo" label="招标单编号" width="150" />
        <el-table-column prop="tenderName" label="招标单名称" width="180" show-overflow-tooltip />
        <el-table-column prop="tenderType" label="招标类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.tenderType === 1 ? 'primary' : 'success'">
              {{ row.tenderType === 1 ? '公开招标' : '邀请招标' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="estimatedBudget" label="预算金额" width="120">
          <template #default="{ row }">
            <span>¥{{ row.estimatedBudget }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bidDeadline" label="投标截止日期" width="130" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="380" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" size="small" @click="handleEdit(row)" :disabled="row.status > 0">编辑</el-button>
            <el-button link type="success" size="small" @click="handlePublish(row)" :disabled="row.status !== 0">发布</el-button>
            <el-button link type="info" size="small" @click="handleOpen(row)" :disabled="row.status !== 2">开标</el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleCancel(row)" 
              :disabled="row.status === 5 || row.status === 6"
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
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="招标单名称" prop="tenderName">
          <el-input v-model="form.tenderName" placeholder="请输入招标单名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招标类型" prop="tenderType">
              <el-select v-model="form.tenderType" placeholder="请选择" style="width: 100%">
                <el-option label="公开招标" :value="1" />
                <el-option label="邀请招标" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算金额" prop="estimatedBudget">
              <el-input-number v-model="form.estimatedBudget" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="招标描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入招标描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="发布日期" prop="publishDate">
              <el-date-picker
                v-model="form.publishDate"
                type="date"
                placeholder="请选择发布日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="投标截止日期" prop="bidDeadline">
              <el-date-picker
                v-model="form.bidDeadline"
                type="date"
                placeholder="请选择投标截止日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开标日期" prop="openDate">
              <el-date-picker
                v-model="form.openDate"
                type="date"
                placeholder="请选择开标日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">联系方式</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="招标地址" prop="tenderAddress">
          <el-input v-model="form.tenderAddress" placeholder="请输入招标地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="招标单详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="招标单编号">{{ currentTender.tenderNo }}</el-descriptions-item>
        <el-descriptions-item label="招标单名称">{{ currentTender.tenderName }}</el-descriptions-item>
        <el-descriptions-item label="招标类型">
          <el-tag :type="currentTender.tenderType === 1 ? 'primary' : 'success'">
            {{ currentTender.tenderType === 1 ? '公开招标' : '邀请招标' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预算金额">¥{{ currentTender.estimatedBudget }}</el-descriptions-item>
        <el-descriptions-item label="发布日期">{{ currentTender.publishDate }}</el-descriptions-item>
        <el-descriptions-item label="投标截止日期">{{ currentTender.bidDeadline }}</el-descriptions-item>
        <el-descriptions-item label="开标日期">{{ currentTender.openDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTender.status)">
            {{ getStatusName(currentTender.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentTender.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentTender.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱" :span="2">{{ currentTender.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="招标地址" :span="2">{{ currentTender.tenderAddress }}</el-descriptions-item>
        <el-descriptions-item label="招标描述" :span="2">{{ currentTender.description }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentTender.status >= 5" style="margin-top: 20px">
        <el-divider content-position="left">中标信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="中标供应商">{{ currentTender.winSupplierName }}</el-descriptions-item>
          <el-descriptions-item label="中标价格">¥{{ currentTender.winPrice }}</el-descriptions-item>
          <el-descriptions-item label="中标日期">{{ currentTender.winDate }}</el-descriptions-item>
          <el-descriptions-item label="中标原因" :span="2">{{ currentTender.winReason }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider content-position="left">投标供应商列表</el-divider>
      
      <el-table :data="bidList" border stripe v-loading="bidLoading">
        <el-table-column prop="supplierName" label="供应商名称" width="180" />
        <el-table-column prop="bidPrice" label="投标价格" width="140">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold">¥{{ row.bidPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bidTime" label="投标时间" width="180" />
        <el-table-column prop="score" label="评分" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.isWin ? '#67C23A' : '' }">{{ row.score || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="isWin" label="是否中标" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isWin" type="success">中标</el-tag>
            <el-tag v-else type="info">未中标</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="technicalParams" label="技术参数" width="150" show-overflow-tooltip />
        <el-table-column prop="deliveryPlan" label="交货计划" width="150" show-overflow-tooltip />
        <el-table-column prop="afterSalesService" label="售后服务" width="150" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="awardDialogVisible"
      title="定标"
      width="600px"
    >
      <el-form :model="awardForm" :rules="awardRules" ref="awardFormRef" label-width="120px">
        <el-form-item label="选择中标供应商" prop="winSupplierId">
          <el-select v-model="awardForm.winSupplierId" placeholder="请选择中标供应商" style="width: 100%">
            <el-option
              v-for="item in bidList"
              :key="item.supplierId"
              :label="`${item.supplierName} - ¥${item.bidPrice}`"
              :value="item.supplierId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="中标价格" prop="winPrice">
          <el-input-number v-model="awardForm.winPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="中标原因" prop="winReason">
          <el-input v-model="awardForm.winReason" type="textarea" :rows="3" placeholder="请输入中标原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="awardDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAward" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getTenderList, 
  getTenderDetail, 
  createTender, 
  updateTender, 
  publishTender, 
  cancelTender, 
  openTender, 
  awardTender,
  getTenderBids 
} from '@/api/tender'

const loading = ref(false)
const submitLoading = ref(false)
const bidLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const awardDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const awardFormRef = ref<FormInstance>()

const searchForm = reactive({
  tenderNo: '',
  tenderName: '',
  tenderType: null as number | null,
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentTender = ref<any>({})
const bidList = ref<any[]>([])
const currentTenderId = ref<number | null>(null)

const form = reactive({
  id: null as number | null,
  tenderName: '',
  tenderType: 1,
  description: '',
  estimatedBudget: 0,
  publishDate: '',
  bidDeadline: '',
  openDate: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  tenderAddress: '',
  remark: ''
})

const awardForm = reactive({
  winSupplierId: null as number | null,
  winPrice: 0,
  winReason: ''
})

const rules: FormRules = {
  tenderName: [{ required: true, message: '请输入招标单名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const awardRules: FormRules = {
  winSupplierId: [{ required: true, message: '请选择中标供应商', trigger: 'change' }],
  winPrice: [{ required: true, message: '请输入中标价格', trigger: 'blur' }],
  winReason: [{ required: true, message: '请输入中标原因', trigger: 'blur' }]
}

const statusMap: Record<number, string> = {
  0: '待发布',
  1: '招标中',
  2: '投标中',
  3: '已开标',
  4: '评标中',
  5: '已定标',
  6: '已取消'
}

const getStatusName = (status: number) => statusMap[status] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 5: return 'success'
    case 6: return 'info'
    case 0: return 'info'
    case 2: return 'primary'
    case 3: return 'warning'
    case 4: return 'warning'
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
    const res = await getTenderList(params)
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
  searchForm.tenderNo = ''
  searchForm.tenderName = ''
  searchForm.tenderType = null
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增招标单'
  isEdit.value = false
  Object.assign(form, {
    id: null,
    tenderName: '',
    tenderType: 1,
    description: '',
    estimatedBudget: 0,
    publishDate: '',
    bidDeadline: '',
    openDate: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    tenderAddress: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑招标单'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = async (row: any) => {
  currentTender.value = { ...row }
  currentTenderId.value = row.id
  viewDialogVisible.value = true
  await loadBids(row.id)
}

const loadBids = async (tenderId: number) => {
  bidLoading.value = true
  try {
    const res = await getTenderBids(tenderId)
    bidList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    bidLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateTender(form.id!, form)
          ElMessage.success('更新成功')
        } else {
          await createTender(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handlePublish = (row: any) => {
  ElMessageBox.confirm('确定要发布该招标单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await publishTender(row.id)
      ElMessage.success('发布成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleOpen = (row: any) => {
  ElMessageBox.confirm('确定要开标吗？开标后将无法再接收投标。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await openTender(row.id)
      ElMessage.success('开标成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleCancel = (row: any) => {
  ElMessageBox.confirm('确定要取消该招标单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelTender(row.id)
      ElMessage.success('取消成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmitAward = async () => {
  if (!awardFormRef.value || !currentTenderId.value) return
  
  await awardFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await awardTender(currentTenderId.value, awardForm)
        ElMessage.success('定标成功')
        awardDialogVisible.value = false
        viewDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.tender-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
