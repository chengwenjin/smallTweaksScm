<template>
  <div class="requirement-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="需求单编号">
          <el-input v-model="searchForm.reqNo" placeholder="请输入需求单编号" clearable />
        </el-form-item>
        <el-form-item label="需求单名称">
          <el-input v-model="searchForm.reqName" placeholder="请输入需求单名称" clearable />
        </el-form-item>
        <el-form-item label="物资名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入物资名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待审批" :value="0" />
            <el-option label="审批通过" :value="1" />
            <el-option label="已询价" :value="2" />
            <el-option label="已比价" :value="3" />
            <el-option label="已采购" :value="4" />
            <el-option label="已完成" :value="5" />
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
          新增需求单
        </el-button>
        <el-button type="success" @click="handleBatchInquiry" :disabled="selectedIds.length === 0">
          <el-icon><Share /></el-icon>
          批量发起询价
        </el-button>
      </div>

      <el-table 
        :data="tableData" 
        border 
        stripe 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reqNo" label="需求单编号" width="150" />
        <el-table-column prop="reqName" label="需求单名称" width="180" show-overflow-tooltip />
        <el-table-column prop="materialName" label="物资名称" width="120" />
        <el-table-column prop="materialSpec" label="规格" width="120" show-overflow-tooltip />
        <el-table-column prop="materialUnit" label="单位" width="80" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="requiredDate" label="需求日期" width="120" />
        <el-table-column prop="urgency" label="紧急程度" width="90">
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
        <el-table-column prop="reqDept" label="需求部门" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" size="small" @click="handleInquiry(row)" :disabled="row.status !== 1">
              发起询价
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="需求单名称" prop="reqName">
          <el-input v-model="form.reqName" placeholder="请输入需求单名称" />
        </el-form-item>
        <el-form-item label="物资名称" prop="materialName">
          <el-input v-model="form.materialName" placeholder="请输入物资名称" />
        </el-form-item>
        <el-form-item label="物资规格" prop="materialSpec">
          <el-input v-model="form.materialSpec" placeholder="请输入物资规格" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物资单位" prop="materialUnit">
              <el-input v-model="form.materialUnit" placeholder="如：件、台、套" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需求数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" :precision="2" style="width: 100%" />
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
        <el-form-item label="预算范围" prop="budgetRange">
          <el-input v-model="form.budgetRange" placeholder="请输入预算范围，如：10000-20000元" />
        </el-form-item>
        <el-form-item label="需求描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入需求描述" />
        </el-form-item>
        <el-divider content-position="left">需求人信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="需求部门" prop="reqDept">
              <el-input v-model="form.reqDept" placeholder="请输入需求部门" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="需求人" prop="reqPerson">
              <el-input v-model="form.reqPerson" placeholder="请输入需求人" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="reqPhone">
              <el-input v-model="form.reqPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="期望交货日期" prop="deliveryDate">
          <el-date-picker
            v-model="form.deliveryDate"
            type="date"
            placeholder="请选择期望交货日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="交货地址" prop="deliveryAddress">
          <el-input v-model="form.deliveryAddress" type="textarea" :rows="2" placeholder="请输入交货地址" />
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
      v-model="inquiryDialogVisible"
      title="发起询价"
      width="600px"
    >
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="需求单编号">{{ selectedRequirement.reqNo }}</el-descriptions-item>
        <el-descriptions-item label="需求单名称">{{ selectedRequirement.reqName }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ selectedRequirement.materialName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ selectedRequirement.quantity }} {{ selectedRequirement.materialUnit }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="inquiryForm" :rules="inquiryRules" ref="inquiryFormRef" label-width="120px">
        <el-form-item label="询价单名称" prop="inquiryName">
          <el-input v-model="inquiryForm.inquiryName" placeholder="请输入询价单名称" />
        </el-form-item>
        <el-form-item label="报价截止日期" prop="deadline">
          <el-date-picker
            v-model="inquiryForm.deadline"
            type="date"
            placeholder="请选择报价截止日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="inquiryForm.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="inquiryForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="inquiryForm.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="询价说明" prop="description">
          <el-input v-model="inquiryForm.description" type="textarea" :rows="3" placeholder="请输入询价说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inquiryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitInquiry" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Share } from '@element-plus/icons-vue'
