<template>
  <div class="incoming-inspection">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="检验单号">
          <el-input v-model="searchForm.inspectionNo" placeholder="请输入检验单号" clearable />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入物料名称" clearable />
        </el-form-item>
        <el-form-item label="检验结果">
          <el-select v-model="searchForm.inspectionResult" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待检验" :value="0" />
            <el-option label="合格" :value="1" />
            <el-option label="让步接收" :value="2" />
            <el-option label="退货" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="检验状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待检验" :value="0" />
            <el-option label="检验中" :value="1" />
            <el-option label="已检验" :value="2" />
            <el-option label="已审批" :value="3" />
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
          新增检验
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="inspectionNo" label="检验单号" width="180" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="materialName" label="物料名称" width="150" show-overflow-tooltip />
        <el-table-column prop="materialSpec" label="规格" width="120" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批次号" width="120" />
        <el-table-column prop="inspectionQuantity" label="检验数量" width="100" />
        <el-table-column prop="qualifiedQuantity" label="合格数量" width="100" />
        <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="110" />
        <el-table-column prop="inspectionResult" label="检验结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.inspectionResult)">
              {{ getResultName(row.inspectionResult) }}
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
        <el-table-column prop="inspectorName" label="检验员" width="100" />
        <el-table-column prop="inspectionDate" label="检验日期" width="120" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleSubmit(row)" 
              :disabled="row.status === 2 || row.status === 3"
            >
              提交
            </el-button>
            <el-button 
              link 
              type="warning" 
              size="small" 
              @click="handleApprove(row)" 
              :disabled="row.status !== 2"
            >
              审批
            </el-button>
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleEdit(row)" 
              :disabled="row.status === 3"
            >
              编辑
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
      title="来料质检详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="检验单号">{{ currentInspection.inspectionNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentInspection.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentInspection.materialName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentInspection.materialSpec }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentInspection.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ currentInspection.productionDate }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentInspection.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentInspection.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="检验数量">{{ currentInspection.inspectionQuantity }}</el-descriptions-item>
        <el-descriptions-item label="抽样数量">{{ currentInspection.sampleQuantity }}</el-descriptions-item>
        <el-descriptions-item label="合格数量">{{ currentInspection.qualifiedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="不合格数量">{{ currentInspection.unqualifiedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="检验结果">
          <el-tag :type="getResultType(currentInspection.inspectionResult)">
            {{ getResultName(currentInspection.inspectionResult) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentInspection.status)">
            {{ getStatusName(currentInspection.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检验员">{{ currentInspection.inspectorName }}</el-descriptions-item>
        <el-descriptions-item label="检验日期">{{ currentInspection.inspectionDate }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentInspection.approverName }}</el-descriptions-item>
        <el-descriptions-item label="审批日期">{{ currentInspection.approvalDate }}</el-descriptions-item>
        <el-descriptions-item label="关联入库单">{{ currentInspection.receiptNo }}</el-descriptions-item>
        <el-descriptions-item label="是否关联入库">
          <el-tag v-if="currentInspection.linkedToReceipt" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检验结论" :span="2">{{ currentInspection.conclusion }}</el-descriptions-item>
        <el-descriptions-item label="不合格原因" :span="2">{{ currentInspection.unqualifiedReason }}</el-descriptions-item>
        <el-descriptions-item label="处理意见" :span="2">{{ currentInspection.handlingOpinion }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentInspection.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单编号" prop="orderId">
              <el-select 
                v-model="form.orderId" 
                placeholder="请选择订单" 
                style="width: 100%" 
                filterable
              >
                <el-option 
                  v-for="order in orderList" 
                  :key="order.id" 
                  :label="order.orderNo + ' - ' + order.orderName" 
                  :value="order.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生产日期" prop="productionDate">
              <el-date-picker
                v-model="form.productionDate"
                type="date"
                placeholder="请选择生产日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验日期" prop="inspectionDate">
              <el-date-picker
                v-model="form.inspectionDate"
                type="date"
                placeholder="请选择检验日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验数量" prop="inspectionQuantity">
              <el-input-number 
                v-model="form.inspectionQuantity" 
                :min="0" 
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抽样数量" prop="sampleQuantity">
              <el-input-number 
                v-model="form.sampleQuantity" 
                :min="0" 
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">检验结果</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合格数量" prop="qualifiedQuantity">
              <el-input-number 
                v-model="form.qualifiedQuantity" 
                :min="0" 
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="不合格数量" prop="unqualifiedQuantity">
              <el-input-number 
                v-model="form.unqualifiedQuantity" 
                :min="0" 
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验结果" prop="inspectionResult">
              <el-select v-model="form.inspectionResult" placeholder="请选择检验结果" style="width: 100%">
                <el-option label="合格" :value="1" />
                <el-option label="让步接收" :value="2" />
                <el-option label="退货" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验员" prop="inspectorName">
              <el-input v-model="form.inspectorName" placeholder="请输入检验员姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="检验结论" prop="conclusion">
          <el-input v-model="form.conclusion" type="textarea" :rows="2" placeholder="请输入检验结论" />
        </el-form-item>
        <el-form-item label="不合格原因" prop="unqualifiedReason">
          <el-input v-model="form.unqualifiedReason" type="textarea" :rows="2" placeholder="不合格原因（如需要）" />
        </el-form-item>
        <el-form-item label="处理意见" prop="handlingOpinion">
          <el-input v-model="form.handlingOpinion" type="textarea" :rows="2" placeholder="处理意见（如需要）" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="approveDialogVisible"
      title="审批来料质检"
      width="500px"
    >
      <el-form :model="approveForm" :rules="approveRules" ref="approveFormRef" label-width="100px">
        <el-form-item label="检验结果">
          <el-tag :type="getResultType(approveForm.inspectionResult)">
            {{ getResultName(approveForm.inspectionResult) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审批结果" prop="approvalResult">
          <el-radio-group v-model="approveForm.approvalResult">
            <el-radio :value="1">同意</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批人" prop="approverName">
          <el-input v-model="approveForm.approverName" placeholder="请输入审批人姓名" />
        </el-form-item>
        <el-form-item label="审批意见" prop="approvalOpinion">
          <el-input v-model="approveForm.approvalOpinion" type="textarea" :rows="2" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove" :loading="submitLoading">确认审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getInspectionList, getInspectionDetail, createInspection, submitInspection, approveInspection, updateInspection } from '@/api/incomingInspection'
import { getOrderList } from '@/api/orderManagement'

const loading = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const dialogVisible = ref(false)
const approveDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const approveFormRef = ref<FormInstance>()

const orderList = ref<any[]>([])

const searchForm = reactive({
  inspectionNo: '',
  orderNo: '',
  materialName: '',
  inspectionResult: null as number | null,
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentInspection = ref<any>({})

const form = reactive({
  id: null as number | null,
  orderId: null as number | null,
  batchNo: '',
  productionDate: '',
  inspectionDate: '',
  inspectionQuantity: 0,
  sampleQuantity: 0,
  qualifiedQuantity: 0,
  unqualifiedQuantity: 0,
  inspectionResult: null as number | null,
  inspectorName: '',
  conclusion: '',
  unqualifiedReason: '',
  handlingOpinion: '',
  remark: ''
})

const approveForm = reactive({
  id: null as number | null,
  inspectionResult: null as number | null,
  approvalResult: 1,
  approverName: '',
  approvalOpinion: ''
})

const rules: FormRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  inspectionQuantity: [{ required: true, message: '请输入检验数量', trigger: 'blur' }],
  inspectionResult: [{ required: true, message: '请选择检验结果', trigger: 'change' }],
  inspectorName: [{ required: true, message: '请输入检验员姓名', trigger: 'blur' }]
}

const approveRules: FormRules = {
  approverName: [{ required: true, message: '请输入审批人姓名', trigger: 'blur' }]
}

const resultMap: Record<number, string> = {
  0: '待检验',
  1: '合格',
  2: '让步接收',
  3: '退货'
}

const statusMap: Record<number, string> = {
  0: '待检验',
  1: '检验中',
  2: '已检验',
  3: '已审批'
}

const getResultName = (result: number) => resultMap[result] || '未知'
const getStatusName = (status: number) => statusMap[status] || '未知'

const getResultType = (result: number) => {
  switch (result) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'primary'
    case 2: return 'warning'
    case 3: return 'success'
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
    const res = await getInspectionList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadOrders = async () => {
  try {
    const res = await getOrderList({ pageNum: 1, pageSize: 100 })
    orderList.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.inspectionNo = ''
  searchForm.orderNo = ''
  searchForm.materialName = ''
  searchForm.inspectionResult = null
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增来料质检'
  isEdit.value = false
  Object.assign(form, {
    id: null,
    orderId: null,
    batchNo: '',
    productionDate: '',
    inspectionDate: new Date().toISOString().split('T')[0],
    inspectionQuantity: 0,
    sampleQuantity: 0,
    qualifiedQuantity: 0,
    unqualifiedQuantity: 0,
    inspectionResult: null,
    inspectorName: '',
    conclusion: '',
    unqualifiedReason: '',
    handlingOpinion: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleView = async (row: any) => {
  currentInspection.value = { ...row }
  viewDialogVisible.value = true
  await loadInspectionDetail(row.id)
}

const loadInspectionDetail = async (id: number) => {
  try {
    const res = await getInspectionDetail(id)
    currentInspection.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑来料质检'
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    orderId: row.orderId,
    batchNo: row.batchNo,
    productionDate: row.productionDate,
    inspectionDate: row.inspectionDate,
    inspectionQuantity: row.inspectionQuantity || 0,
    sampleQuantity: row.sampleQuantity || 0,
    qualifiedQuantity: row.qualifiedQuantity || 0,
    unqualifiedQuantity: row.unqualifiedQuantity || 0,
    inspectionResult: row.inspectionResult,
    inspectorName: row.inspectorName,
    conclusion: row.conclusion,
    unqualifiedReason: row.unqualifiedReason,
    handlingOpinion: row.handlingOpinion,
    remark: row.remark
  })
  dialogVisible.value = true
}

const handleSubmit = (row: any) => {
  ElMessageBox.confirm('确定要提交该检验单吗？提交后将进入审批流程。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await submitInspection(row.id)
      ElMessage.success('提交成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleApprove = (row: any) => {
  approveForm.id = row.id
  approveForm.inspectionResult = row.inspectionResult
  approveForm.approvalResult = 1
  approveForm.approverName = ''
  approveForm.approvalOpinion = ''
  approveDialogVisible.value = true
}

const submitApprove = async () => {
  if (!approveFormRef.value) return
  await approveFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await approveInspection(approveForm.id!, {
          approverName: approveForm.approverName,
          approvalResult: approveForm.approvalResult,
          approvalOpinion: approveForm.approvalOpinion
        })
        ElMessage.success('审批成功')
        approveDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSubmitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value && form.id) {
          await updateInspection(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await createInspection(form)
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
    orderId: null,
    batchNo: '',
    productionDate: '',
    inspectionDate: '',
    inspectionQuantity: 0,
    sampleQuantity: 0,
    qualifiedQuantity: 0,
    unqualifiedQuantity: 0,
    inspectionResult: null,
    inspectorName: '',
    conclusion: '',
    unqualifiedReason: '',
    handlingOpinion: '',
    remark: ''
  })
}

onMounted(() => {
  fetchData()
  loadOrders()
})
</script>

<style scoped>
.incoming-inspection {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
