<template>
  <div class="demand-summary-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="汇总单编号">
          <el-input v-model="searchForm.summaryNo" placeholder="请输入汇总单编号" clearable />
        </el-form-item>
        <el-form-item label="汇总单名称">
          <el-input v-model="searchForm.summaryName" placeholder="请输入汇总单名称" clearable />
        </el-form-item>
        <el-form-item label="物料类别">
          <el-select v-model="searchForm.materialCategory" placeholder="请选择" clearable style="width: 120px">
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期类型">
          <el-select v-model="searchForm.periodType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="月度" :value="1" />
            <el-option label="季度" :value="2" />
            <el-option label="年度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="handleGenerateSummary">
          <el-icon><Plus /></el-icon>
          生成需求汇总
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="500">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="summaryNo" label="汇总单编号" width="150" />
        <el-table-column prop="summaryName" label="汇总单名称" width="200" show-overflow-tooltip />
        <el-table-column prop="materialCategory" label="物料类别" width="100">
          <template #default="{ row }">
            {{ getMaterialCategoryName(row.materialCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="periodType" label="周期类型" width="80">
          <template #default="{ row }">
            {{ getPeriodTypeName(row.periodType) }}
          </template>
        </el-table-column>
        <el-table-column prop="requestCount" label="申请单数" width="100" />
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button link type="success" size="small" @click="handleConfirm(row)" v-if="row.status === 'PENDING'">确认</el-button>
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
      v-model="dialogVisible"
      title="生成需求汇总"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="物料类别" prop="materialCategory">
          <el-select v-model="form.materialCategory" placeholder="请选择" style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期类型" prop="periodType">
          <el-select v-model="form.periodType" placeholder="请选择" style="width: 100%">
            <el-option label="月度" :value="1" />
            <el-option label="季度" :value="2" />
            <el-option label="年度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="汇总名称" prop="summaryName">
          <el-input v-model="form.summaryName" placeholder="请输入汇总名称" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="请选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitGenerate" :loading="submitLoading">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="查看需求汇总"
      width="900px"
    >
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="汇总单编号">{{ viewData.summaryNo }}</el-descriptions-item>
        <el-descriptions-item label="汇总单名称">{{ viewData.summaryName }}</el-descriptions-item>
        <el-descriptions-item label="物料类别">{{ getMaterialCategoryName(viewData.materialCategory) }}</el-descriptions-item>
        <el-descriptions-item label="周期类型">{{ getPeriodTypeName(viewData.periodType) }}</el-descriptions-item>
        <el-descriptions-item label="关联申请单数">{{ viewData.requestCount }}</el-descriptions-item>
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
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">汇总明细</el-divider>
      
      <el-table :data="viewData.items || []" border stripe max-height="300">
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="materialSpec" label="规格" width="120" />
        <el-table-column prop="materialUnit" label="单位" width="80" />
        <el-table-column prop="materialCategory" label="类别" width="80">
          <template #default="{ row }">
            {{ getMaterialCategoryName(row.materialCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceRequestCount" label="来源申请数" width="100" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="avgUnitPrice" label="平均单价" width="100">
          <template #default="{ row }">
            {{ row.avgUnitPrice ? '¥' + parseFloat(row.avgUnitPrice).toFixed(2) : '-' }}
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getDemandSummaryList, 
  getDemandSummaryDetail,
  generateDemandSummary,
  confirmDemandSummary,
  deleteDemandSummary
} from '@/api/demandSummary'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const viewDialogVisible = ref(false)
const viewData = ref<any>({})

const searchForm = reactive({
  summaryNo: '',
  summaryName: '',
  materialCategory: null as number | null,
  periodType: null as number | null,
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const form = reactive({
  materialCategory: null as number | null,
  periodType: 1,
  summaryName: '',
  startDate: '',
  endDate: '',
  remark: ''
})

const rules: FormRules = {
  summaryName: [{ required: true, message: '请输入汇总名称', trigger: 'blur' }],
  periodType: [{ required: true, message: '请选择周期类型', trigger: 'change' }]
}

const materialCategoryMap: Record<number, string> = {
  1: '原材料',
  2: '辅料',
  3: '设备'
}

const periodTypeMap: Record<number, string> = {
  1: '月度',
  2: '季度',
  3: '年度'
}

const statusMap: Record<string, string> = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  CANCELLED: '已取消'
}

const getMaterialCategoryName = (category: any) => {
  if (typeof category === 'string') {
    return category
  }
  return materialCategoryMap[category] || category || '未知'
}

const getPeriodTypeName = (type: number) => periodTypeMap[type] || '未知'
const getStatusName = (status: string) => statusMap[status] || '未知'

const getStatusType = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
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
    const res = await getDemandSummaryList(params)
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
  searchForm.summaryNo = ''
  searchForm.summaryName = ''
  searchForm.materialCategory = null
  searchForm.periodType = null
  searchForm.status = ''
  handleSearch()
}

const handleGenerateSummary = () => {
  resetForm()
  dialogVisible.value = true
}

const handleView = async (row: any) => {
  try {
    const res = await getDemandSummaryDetail(row.id)
    viewData.value = res.data || {}
    viewDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = (row: any) => {
  ElMessageBox.confirm('确定要确认该需求汇总吗？确认后将不可再修改。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await confirmDemandSummary(row.id)
      ElMessage.success('确认成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该需求汇总吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDemandSummary(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmitGenerate = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData: any = {
          summaryName: form.summaryName,
          periodType: form.periodType
        }
        if (form.materialCategory) {
          submitData.materialCategory = form.materialCategory
        }
        if (form.startDate) {
          submitData.startDate = form.startDate
        }
        if (form.endDate) {
          submitData.endDate = form.endDate
        }
        if (form.remark) {
          submitData.remark = form.remark
        }
        
        await generateDemandSummary(submitData)
        ElMessage.success('需求汇总生成成功')
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
  form.materialCategory = null
  form.periodType = 1
  form.summaryName = ''
  form.startDate = ''
  form.endDate = ''
  form.remark = ''
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.demand-summary-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