import { getRequirementList, createRequirement, updateRequirement, deleteRequirement } from '@/api/requirement'
import { createInquiry } from '@/api/inquiry'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const inquiryDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const inquiryFormRef = ref<FormInstance>()

const selectedIds = ref<number[]>([])
const selectedRequirement = ref<any>({})

const searchForm = reactive({
  reqNo: '',
  reqName: '',
  materialName: '',
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
  reqName: '',
  materialName: '',
  materialSpec: '',
  materialUnit: '',
  quantity: 1,
  requiredDate: '',
  urgency: 1,
  budgetRange: '',
  description: '',
  reqDept: '',
  reqPerson: '',
  reqPhone: '',
  deliveryDate: '',
  deliveryAddress: '',
  remark: ''
})

const inquiryForm = reactive({
  inquiryName: '',
  deadline: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  description: '',
  reqIds: [] as number[]
})

const rules: FormRules = {
  reqName: [{ required: true, message: '请输入需求单名称', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  materialUnit: [{ required: true, message: '请输入物资单位', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入需求数量', trigger: 'blur' }]
}

const inquiryRules: FormRules = {
  inquiryName: [{ required: true, message: '请输入询价单名称', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择报价截止日期', trigger: 'change' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const urgencyMap: Record<number, string> = {
  1: '普通',
  2: '紧急',
  3: '特急'
}

const statusMap: Record<number, string> = {
  0: '待审批',
  1: '审批通过',
  2: '已询价',
  3: '已比价',
  4: '已采购',
  5: '已完成',
  6: '已取消'
}

const getUrgencyName = (urgency: number) => urgencyMap[urgency] || '未知'
const getStatusName = (status: number) => statusMap[status] || '未知'

const getUrgencyType = (urgency: number) => {
  switch (urgency) {
    case 3: return 'danger'
    case 2: return 'warning'
    default: return 'info'
  }
}

const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'success'
    case 5: return 'success'
    case 6: return 'info'
    default: return 'warning'
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
    const res = await getRequirementList(params)
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
  searchForm.reqNo = ''
  searchForm.reqName = ''
  searchForm.materialName = ''
  searchForm.status = null
  searchForm.urgency = null
  handleSearch()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.filter(item => item.status === 1).map(item => item.id)
}

const handleAdd = () => {
  dialogTitle.value = '新增采购需求单'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑采购需求单'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该采购需求单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRequirement(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateRequirement(form.id!, form)
          ElMessage.success('更新成功')
        } else {
          await createRequirement(form)
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
  Object.assign(form, {
    id: null,
    reqName: '',
    materialName: '',
    materialSpec: '',
    materialUnit: '',
    quantity: 1,
    requiredDate: '',
    urgency: 1,
    budgetRange: '',
    description: '',
    reqDept: '',
    reqPerson: '',
    reqPhone: '',
    deliveryDate: '',
    deliveryAddress: '',
    remark: ''
  })
}

const handleInquiry = (row: any) => {
  selectedRequirement.value = { ...row }
  inquiryForm.inquiryName = '询价单 - ' + row.reqName
  inquiryForm.reqIds = [row.id]
  inquiryDialogVisible.value = true
}

const handleBatchInquiry = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择已审批通过的需求单')
    return
  }
  inquiryForm.inquiryName = '批量询价单 - ' + selectedIds.value.length + '条需求'
  inquiryForm.reqIds = [...selectedIds.value]
  inquiryDialogVisible.value = true
}

const handleSubmitInquiry = async () => {
  if (!inquiryFormRef.value) return
  
  await inquiryFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = {
          ...inquiryForm,
          reqIds: inquiryForm.reqIds.join(',')
        }
        await createInquiry(data)
        ElMessage.success('询价单创建成功')
        inquiryDialogVisible.value = false
        fetchData()
      } catch (error: any) {
        ElMessage.error(error.msg || '创建失败')
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
.requirement-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
