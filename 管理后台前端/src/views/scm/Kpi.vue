<template>
  <div class="kpi-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="周期类型">
          <el-select v-model="searchForm.periodType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="月度" :value="1" />
            <el-option label="季度" :value="2" />
            <el-option label="年度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="searchForm.year" placeholder="请选择" clearable style="width: 120px">
            <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.grade" placeholder="请选择" clearable style="width: 120px">
            <el-option label="A级" :value="1" />
            <el-option label="AA级" :value="2" />
            <el-option label="AAA级" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="handleCalculate">
          <el-icon><Cpu /></el-icon>
          计算KPI
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierName" label="供应商名称" width="180" />
        <el-table-column prop="periodTypeName" label="周期类型" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ row.periodTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="quarter" label="季度" width="80">
          <template #default="{ row }">
            <span v-if="row.quarter">Q{{ row.quarter }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="month" label="月份" width="80">
          <template #default="{ row }">
            <span v-if="row.month">{{ row.month }}月</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryOnTimeRate" label="交付准时率" width="110">
          <template #default="{ row }">
            <el-progress 
              :percentage="Number(row.deliveryOnTimeRate)" 
              :stroke-width="18"
              :color="getProgressColor(row.deliveryOnTimeRate)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="qualityPassRate" label="质检合格率" width="110">
          <template #default="{ row }">
            <el-progress 
              :percentage="Number(row.qualityPassRate)" 
              :stroke-width="18"
              :color="getProgressColor(row.qualityPassRate)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="priceCompetitiveness" label="价格竞争力" width="110">
          <template #default="{ row }">
            <el-progress 
              :percentage="Number(row.priceCompetitiveness)" 
              :stroke-width="18"
              :color="getProgressColor(row.priceCompetitiveness)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="serviceResponseSpeed" label="服务响应" width="110">
          <template #default="{ row }">
            <el-progress 
              :percentage="Number(row.serviceResponseSpeed)" 
              :stroke-width="18"
              :color="getProgressColor(row.serviceResponseSpeed)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="综合评分" width="100">
          <template #default="{ row }">
            <span :style="{ color: getScoreColor(row.totalScore), fontWeight: 'bold' }">
              {{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="gradeName" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getGradeType(row.grade)">
              {{ row.gradeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
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
      title="KPI详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商名称">{{ currentKpi.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="周期类型">{{ currentKpi.periodTypeName }}</el-descriptions-item>
        <el-descriptions-item label="年份">{{ currentKpi.year }}年</el-descriptions-item>
        <el-descriptions-item label="季度/月份">
          <span v-if="currentKpi.quarter">第{{ currentKpi.quarter }}季度</span>
          <span v-else-if="currentKpi.month">{{ currentKpi.month }}月</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="综合评分">
          <span :style="{ color: getScoreColor(currentKpi.totalScore), fontWeight: 'bold', fontSize: '18px' }">
            {{ currentKpi.totalScore }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="getGradeType(currentKpi.grade)" size="large">
            {{ currentKpi.gradeName }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">KPI指标明细</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-header">
              <span class="kpi-title">交付准时率</span>
              <span class="kpi-value">{{ currentKpi.deliveryOnTimeRate }}%</span>
            </div>
            <el-progress 
              :percentage="Number(currentKpi.deliveryOnTimeRate)" 
              :stroke-width="20"
              :color="getProgressColor(currentKpi.deliveryOnTimeRate)"
            />
            <div class="kpi-detail">
              <span>总交付次数: {{ currentKpi.deliveryTotalCount || 0 }}</span>
              <span>准时次数: {{ currentKpi.deliveryOnTimeCount || 0 }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-header">
              <span class="kpi-title">来料质检合格率</span>
              <span class="kpi-value">{{ currentKpi.qualityPassRate }}%</span>
            </div>
            <el-progress 
              :percentage="Number(currentKpi.qualityPassRate)" 
              :stroke-width="20"
              :color="getProgressColor(currentKpi.qualityPassRate)"
            />
            <div class="kpi-detail">
              <span>总质检次数: {{ currentKpi.qualityTotalCount || 0 }}</span>
              <span>合格次数: {{ currentKpi.qualityPassCount || 0 }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-header">
              <span class="kpi-title">价格竞争力</span>
              <span class="kpi-value">{{ currentKpi.priceCompetitiveness }}%</span>
            </div>
            <el-progress 
              :percentage="Number(currentKpi.priceCompetitiveness)" 
              :stroke-width="20"
              :color="getProgressColor(currentKpi.priceCompetitiveness)"
            />
            <div class="kpi-detail">
              <span>价格对比次数: {{ currentKpi.priceCompareCount || 0 }}</span>
              <span>最优次数: {{ currentKpi.priceBestCount || 0 }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-header">
              <span class="kpi-title">售后服务响应速度</span>
              <span class="kpi-value">{{ currentKpi.serviceResponseSpeed }}%</span>
            </div>
            <el-progress 
              :percentage="Number(currentKpi.serviceResponseSpeed)" 
              :stroke-width="20"
              :color="getProgressColor(currentKpi.serviceResponseSpeed)"
            />
            <div class="kpi-detail">
              <span>服务总次数: {{ currentKpi.serviceTotalCount || 0 }}</span>
              <span>响应及时次数: {{ currentKpi.serviceResponseOnTimeCount || 0 }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-divider content-position="left">计算规则说明</el-divider>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="综合评分计算">
          交付准时率(30%) + 来料质检合格率(30%) + 价格竞争力(20%) + 售后服务响应速度(20%)
        </el-descriptions-item>
        <el-descriptions-item label="等级评定">
          AAA级(≥90分) | AA级(≥80分) | A级(<80分)
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="calculateDialogVisible"
      title="计算KPI"
      width="500px"
    >
      <el-form :model="calculateForm" :rules="calculateRules" ref="calculateFormRef" label-width="100px">
        <el-form-item label="周期类型" prop="periodType">
          <el-select v-model="calculateForm.periodType" placeholder="请选择周期类型" style="width: 100%">
            <el-option label="月度" :value="1" />
            <el-option label="季度" :value="2" />
            <el-option label="年度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份" prop="year">
          <el-select v-model="calculateForm.year" placeholder="请选择年份" style="width: 100%">
            <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="季度" prop="quarter" v-if="calculateForm.periodType === 2">
          <el-select v-model="calculateForm.quarter" placeholder="请选择季度" style="width: 100%">
            <el-option label="第一季度" :value="1" />
            <el-option label="第二季度" :value="2" />
            <el-option label="第三季度" :value="3" />
            <el-option label="第四季度" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份" prop="month" v-if="calculateForm.periodType === 1">
          <el-select v-model="calculateForm.month" placeholder="请选择月份" style="width: 100%">
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="calculateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCalculate" :loading="submitLoading">开始计算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Cpu } from '@element-plus/icons-vue'
import { getKpiList, getKpiDetail, calculateKpis } from '@/api/kpi'

const loading = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const calculateDialogVisible = ref(false)
const calculateFormRef = ref<FormInstance>()

const currentYear = new Date().getFullYear()
const yearOptions = reactive([
  currentYear,
  currentYear - 1,
  currentYear - 2,
  currentYear - 3
])

const searchForm = reactive({
  supplierName: '',
  periodType: null as number | null,
  year: null as number | null,
  grade: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentKpi = ref<any>({})

const calculateForm = reactive({
  periodType: 2,
  year: currentYear - 1,
  quarter: 1,
  month: 1
})

const calculateRules: FormRules = {
  periodType: [{ required: true, message: '请选择周期类型', trigger: 'change' }],
  year: [{ required: true, message: '请选择年份', trigger: 'change' }]
}

const getProgressColor = (percentage: any) => {
  const p = Number(percentage)
  if (p >= 90) return '#67C23A'
  if (p >= 80) return '#E6A23C'
  return '#F56C6C'
}

const getScoreColor = (score: any) => {
  const s = Number(score)
  if (s >= 90) return '#67C23A'
  if (s >= 80) return '#E6A23C'
  return '#F56C6C'
}

const getGradeType = (grade: number) => {
  switch (grade) {
    case 3: return 'success'
    case 2: return 'warning'
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
    const res = await getKpiList(params)
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
  searchForm.supplierName = ''
  searchForm.periodType = null
  searchForm.year = null
  searchForm.grade = null
  handleSearch()
}

const handleView = async (row: any) => {
  try {
    const res = await getKpiDetail(row.id)
    currentKpi.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleCalculate = () => {
  calculateForm.periodType = 2
  calculateForm.year = currentYear - 1
  calculateForm.quarter = 1
  calculateForm.month = 1
  calculateDialogVisible.value = true
}

const handleSubmitCalculate = async () => {
  if (!calculateFormRef.value) return
  
  await calculateFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await calculateKpis(calculateForm)
        ElMessage.success('KPI计算完成')
        calculateDialogVisible.value = false
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
.kpi-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.kpi-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.kpi-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.kpi-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
}
</style>
