<template>
  <div class="purchase-request-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="申请单编号">
          <el-input v-model="searchForm.reqNo" placeholder="请输入申请单编号" clearable />
        </el-form-item>
        <el-form-item label="申请单标题">
          <el-input v-model="searchForm.reqTitle" placeholder="请输入申请单标题" clearable />
        </el-form-item>
        <el-form-item label="申请部门">
          <el-select v-model="searchForm.reqDept" placeholder="请选择" clearable style="width: 120px">
            <el-option label="生产部" value="生产部" />
            <el-option label="技术部" value="技术部" />
            <el-option label="采购部" value="采购部" />
            <el-option label="质量部" value="质量部" />
            <el-option label="设备部" value="设备部" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="待提交" :value="1" />
            <el-option label="审批中" :value="2" />
            <el-option label="审批通过" :value="3" />
            <el-option label="审批拒绝" :value="4" />
            <el-option label="已转订单" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="searchForm.urgency" placeholder="请选择" clearable style="width: 120px">
            <el-option label="普通" :value="1" />
            <el-option label="紧急" :value="2" />
            <el-option label="特急" :value="3" />
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
          新增采购申请
        </el-button>
        <el-button type="success" @click="handleGenerateSummary">
          <el-icon><Document /></el-icon>
          生成需求汇总
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="500">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reqNo" label="申请单编号" width="150" />
        <el-table-column prop="reqTitle" label="申请单标题" width="200" show-overflow-tooltip />
        <el-table-column prop="reqDept" label="申请部门" width="100" />
        <el-table-column prop="reqPerson" label="申请人" width="80" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            {{ row.totalAmount ? '¥' + parseFloat(row.totalAmount).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="urgency" label="紧急程度" width="80">
          <template #default="{ row }">
            <el-tag :type="getUrgencyType(row.urgency)">
              {{ getUrgencyName(row.urgency) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="row.status === 0 || row.status === 1">编辑</el-button>
            <el-button link type="success" size="small" @click="handleSubmit(row)" v-if="row.status === 1">提交</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="row.status === 0 || row.status === 1">删除</el-button>
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请单标题" prop="reqTitle">
              <el-input v-model="form.reqTitle" placeholder="请输入申请单标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请部门" prop="reqDept">
              <el-select v-model="form.reqDept" placeholder="请选择" style="width: 100%">
                <el-option label="生产部" value="生产部" />
                <el-option label="技术部" value="技术部" />
                <el-option label="采购部" value="采购部" />
                <el-option label="质量部" value="质量部" />
                <el-option label="设备部" value="设备部" />
                <el-option label="仓库" value="仓库" />
                <el-option label="行政部" value="行政部" />
                <el-option label="财务部" value="财务部" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请人" prop="reqPerson">
              <el-input v-model="form.reqPerson" placeholder="请输入申请人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="reqPhone">
              <el-input v-model="form.reqPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="需求日期" prop="requiredDate">
              <el-date-picker
                v-model="form.requiredDate"
                type="date"
                placeholder="请选择需求日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急程度" prop="urgency">
              <el-select v-model="form.urgency" placeholder="请选择" style="width: 100%">
                <el-option label="普通" :value="1" />
                <el-option label="紧急" :value="2" />
                <el-option label="特急" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算来源" prop="budgetSource">
              <el-select v-model="form.budgetSource" placeholder="请选择" style="width: 100%">
                <el-option label="年度预算" value="年度预算" />
                <el-option label="紧急采购预算" value="紧急采购预算" />
                <el-option label="项目预算" value="项目预算" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交货地址" prop="deliveryAddress">
              <el-input v-model="form.deliveryAddress" placeholder="请输入交货地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="需求描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入需求描述" />
        </el-form-item>
        
        <el-divider content-position="left">采购明细</el-divider>
        
        <el-table :data="form.items" border stripe style="margin-bottom: 10px">
          <el-table-column prop="materialCode" label="物料编码" width="120">
            <template #default="{ row, $index }">
              <el-input v-model="row.materialCode" placeholder="物料编码" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="materialName" label="物料名称" width="150">
            <template #default="{ row, $index }">
              <el-input v-model="row.materialName" placeholder="物料名称" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="materialSpec" label="规格" width="100">
            <template #default="{ row, $index }">
              <el-input v-model="row.materialSpec" placeholder="规格" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="materialUnit" label="单位" width="80">
            <template #default="{ row, $index }">
              <el-input v-model="row.materialUnit" placeholder="单位" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="materialCategory" label="类别" width="80">
            <template #default="{ row, $index }">
              <el-select v-model="row.materialCategory" placeholder="类别" size="small" style="width: 100%">
                <el-option label="原材料" :value="1" />
                <el-option label="辅料" :value="2" />
                <el-option label="设备" :value="3" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100">
            <template #default="{ row, $index }">
              <el-input-number v-model="row.quantity" :min="0" :precision="2" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column prop="unitPrice" label="单价" width="100">
            <template #default="{ row, $index }">
              <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column prop="totalPrice" label="金额" width="100">
            <template #default="{ row }">
              {{ (row.quantity && row.unitPrice) ? '¥' + (parseFloat(row.quantity) * parseFloat(row.unitPrice)).toFixed(2) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="handleRemoveItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" size="small" @click="handleAddItem">
          <el-icon><Plus /></el-icon>
          添加明细
        </el-button>
        
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="查看采购申请"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请单编号">{{ viewData.reqNo }}</el-descriptions-item>
        <el-descriptions-item label="申请单标题">{{ viewData.reqTitle }}</el-descriptions-item>
        <el-descriptions-item label="申请部门">{{ viewData.reqDept }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ viewData.reqPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.reqPhone }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          {{ viewData.totalAmount ? '¥' + parseFloat(viewData.totalAmount).toFixed(2) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag :type="getUrgencyType(viewData.urgency)">
            {{ getUrgencyName(viewData.urgency) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)">
            {{ getStatusName(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="需求日期" :span="2">{{ viewData.requiredDate }}</el-descriptions-item>
        <el-descriptions-item label="交货地址" :span="2">{{ viewData.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="summaryDialogVisible"
      title="生成需求汇总"
      width="500px"
    >
      <el-form :model="summaryForm" label-width="120px">
        <el-form-item label="物料类别">
          <el-select v-model="summaryForm.materialCategory" placeholder="请选择" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期类型">
          <el-select v-model="summaryForm.periodType" placeholder="请选择" style="width: 100%">
            <el-option label="月度" :value="1" />
            <el-option label="季度" :value="2" />
            <el-option label="年度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="汇总名称">
          <el-input v-model="summaryForm.summaryName" placeholder="请输入汇总名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="summaryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmGenerateSummary" :loading="submitLoading">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import { 
  getPurchaseRequestList, 
  createPurchaseRequest, 
  updatePurchaseRequest, 
  deletePurchaseRequest,
  submitPurchaseRequest
} from '@/api/purchaseRequest'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const viewDialogVisible = ref(false)
const viewData = ref<any>({})

const summaryDialogVisible = ref(false)

const searchForm = reactive({
  reqNo: '',
  reqTitle: '',
  reqDept: '',
  status: null as number | null,
  urgency: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const form = reactive({
  id: null as number | null,
  reqNo: '',
  reqTitle: '',
  reqDept: '',
  reqPerson: '',
  reqPhone: '',
  requiredDate: '',
  deliveryAddress: '',
  urgency: 1,
  totalAmount: 0,
  budgetSource: '',
  description: '',
  status: 0,
  remark: '',
  items: [] as any[]
})

const summaryForm = reactive({
  materialCategory: null as number | null,
  periodType: 1,
  summaryName: ''
})

const rules: FormRules = {
  reqTitle: [{ required: true, message: '请输入申请单标题', trigger: 'blur' }],
  reqDept: [{ required: true, message: '请选择申请部门', trigger: 'change' }],
  reqPerson: [{ required: true, message: '请输入申请人', trigger: 'blur' }]
}

const statusMap: Record<number, string> = {
  0: '草稿',
  1: '待提交',
  2: '审批中',
  3: '审批通过',
  4: '审批拒绝',
  5: '已转订单',
  6: '已取消'
}

const urgencyMap: Record<number, string> = {
  1: '普通',
  2: '紧急',
  3: '特急'
}

const getStatusName = (status: number) => statusMap[status] || '未知'
const getUrgencyName = (urgency: number) => urgencyMap[urgency] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 3:
    case 5:
      return 'success'
    case 2:
      return 'warning'
    case 4:
    case 6:
      return 'danger'
    default:
      return 'info'
  }
}

const getUrgencyType = (urgency: number) => {
  switch (urgency) {
    case 3:
      return 'danger'
    case 2:
      return 'warning'
    default:
      return 'info'
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
    const res = await getPurchaseRequestList(params)
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
  searchForm.reqNo = ''
  searchForm.reqTitle = ''
  searchForm.reqDept = ''
  searchForm.status = null
  searchForm.urgency = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增采购申请'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑采购申请'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row: any) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该采购申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePurchaseRequest(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmit = (row: any) => {
  ElMessageBox.confirm('确定要提交该采购申请吗？提交后将进入审批流程。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await submitPurchaseRequest(row.id)
      ElMessage.success('提交成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData: any = {
          ...form,
          items: form.items.filter((item: any) => item.materialCode && item.materialName)
        }
        
        if (isEdit.value && form.id) {
          await updatePurchaseRequest(form.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createPurchaseRequest(submitData)
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

const resetForm = () => {
  form.id = null
  form.reqNo = ''
  form.reqTitle = ''
  form.reqDept = ''
  form.reqPerson = ''
  form.reqPhone = ''
  form.requiredDate = ''
  form.deliveryAddress = ''
  form.urgency = 1
  form.totalAmount = 0
  form.budgetSource = ''
  form.description = ''
  form.status = 0
  form.remark = ''
  form.items = []
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const handleAddItem = () => {
  form.items.push({
    materialCode: '',
    materialName: '',
    materialSpec: '',
    materialUnit: '',
    materialCategory: 1,
    quantity: 0,
    unitPrice: 0
  })
}

const handleRemoveItem = (index: number) => {
  form.items.splice(index, 1)
}

const handleGenerateSummary = () => {
  summaryForm.materialCategory = null
  summaryForm.periodType = 1
  summaryForm.summaryName = ''
  summaryDialogVisible.value = true
}

const handleConfirmGenerateSummary = async () => {
  submitLoading.value = true
  try {
    ElMessage.success('需求汇总生成请求已发送，请在需求汇总页面查看结果')
    summaryDialogVisible.value = false
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
.purchase-request-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
