<template>
  <div class="progress-tracking">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="生产进度" name="progress">
          <el-form :inline="true" :model="progressSearchForm" class="search-form">
            <el-form-item label="订单编号">
              <el-input v-model="progressSearchForm.orderNo" placeholder="请输入订单编号" clearable />
            </el-form-item>
            <el-form-item label="物料名称">
              <el-input v-model="progressSearchForm.materialName" placeholder="请输入物料名称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="progressSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="待开始" :value="0" />
                <el-option label="进行中" :value="1" />
                <el-option label="已完成" :value="2" />
                <el-option label="已暂停" :value="3" />
                <el-option label="已延误" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchProgressData">查询</el-button>
              <el-button @click="resetProgressSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="progressList" border stripe v-loading="progressLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="orderNo" label="订单编号" width="150" />
            <el-table-column prop="materialName" label="物料名称" width="150" show-overflow-tooltip />
            <el-table-column prop="materialSpec" label="规格" width="120" show-overflow-tooltip />
            <el-table-column prop="totalQuantity" label="总数量" width="100" />
            <el-table-column prop="completedQuantity" label="已完成数量" width="120" />
            <el-table-column prop="progressRate" label="进度" width="120">
              <template #default="{ row }">
                <el-progress 
                  :percentage="Number(row.progressRate || 0)" 
                  :stroke-width="18"
                  :status="row.progressRate >= 100 ? 'success' : row.progressRate > 0 ? '' : 'exception'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="plannedStartDate" label="计划开始" width="120" />
            <el-table-column prop="plannedEndDate" label="计划完成" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getProgressStatusType(row.status)">
                  {{ getProgressStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewProgress(row)">详情</el-button>
                <el-button link type="success" size="small" @click="updateProgress(row)" :disabled="row.status === 2">
                  更新
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="progressPagination.pageNum"
            v-model:page-size="progressPagination.pageSize"
            :total="progressPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchProgressData"
            @current-change="fetchProgressData"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>

        <el-tab-pane label="发货记录" name="shipment">
          <el-form :inline="true" :model="shipmentSearchForm" class="search-form">
            <el-form-item label="发货单号">
              <el-input v-model="shipmentSearchForm.shipmentNo" placeholder="请输入发货单号" clearable />
            </el-form-item>
            <el-form-item label="订单编号">
              <el-input v-model="shipmentSearchForm.orderNo" placeholder="请输入订单编号" clearable />
            </el-form-item>
            <el-form-item label="供应商">
              <el-input v-model="shipmentSearchForm.supplierName" placeholder="请输入供应商名称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="shipmentSearchForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="待发货" :value="0" />
                <el-option label="已发货" :value="1" />
                <el-option label="运输中" :value="2" />
                <el-option label="已送达" :value="3" />
                <el-option label="已签收" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchShipmentData">查询</el-button>
              <el-button @click="resetShipmentSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="shipmentList" border stripe v-loading="shipmentLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="shipmentNo" label="发货单号" width="150" />
            <el-table-column prop="orderNo" label="订单编号" width="150" />
            <el-table-column prop="supplierName" label="供应商" width="150" show-overflow-tooltip />
            <el-table-column prop="totalQuantity" label="发货数量" width="100" />
            <el-table-column prop="shipmentDate" label="发货日期" width="120" />
            <el-table-column prop="estimatedArrivalDate" label="预计到达" width="120" />
            <el-table-column prop="logisticsCompany" label="物流公司" width="120" show-overflow-tooltip />
            <el-table-column prop="trackingNo" label="物流单号" width="150" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getShipmentStatusType(row.status)">
                  {{ getShipmentStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewShipment(row)">详情</el-button>
                <el-button link type="success" size="small" @click="viewLogistics(row)">物流</el-button>
                <el-button link type="warning" size="small" @click="updateShipmentStatus(row)">
                  更新状态
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="shipmentPagination.pageNum"
            v-model:page-size="shipmentPagination.pageSize"
            :total="shipmentPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchShipmentData"
            @current-change="fetchShipmentData"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="progressDialogVisible"
      title="生产进度详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentProgress.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentProgress.materialName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentProgress.materialSpec }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getProgressStatusType(currentProgress.status)">
            {{ getProgressStatusName(currentProgress.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentProgress.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="已完成数量">{{ currentProgress.completedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="进度">
          <el-progress 
            :percentage="Number(currentProgress.progressRate || 0)" 
            :stroke-width="18"
            :status="currentProgress.progressRate >= 100 ? 'success' : currentProgress.progressRate > 0 ? '' : 'exception'"
          />
        </el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ currentProgress.plannedStartDate }}</el-descriptions-item>
        <el-descriptions-item label="计划完成">{{ currentProgress.plannedEndDate }}</el-descriptions-item>
        <el-descriptions-item label="实际开始">{{ currentProgress.actualStartDate }}</el-descriptions-item>
        <el-descriptions-item label="实际完成">{{ currentProgress.actualEndDate }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentProgress.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="updateProgressDialogVisible"
      title="更新生产进度"
      width="500px"
    >
      <el-form :model="updateProgressForm" label-width="120px">
        <el-form-item label="当前进度">
          <el-progress 
            :percentage="Number(updateProgressForm.progressRate || 0)" 
            :stroke-width="18"
          />
        </el-form-item>
        <el-form-item label="已完成数量">
          <el-input-number 
            v-model="updateProgressForm.completedQuantity" 
            :min="0" 
            :max="updateProgressForm.totalQuantity"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="updateProgressForm.status" style="width: 100%">
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已暂停" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="updateProgressForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateProgressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdateProgress" :loading="submitLoading">确认更新</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="shipmentDialogVisible"
      title="发货记录详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发货单号">{{ currentShipment.shipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentShipment.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentShipment.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="发货数量">{{ currentShipment.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="发货日期">{{ currentShipment.shipmentDate }}</el-descriptions-item>
        <el-descriptions-item label="预计到达">{{ currentShipment.estimatedArrivalDate }}</el-descriptions-item>
        <el-descriptions-item label="实际到达">{{ currentShipment.actualArrivalDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getShipmentStatusType(currentShipment.status)">
            {{ getShipmentStatusName(currentShipment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ currentShipment.logisticsCompany }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ currentShipment.trackingNo }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentShipment.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="logisticsDialogVisible"
      title="物流轨迹"
      width="700px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="(track, index) in logisticsTracks"
          :key="track.id"
          :timestamp="track.trackTime"
          :type="getTrackType(track.status, index)"
          :color="getTrackColor(track.status, index)"
          placement="top"
        >
          <el-card>
            <h4>{{ track.description }}</h4>
            <p>当前位置：{{ track.location }}</p>
            <p v-if="track.operator">操作人：{{ track.operator }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog
      v-model="updateShipmentStatusDialogVisible"
      title="更新发货状态"
      width="500px"
    >
      <el-form :model="updateShipmentStatusForm" label-width="120px">
        <el-form-item label="当前状态">
          <el-tag :type="getShipmentStatusType(updateShipmentStatusForm.status)">
            {{ getShipmentStatusName(updateShipmentStatusForm.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="updateShipmentStatusForm.newStatus" style="width: 100%">
            <el-option label="待发货" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="运输中" :value="2" />
            <el-option label="已送达" :value="3" />
            <el-option label="已签收" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前位置">
          <el-input v-model="updateShipmentStatusForm.location" placeholder="请输入当前位置" />
        </el-form-item>
        <el-form-item label="轨迹描述">
          <el-input v-model="updateShipmentStatusForm.description" type="textarea" :rows="2" placeholder="请输入轨迹描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateShipmentStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdateShipmentStatus" :loading="submitLoading">确认更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProgressList, getProgressDetail, updateProgress as updateProgressApi, updateProgressStatus, 
         getShipmentList, getShipmentDetail, getLogisticsTracks, updateShipmentStatus as updateShipmentStatusApi } from '@/api/progressTracking'

const activeTab = ref('progress')
const submitLoading = ref(false)

const progressLoading = ref(false)
const shipmentLoading = ref(false)

const progressDialogVisible = ref(false)
const updateProgressDialogVisible = ref(false)
const shipmentDialogVisible = ref(false)
const logisticsDialogVisible = ref(false)
const updateShipmentStatusDialogVisible = ref(false)

const progressSearchForm = reactive({
  orderNo: '',
  materialName: '',
  status: null as number | null
})

const progressPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const progressList = ref<any[]>([])
const currentProgress = ref<any>({})

const updateProgressForm = reactive({
  id: null as number | null,
  completedQuantity: 0,
  totalQuantity: 0,
  progressRate: 0,
  status: null as number | null,
  remark: ''
})

const shipmentSearchForm = reactive({
  shipmentNo: '',
  orderNo: '',
  supplierName: '',
  status: null as number | null
})

const shipmentPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const shipmentList = ref<any[]>([])
const currentShipment = ref<any>({})
const logisticsTracks = ref<any[]>([])

const updateShipmentStatusForm = reactive({
  id: null as number | null,
  status: null as number | null,
  newStatus: null as number | null,
  location: '',
  description: ''
})

const progressStatusMap: Record<number, string> = {
  0: '待开始',
  1: '进行中',
  2: '已完成',
  3: '已暂停',
  4: '已延误'
}

const shipmentStatusMap: Record<number, string> = {
  0: '待发货',
  1: '已发货',
  2: '运输中',
  3: '已送达',
  4: '已签收'
}

const getProgressStatusName = (status: number) => progressStatusMap[status] || '未知'
const getShipmentStatusName = (status: number) => shipmentStatusMap[status] || '未知'

const getProgressStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'warning'
    case 4: return 'danger'
    default: return 'info'
  }
}

const getShipmentStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'primary'
    case 2: return 'primary'
    case 3: return 'success'
    case 4: return 'success'
    default: return 'info'
  }
}

const getTrackType = (status: number, index: number) => {
  return index === 0 ? 'primary' : ''
}

const getTrackColor = (status: number, index: number) => {
  return index === 0 ? '#409EFF' : ''
}

const fetchProgressData = async () => {
  progressLoading.value = true
  try {
    const params = {
      ...progressSearchForm,
      pageNum: progressPagination.pageNum,
      pageSize: progressPagination.pageSize
    }
    const res = await getProgressList(params)
    progressList.value = res.data.records
    progressPagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    progressLoading.value = false
  }
}

const resetProgressSearch = () => {
  progressSearchForm.orderNo = ''
  progressSearchForm.materialName = ''
  progressSearchForm.status = null
  fetchProgressData()
}

const fetchShipmentData = async () => {
  shipmentLoading.value = true
  try {
    const params = {
      ...shipmentSearchForm,
      pageNum: shipmentPagination.pageNum,
      pageSize: shipmentPagination.pageSize
    }
    const res = await getShipmentList(params)
    shipmentList.value = res.data.records
    shipmentPagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    shipmentLoading.value = false
  }
}

const resetShipmentSearch = () => {
  shipmentSearchForm.shipmentNo = ''
  shipmentSearchForm.orderNo = ''
  shipmentSearchForm.supplierName = ''
  shipmentSearchForm.status = null
  fetchShipmentData()
}

const viewProgress = async (row: any) => {
  currentProgress.value = { ...row }
  progressDialogVisible.value = true
  await loadProgressDetail(row.id)
}

const loadProgressDetail = async (id: number) => {
  try {
    const res = await getProgressDetail(id)
    currentProgress.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const updateProgress = (row: any) => {
  currentProgress.value = { ...row }
  updateProgressForm.id = row.id
  updateProgressForm.completedQuantity = row.completedQuantity || 0
  updateProgressForm.totalQuantity = row.totalQuantity || 0
  updateProgressForm.progressRate = row.progressRate || 0
  updateProgressForm.status = row.status
  updateProgressForm.remark = ''
  updateProgressDialogVisible.value = true
}

const submitUpdateProgress = async () => {
  submitLoading.value = true
  try {
    await updateProgressApi(updateProgressForm.id!, {
      completedQuantity: updateProgressForm.completedQuantity,
      remark: updateProgressForm.remark
    })
    
    if (updateProgressForm.status !== null) {
      await updateProgressStatus(updateProgressForm.id!, updateProgressForm.status)
    }
    
    ElMessage.success('更新成功')
    updateProgressDialogVisible.value = false
    fetchProgressData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const viewShipment = async (row: any) => {
  currentShipment.value = { ...row }
  shipmentDialogVisible.value = true
  await loadShipmentDetail(row.id)
}

const loadShipmentDetail = async (id: number) => {
  try {
    const res = await getShipmentDetail(id)
    currentShipment.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const viewLogistics = async (row: any) => {
  currentShipment.value = { ...row }
  logisticsDialogVisible.value = true
  await loadLogisticsTracks(row.id)
}

const loadLogisticsTracks = async (shipmentId: number) => {
  try {
    const res = await getLogisticsTracks(shipmentId)
    logisticsTracks.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const updateShipmentStatus = (row: any) => {
  currentShipment.value = { ...row }
  updateShipmentStatusForm.id = row.id
  updateShipmentStatusForm.status = row.status
  updateShipmentStatusForm.newStatus = row.status
  updateShipmentStatusForm.location = ''
  updateShipmentStatusForm.description = ''
  updateShipmentStatusDialogVisible.value = true
}

const submitUpdateShipmentStatus = async () => {
  if (updateShipmentStatusForm.newStatus === null) {
    ElMessage.warning('请选择新状态')
    return
  }
  
  submitLoading.value = true
  try {
    await updateShipmentStatusApi(updateShipmentStatusForm.id!, {
      status: updateShipmentStatusForm.newStatus,
      location: updateShipmentStatusForm.location,
      description: updateShipmentStatusForm.description
    })
    ElMessage.success('更新成功')
    updateShipmentStatusDialogVisible.value = false
    fetchShipmentData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchProgressData()
  fetchShipmentData()
})
</script>

<style scoped>
.progress-tracking {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
