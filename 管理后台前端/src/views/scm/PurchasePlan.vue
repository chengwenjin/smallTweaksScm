<template>
  <div class="purchase-plan-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="计划单编号">
          <el-input v-model="searchForm.planNo" placeholder="请输入计划单编号" clearable />
        </el-form-item>
        <el-form-item label="计划单名称">
          <el-input v-model="searchForm.planName" placeholder="请输入计划单名称" clearable />
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="searchForm.planType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="月度计划" :value="1" />
            <el-option label="季度计划" :value="2" />
            <el-option label="年度计划" :value="3" />
            <el-option label="紧急计划" :value="4" />
            <el-option label="补货计划" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="searchForm.sourceType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="需求汇总" value="1" />
            <el-option label="生产工单" value="2" />
            <el-option label="安全库存" value="3" />
            <el-option label="人工创建" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已执行" value="EXECUTED" />
            <el-option label="已取消" value="CANCELLED" />
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
          新建采购计划
        </el-button>
        <el-button type="success" @click="handleGenerateReplenishment">
          <el-icon><MagicStick /></el-icon>
          智能补货计划
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="500">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="planNo" label="计划单编号" width="150" />
        <el-table-column prop="planName" label="计划单名称" width="200" show-overflow-tooltip />
        <el-table-column prop="planType" label="计划类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlanTypeTagType(row.planType)">
              {{ getPlanTypeName(row.planType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源类型" width="100">
          <template #default="{ row }">
            {{ getSourceTypeName(row.sourceType) }}
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="物料种类" width="100" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template #default="{ row }">
            {{ row.estimatedAmount ? '¥' + parseFloat(row.estimatedAmount).toFixed(2) : '-' }}
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="row.status === 'PENDING'">删除</el-button>
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
      v-model="replenishmentDialogVisible"
      title="生成智能补货计划"
      width="600px"
    >
      <el-alert
        title="智能补货计划将根据以下条件生成："
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <ul>
            <li>1. 基于BOM计算生产工单所需物料</li>
            <li>2. 对比当前库存数量</li>
            <li>3. 计算安全库存缺口</li>
            <li>4. 自动生成补货建议</li>
          </ul>
        </template>
      </el-alert>
      <el-form :model="replenishmentForm" label-width="120px">
        <el-form-item label="计划名称">
          <el-input v-model="replenishmentForm.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="包含生产工单">
          <el-switch v-model="replenishmentForm.includeWorkOrders" />
          <span style="margin-left: 10px; color: #909399">根据生产工单的BOM计算需求</span>
        </el-form-item>
        <el-form-item label="包含安全库存">
          <el-switch v-model="replenishmentForm.includeSafetyStock" />
          <span style="margin-left: 10px; color: #909399">计算低于安全库存的物料缺口</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="replenishmentForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replenishmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmReplenishment" :loading="submitLoading">生成补货计划</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="查看采购计划"
      width="900px"
    >
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="计划单编号">{{ viewData.planNo }}</el-descriptions-item>
        <el-descriptions-item label="计划单名称">{{ viewData.planName }}</el-descriptions-item>
        <el-descriptions-item label="计划类型">
          <el-tag :type="getPlanTypeTagType(viewData.planType)">
            {{ getPlanTypeName(viewData.planType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源类型">{{ getSourceTypeName(viewData.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="物料种类数">{{ viewData.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ viewData.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="预估金额">
          {{ viewData.estimatedAmount ? '¥' + parseFloat(viewData.estimatedAmount).toFixed(2) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)">
            {{ getStatusName(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">计划明细</el-divider>
      
      <el-table :data="viewData.items || []" border stripe max-height="300">
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="materialSpec" label="规格" width="120" />
        <el-table-column prop="materialUnit" label="单位" width="80" />
        <el-table-column prop="requiredQuantity" label="需求数量" width="100" />
        <el-table-column prop="stockQuantity" label="现有库存" width="100" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column prop="shortageQuantity" label="缺货数量" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-red': parseFloat(row.shortageQuantity) > 0 }">
              {{ row.shortageQuantity || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="purchaseQuantity" label="建议采购量" width="100">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.purchaseQuantity || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="参考单价" width="100">
          <template #default="{ row }">
            {{ row.unitPrice ? '¥' + parseFloat(row.unitPrice).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template #default="{ row }">
            {{ row.estimatedAmount ? '¥' + parseFloat(row.estimatedAmount).toFixed(2) : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import { 
  getPurchasePlanList, 
  getPurchasePlanDetail,
  generateReplenishmentPlan,
  createPurchasePlan,
  deletePurchasePlan
} from '@/api/purchasePlan'

const loading = ref(false)
const submitLoading = ref(false)

const replenishmentDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const viewData = ref<any>({})

const searchForm = reactive({
  planNo: '',
  planName: '',
  planType: null as number | null,
  sourceType: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const replenishmentForm = reactive({
  planName: '',
  includeWorkOrders: true,
  includeSafetyStock: true,
  remark: ''
})

const planTypeMap: Record<number, string> = {
  1: '月度计划',
  2: '季度计划',
  3: '年度计划',
  4: '紧急计划',
  5: '补货计划'
}

const sourceTypeMap: Record<string, string> = {
  '1': '需求汇总',
  '2': '生产工单',
  '3': '安全库存',
  '4': '人工创建'
}

const statusMap: Record<string, string> = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  EXECUTED: '已执行',
  CANCELLED: '已取消'
}

const getPlanTypeName = (type: number) => planTypeMap[type] || '未知'
const getSourceTypeName = (type: string) => sourceTypeMap[type] || type || '未知'
const getStatusName = (status: string) => statusMap[status] || '未知'

const getPlanTypeTagType = (type: number) => {
  switch (type) {
    case 4:
    case 5:
      return 'danger'
    default:
      return 'primary'
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
    case 'EXECUTED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'warning'
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
    const res = await getPurchasePlanList(params)
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
  searchForm.planNo = ''
  searchForm.planName = ''
  searchForm.planType = null
  searchForm.sourceType = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  ElMessage.info('请使用\"生成智能补货计划\"功能或通过需求汇总生成采购计划')
}

const handleGenerateReplenishment = () => {
  replenishmentForm.planName = ''
  replenishmentForm.includeWorkOrders = true
  replenishmentForm.includeSafetyStock = true
  replenishmentForm.remark = ''
  replenishmentDialogVisible.value = true
}

const handleConfirmReplenishment = async () => {
  submitLoading.value = true
  try {
    const data: any = {
      includeWorkOrders: replenishmentForm.includeWorkOrders,
      includeSafetyStock: replenishmentForm.includeSafetyStock
    }
    if (replenishmentForm.planName) {
      data.planName = replenishmentForm.planName
    }
    if (replenishmentForm.remark) {
      data.remark = replenishmentForm.remark
    }
    
    await generateReplenishmentPlan(data)
    ElMessage.success('智能补货计划生成成功')
    replenishmentDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleView = async (row: any) => {
  try {
    const res = await getPurchasePlanDetail(row.id)
    viewData.value = res.data || {}
    viewDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该采购计划吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePurchasePlan(row.id)
      ElMessage.success('删除成功')
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
.purchase-plan-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.text-red {
  color: #f56c6c;
  font-weight: bold;
}
</style>
