<template>
  <div class="order-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="订单名称">
          <el-input v-model="searchForm.orderName" placeholder="请输入订单名称" clearable />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="新建" :value="0" />
            <el-option label="待审批" :value="1" />
            <el-option label="审批通过" :value="2" />
            <el-option label="已发布" :value="3" />
            <el-option label="待确认" :value="4" />
            <el-option label="已确认" :value="5" />
            <el-option label="生产中" :value="6" />
            <el-option label="发货中" :value="7" />
            <el-option label="部分收货" :value="8" />
            <el-option label="全部收货" :value="9" />
            <el-option label="已完成" :value="10" />
            <el-option label="已取消" :value="11" />
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
          新增订单
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="orderName" label="订单名称" width="180" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="供应商" width="150" show-overflow-tooltip />
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            <span v-if="row.totalAmount">¥{{ row.totalAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="expectedDeliveryDate" label="期望交货日期" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="400" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleIssue(row)" 
              :disabled="row.status !== 0"
            >
              下发
            </el-button>
            <el-button 
              link 
              type="warning" 
              size="small" 
              @click="handleChange(row)" 
              :disabled="row.status < 4 || row.status === 11"
            >
              变更
            </el-button>
            <el-button 
              link 
              type="info" 
              size="small" 
              @click="handleConfirm(row)" 
              :disabled="row.status !== 4"
            >
              确认
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleCancel(row)" 
              :disabled="row.status === 11 || row.status >= 8"
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
      title="订单详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单名称">{{ currentOrder.orderName }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentOrder.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ getOrderTypeName(currentOrder.orderType) }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentOrder.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span v-if="currentOrder.totalAmount">¥{{ currentOrder.totalAmount }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单日期">{{ currentOrder.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="期望交货日期">{{ currentOrder.expectedDeliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusName(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预计交货期">
          <span v-if="currentOrder.supplierConfirmDate">{{ currentOrder.supplierConfirmDate }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="付款条款" :span="2">{{ currentOrder.paymentTerms }}</el-descriptions-item>
        <el-descriptions-item label="交货地址" :span="2">{{ currentOrder.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentOrder.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">订单明细</el-divider>
      
      <el-table :data="currentOrder.items || []" border stripe>
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="materialSpec" label="规格" width="150" show-overflow-tooltip />
        <el-table-column prop="materialUnit" label="单位" width="80" />
        <el-table-column prop="quantity" label="订单数量" width="100" />
        <el-table-column prop="receivedQuantity" label="已收货数量" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100">
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
      </el-table>

      <el-divider content-position="left">变更记录</el-divider>
      
      <el-table :data="changeList" border stripe v-loading="changeLoading">
        <el-table-column prop="changeNo" label="变更单号" width="150" />
        <el-table-column prop="changeType" label="变更类型" width="120">
          <template #default="{ row }">
            {{ getChangeTypeName(row.changeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="changeReason" label="变更原因" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getChangeStatusType(row.status)">
              {{ getChangeStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="applyBy" label="申请人" width="100" />
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="issueDialogVisible"
      title="下发订单"
      width="500px"
    >
      <el-form :model="issueForm" label-width="100px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单名称">{{ currentOrder.orderName }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ currentOrder.supplierName }}</el-descriptions-item>
        </el-descriptions>
        <el-alert type="warning" title="确认下发该订单给供应商？" style="margin-top: 20px" />
      </el-form>
      <template #footer>
        <el-button @click="issueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitIssue" :loading="submitLoading">确认下发</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="changeDialogVisible"
      title="订单变更"
      width="600px"
    >
      <el-form :model="changeForm" :rules="changeRules" ref="changeFormRef" label-width="100px">
        <el-form-item label="变更类型" prop="changeType">
          <el-select v-model="changeForm.changeType" placeholder="请选择变更类型" style="width: 100%">
            <el-option label="数量变更" :value="1" />
            <el-option label="价格变更" :value="2" />
            <el-option label="交货日期变更" :value="3" />
            <el-option label="取消订单" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因" prop="changeReason">
          <el-input v-model="changeForm.changeReason" type="textarea" :rows="3" placeholder="请输入变更原因" />
        </el-form-item>
        <el-form-item label="变更内容" prop="changeContent">
          <el-input v-model="changeForm.changeContent" type="textarea" :rows="3" placeholder="请输入变更内容详情" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChange" :loading="submitLoading">提交变更</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="confirmDialogVisible"
      title="供应商确认接单"
      width="600px"
    >
      <el-form :model="confirmForm" :rules="confirmRules" ref="confirmFormRef" label-width="120px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单名称">{{ currentOrder.orderName }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ currentOrder.supplierName }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <el-form-item label="预计交货日期" prop="supplierConfirmDate">
          <el-date-picker
            v-model="confirmForm.supplierConfirmDate"
            type="date"
            placeholder="请选择预计交货日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="确认备注" prop="confirmRemark">
          <el-input v-model="confirmForm.confirmRemark" type="textarea" :rows="2" placeholder="请输入确认备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirm" :loading="submitLoading">确认接单</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cancelDialogVisible"
      title="取消订单"
      width="500px"
    >
      <el-form :model="cancelForm" :rules="cancelRules" ref="cancelFormRef" label-width="100px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单名称">{{ currentOrder.orderName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusName(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider />
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
import { getOrderList, getOrderDetail, getOrderChanges, issueOrder, changeOrder, confirmOrder, cancelOrder } from '@/api/orderManagement'

const loading = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const issueDialogVisible = ref(false)
const changeDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const changeLoading = ref(false)
const changeFormRef = ref<FormInstance>()
const confirmFormRef = ref<FormInstance>()
const cancelFormRef = ref<FormInstance>()

const searchForm = reactive({
  orderNo: '',
  orderName: '',
  supplierName: '',
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentOrder = ref<any>({})
const changeList = ref<any[]>([])

const issueForm = reactive({
  remark: ''
})

const changeForm = reactive({
  orderId: null as number | null,
  changeType: null as number | null,
  changeReason: '',
  changeContent: ''
})

const confirmForm = reactive({
  orderId: null as number | null,
  supplierId: null as number | null,
  supplierConfirmDate: '',
  confirmRemark: ''
})

const cancelForm = reactive({
  orderId: null as number | null,
  cancelReason: ''
})

const changeRules: FormRules = {
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  changeReason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
}

const confirmRules: FormRules = {
  supplierConfirmDate: [{ required: true, message: '请选择预计交货日期', trigger: 'change' }]
}

const cancelRules: FormRules = {
  cancelReason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

const statusMap: Record<number, string> = {
  0: '新建',
  1: '待审批',
  2: '审批通过',
  3: '已发布',
  4: '待确认',
  5: '已确认',
  6: '生产中',
  7: '发货中',
  8: '部分收货',
  9: '全部收货',
  10: '已完成',
  11: '已取消'
}

const orderTypeMap: Record<number, string> = {
  1: '标准订单',
  2: '紧急订单',
  3: '补货订单'
}

const changeTypeMap: Record<number, string> = {
  1: '数量变更',
  2: '价格变更',
  3: '交货日期变更',
  4: '取消订单',
  5: '其他'
}

const changeStatusMap: Record<number, string> = {
  0: '待提交',
  1: '待审批',
  2: '审批通过',
  3: '审批拒绝',
  4: '已取消'
}

const getStatusName = (status: number) => statusMap[status] || '未知'
const getOrderTypeName = (type: number) => orderTypeMap[type] || '未知'
const getChangeTypeName = (type: number) => changeTypeMap[type] || '未知'
const getChangeStatusName = (status: number) => changeStatusMap[status] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'warning'
    case 2: return 'success'
    case 3: return 'success'
    case 4: return 'warning'
    case 5: return 'success'
    case 6: return 'primary'
    case 7: return 'primary'
    case 8: return 'warning'
    case 9: return 'success'
    case 10: return 'success'
    case 11: return 'info'
    default: return 'info'
  }
}

const getChangeStatusType = (status: number) => {
  switch (status) {
    case 2: return 'success'
    case 3: return 'danger'
    case 4: return 'info'
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
    const res = await getOrderList(params)
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
  searchForm.orderNo = ''
  searchForm.orderName = ''
  searchForm.supplierName = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  ElMessage.warning('请从采购申请或采购计划中创建订单')
}

const handleView = async (row: any) => {
  currentOrder.value = { ...row }
  viewDialogVisible.value = true
  await loadOrderDetail(row.id)
  await loadChanges(row.id)
}

const loadOrderDetail = async (orderId: number) => {
  try {
    const res = await getOrderDetail(orderId)
    currentOrder.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadChanges = async (orderId: number) => {
  changeLoading.value = true
  try {
    const res = await getOrderChanges(orderId)
    changeList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    changeLoading.value = false
  }
}

const handleIssue = (row: any) => {
  currentOrder.value = { ...row }
  issueDialogVisible.value = true
}

const submitIssue = async () => {
  submitLoading.value = true
  try {
    await issueOrder(currentOrder.value.id)
    ElMessage.success('下发成功')
    issueDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleChange = (row: any) => {
  currentOrder.value = { ...row }
  changeForm.orderId = row.id
  changeForm.changeType = null
  changeForm.changeReason = ''
  changeForm.changeContent = ''
  changeDialogVisible.value = true
}

const submitChange = async () => {
  if (!changeFormRef.value) return
  await changeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await changeOrder({
          orderId: changeForm.orderId,
          changeType: changeForm.changeType,
          changeReason: changeForm.changeReason,
          changeContent: changeForm.changeContent
        })
        ElMessage.success('变更申请提交成功')
        changeDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleConfirm = (row: any) => {
  currentOrder.value = { ...row }
  confirmForm.orderId = row.id
  confirmForm.supplierId = row.supplierId
  confirmForm.supplierConfirmDate = ''
  confirmForm.confirmRemark = ''
  confirmDialogVisible.value = true
}

const submitConfirm = async () => {
  if (!confirmFormRef.value) return
  await confirmFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await confirmOrder({
          orderId: confirmForm.orderId,
          supplierId: confirmForm.supplierId,
          supplierConfirmDate: confirmForm.supplierConfirmDate,
          confirmRemark: confirmForm.confirmRemark
        })
        ElMessage.success('确认接单成功')
        confirmDialogVisible.value = false
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
  currentOrder.value = { ...row }
  cancelForm.orderId = row.id
  cancelForm.cancelReason = ''
  cancelDialogVisible.value = true
}

const submitCancel = async () => {
  if (!cancelFormRef.value) return
  await cancelFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await cancelOrder({
          orderId: cancelForm.orderId,
          cancelReason: cancelForm.cancelReason
        })
        ElMessage.success('订单取消成功')
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
