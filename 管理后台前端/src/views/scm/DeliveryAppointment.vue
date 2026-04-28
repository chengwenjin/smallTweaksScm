<template>
  <div class="delivery-appointment">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预约单号">
          <el-input v-model="searchForm.appointmentNo" placeholder="请输入预约单号" clearable />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseName" placeholder="请选择仓库" clearable style="width: 140px">
            <el-option label="A仓库-原材料区" value="A仓库-原材料区" />
            <el-option label="B仓库-半成品区" value="B仓库-半成品区" />
            <el-option label="C仓库-成品区" value="C仓库-成品区" />
            <el-option label="D仓库-备件区" value="D仓库-备件区" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已签到" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
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
          新增预约
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="appointmentNo" label="预约单号" width="150" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="supplierName" label="供应商" width="150" show-overflow-tooltip />
        <el-table-column prop="deliveryDate" label="送货日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="150" show-overflow-tooltip />
        <el-table-column prop="itemCount" label="物料种类" width="100" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="vehicleNo" label="车牌号" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleConfirm(row)" 
              :disabled="row.status !== 0"
            >
              确认
            </el-button>
            <el-button 
              link 
              type="info" 
              size="small" 
              @click="handleCheckIn(row)" 
              :disabled="row.status !== 1"
            >
              签到
            </el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleComplete(row)" 
              :disabled="row.status !== 2"
            >
              完成
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleCancel(row)" 
              :disabled="row.status === 3 || row.status === 4"
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
      title="送货预约详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预约单号">{{ currentAppointment.appointmentNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentAppointment.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentAppointment.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="送货日期">{{ currentAppointment.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="时间段">{{ currentAppointment.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentAppointment.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="物料种类">{{ currentAppointment.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentAppointment.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentAppointment.status)">
            {{ getStatusName(currentAppointment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentAppointment.vehicleNo }}</el-descriptions-item>
        <el-descriptions-item label="司机姓名">{{ currentAppointment.driverName }}</el-descriptions-item>
        <el-descriptions-item label="司机电话">{{ currentAppointment.driverPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentAppointment.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentAppointment.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="签到时间">{{ currentAppointment.checkInTime }}</el-descriptions-item>
        <el-descriptions-item label="签退时间">{{ currentAppointment.checkOutTime }}</el-descriptions-item>
        <el-descriptions-item label="仓库操作员">{{ currentAppointment.warehouseOperator }}</el-descriptions-item>
        <el-descriptions-item label="取消原因">{{ currentAppointment.cancelReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentAppointment.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="送货日期" prop="deliveryDate">
              <el-date-picker
                v-model="form.deliveryDate"
                type="date"
                placeholder="请选择送货日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时间段" prop="timeSlot">
              <el-select v-model="form.timeSlot" placeholder="请选择时间段" style="width: 100%">
                <el-option label="09:00-10:00" value="09:00-10:00" />
                <el-option label="10:00-11:00" value="10:00-11:00" />
                <el-option label="11:00-12:00" value="11:00-12:00" />
                <el-option label="13:00-14:00" value="13:00-14:00" />
                <el-option label="14:00-15:00" value="14:00-15:00" />
                <el-option label="15:00-16:00" value="15:00-16:00" />
                <el-option label="16:00-17:00" value="16:00-17:00" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="仓库" prop="warehouseName">
          <el-select v-model="form.warehouseName" placeholder="请选择仓库" style="width: 100%">
            <el-option label="A仓库-原材料区" value="A仓库-原材料区" />
            <el-option label="B仓库-半成品区" value="B仓库-半成品区" />
            <el-option label="C仓库-成品区" value="C仓库-成品区" />
            <el-option label="D仓库-备件区" value="D仓库-备件区" />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">送货信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="车牌号" prop="vehicleNo">
              <el-input v-model="form.vehicleNo" placeholder="请输入车牌号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="司机姓名" prop="driverName">
              <el-input v-model="form.driverName" placeholder="请输入司机姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="司机电话" prop="driverPhone">
              <el-input v-model="form.driverPhone" placeholder="请输入司机电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
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
      v-model="completeDialogVisible"
      title="完成送货预约"
      width="500px"
    >
      <el-form :model="completeForm" :rules="completeRules" ref="completeFormRef" label-width="100px">
        <el-form-item label="仓库操作员" prop="warehouseOperator">
          <el-input v-model="completeForm.warehouseOperator" placeholder="请输入仓库操作员姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete" :loading="submitLoading">确认完成</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cancelDialogVisible"
      title="取消送货预约"
      width="500px"
    >
      <el-form :model="cancelForm" :rules="cancelRules" ref="cancelFormRef" label-width="100px">
        <el-form-item label="取消原因" prop="cancelReason">
          <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitCancel" :loading="submitLoading">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAppointmentList, getAppointmentDetail, createAppointment, confirmAppointment, checkInAppointment, completeAppointment, cancelAppointment } from '@/api/deliveryAppointment'
import { getOrderList } from '@/api/orderManagement'

const loading = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const dialogVisible = ref(false)
const completeDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const completeFormRef = ref<FormInstance>()
const cancelFormRef = ref<FormInstance>()

const orderList = ref<any[]>([])

const searchForm = reactive({
  appointmentNo: '',
  orderNo: '',
  supplierName: '',
  warehouseName: '',
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentAppointment = ref<any>({})

const form = reactive({
  id: null as number | null,
  orderId: null as number | null,
  deliveryDate: '',
  timeSlot: '',
  warehouseName: '',
  vehicleNo: '',
  driverName: '',
  driverPhone: '',
  contactPerson: '',
  contactPhone: '',
  remark: ''
})

const completeForm = reactive({
  id: null as number | null,
  warehouseOperator: ''
})

const cancelForm = reactive({
  id: null as number | null,
  cancelReason: ''
})

const rules: FormRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  deliveryDate: [{ required: true, message: '请选择送货日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  warehouseName: [{ required: true, message: '请选择仓库', trigger: 'change' }]
}

const completeRules: FormRules = {
  warehouseOperator: [{ required: true, message: '请输入仓库操作员姓名', trigger: 'blur' }]
}

const cancelRules: FormRules = {
  cancelReason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

const statusMap: Record<number, string> = {
  0: '待确认',
  1: '已确认',
  2: '已签到',
  3: '已完成',
  4: '已取消'
}

const getStatusName = (status: number) => statusMap[status] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'info'
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
    const res = await getAppointmentList(params)
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
  searchForm.appointmentNo = ''
  searchForm.orderNo = ''
  searchForm.supplierName = ''
  searchForm.warehouseName = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增送货预约'
  isEdit.value = false
  Object.assign(form, {
    id: null,
    orderId: null,
    deliveryDate: '',
    timeSlot: '',
    warehouseName: '',
    vehicleNo: '',
    driverName: '',
    driverPhone: '',
    contactPerson: '',
    contactPhone: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleView = async (row: any) => {
  currentAppointment.value = { ...row }
  viewDialogVisible.value = true
  await loadAppointmentDetail(row.id)
}

const loadAppointmentDetail = async (id: number) => {
  try {
    const res = await getAppointmentDetail(id)
    currentAppointment.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = (row: any) => {
  ElMessageBox.confirm('确定要确认该送货预约吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await confirmAppointment(row.id)
      ElMessage.success('确认成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleCheckIn = (row: any) => {
  ElMessageBox.confirm('确定要签到吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await checkInAppointment(row.id)
      ElMessage.success('签到成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleComplete = (row: any) => {
  completeForm.id = row.id
  completeForm.warehouseOperator = ''
  completeDialogVisible.value = true
}

const submitComplete = async () => {
  if (!completeFormRef.value) return
  await completeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await completeAppointment(completeForm.id!, completeForm.warehouseOperator)
        ElMessage.success('完成成功')
        completeDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleCancel = (row: any) => {
  cancelForm.id = row.id
  cancelForm.cancelReason = ''
  cancelDialogVisible.value = true
}

const submitCancel = async () => {
  if (!cancelFormRef.value) return
  await cancelFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await cancelAppointment(cancelForm.id!, cancelForm.cancelReason)
        ElMessage.success('取消成功')
        cancelDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createAppointment(form)
        ElMessage.success('创建成功')
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
    deliveryDate: '',
    timeSlot: '',
    warehouseName: '',
    vehicleNo: '',
    driverName: '',
    driverPhone: '',
    contactPerson: '',
    contactPhone: '',
    remark: ''
  })
}

onMounted(() => {
  fetchData()
  loadOrders()
})
</script>

<style scoped>
.delivery-appointment {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
