<template>
  <div class="report-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="报告编号">
          <el-input v-model="searchForm.reportNo" placeholder="请输入报告编号" clearable />
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="searchForm.reportType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="季度报告" :value="1" />
            <el-option label="年度报告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="searchForm.year" placeholder="请选择" clearable style="width: 120px">
            <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
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
        <el-button type="primary" @click="handleGenerate">
          <el-icon><Document /></el-icon>
          生成报告
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="reportNo" label="报告编号" width="180" />
        <el-table-column prop="reportName" label="报告名称" width="280" />
        <el-table-column prop="reportTypeName" label="报告类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.reportType === 1 ? 'primary' : 'success'">
              {{ row.reportTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="quarter" label="季度" width="80">
          <template #default="{ row }">
            <span v-if="row.quarter">Q{{ row.quarter }}</span>
            <span v-else>-</span>
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
        <el-table-column prop="rank" label="排名" width="80">
          <template #default="{ row }">
            <span v-if="row.rank">
              <el-tag :type="row.rank <= 3 ? 'danger' : 'info'" size="small">
                第{{ row.rank }}名
              </el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="quotaSuggestion" label="配额建议" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="生成时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看报告</el-button>
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
      :title="'绩效报告 - ' + currentReport.reportNo"
      width="900px"
    >
      <div class="report-content">
        <div class="report-header">
          <h2>{{ currentReport.reportName }}</h2>
          <div class="report-meta">
            <span>报告编号: {{ currentReport.reportNo }}</span>
            <span>生成时间: {{ currentReport.createTime }}</span>
          </div>
        </div>

        <el-divider content-position="left">供应商信息</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="供应商名称">{{ currentReport.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ currentReport.creditCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentReport.contactPerson || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">绩效概览</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="score-card">
              <div class="score-title">综合评分</div>
              <div class="score-value" :style="{ color: getScoreColor(currentReport.totalScore) }">
                {{ currentReport.totalScore }}
              </div>
              <el-tag :type="getGradeType(currentReport.grade)" size="large" style="margin-top: 10px">
                {{ currentReport.gradeName }}
              </el-tag>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="score-card">
              <div class="score-title">本期排名</div>
              <div class="score-value" :style="{ color: currentReport.rank <= 3 ? '#F56C6C' : '#409EFF' }">
                第{{ currentReport.rank }}名
              </div>
              <div class="score-subtitle" style="margin-top: 10px; color: #909399">
                共{{ currentReport.totalSuppliers || 0 }}家供应商
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="score-card">
              <div class="score-title">上期对比</div>
              <div class="score-value">
                <span v-if="currentReport.scoreChange > 0" style="color: #67C23A">
                  ↑ {{ Math.abs(currentReport.scoreChange) }}
                </span>
                <span v-else-if="currentReport.scoreChange < 0" style="color: #F56C6C">
                  ↓ {{ Math.abs(currentReport.scoreChange) }}
                </span>
                <span v-else style="color: #909399">持平</span>
              </div>
              <div class="score-subtitle" style="margin-top: 10px; color: #909399">
                上期: {{ currentReport.prevScore || '-' }}分
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="score-card">
              <div class="score-title">配额建议</div>
              <div class="score-value" style="color: #409EFF">
                {{ currentReport.quotaSuggestion }}
              </div>
              <div class="score-subtitle" style="margin-top: 10px; color: #909399">
                建议配额: {{ currentReport.quotaPercentage || 0 }}%
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider content-position="left">KPI指标明细</el-divider>
        <el-table :data="[currentReport]" border show-summary>
          <el-table-column prop="deliveryOnTimeRate" label="交付准时率" width="180">
            <template #default="{ row }">
              <el-progress 
                :percentage="Number(row.deliveryOnTimeRate)" 
                :stroke-width="20"
                :color="getProgressColor(row.deliveryOnTimeRate)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="qualityPassRate" label="来料质检合格率" width="180">
            <template #default="{ row }">
              <el-progress 
                :percentage="Number(row.qualityPassRate)" 
                :stroke-width="20"
                :color="getProgressColor(row.qualityPassRate)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="priceCompetitiveness" label="价格竞争力" width="180">
            <template #default="{ row }">
              <el-progress 
                :percentage="Number(row.priceCompetitiveness)" 
                :stroke-width="20"
                :color="getProgressColor(row.priceCompetitiveness)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="serviceResponseSpeed" label="服务响应速度" width="180">
            <template #default="{ row }">
              <el-progress 
                :percentage="Number(row.serviceResponseSpeed)" 
                :stroke-width="20"
                :color="getProgressColor(row.serviceResponseSpeed)"
              />
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">综合分析</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover" class="analysis-card">
              <template #header>
                <div class="card-header">
                  <el-icon><CircleCheck /></el-icon>
                  <span>优势分析</span>
                </div>
              </template>
              <div class="analysis-content" v-html="currentReport.strengths || '暂无优势分析'"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="analysis-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Warning /></el-icon>
                  <span>劣势分析</span>
                </div>
              </template>
              <div class="analysis-content" v-html="currentReport.weaknesses || '暂无劣势分析'"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="24">
            <el-card shadow="hover" class="analysis-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Tools /></el-icon>
                  <span>改进建议</span>
                </div>
              </template>
              <div class="analysis-content" v-html="currentReport.improvementSuggestions || '暂无改进建议'"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider content-position="left">权重配置</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="交付准时率权重">{{ currentReport.deliveryWeight }}%</el-descriptions-item>
          <el-descriptions-item label="来料质检合格率权重">{{ currentReport.qualityWeight }}%</el-descriptions-item>
          <el-descriptions-item label="价格竞争力权重">{{ currentReport.priceWeight }}%</el-descriptions-item>
          <el-descriptions-item label="服务响应速度权重">{{ currentReport.serviceWeight }}%</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印报告
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="generateDialogVisible"
      title="生成绩效报告"
      width="500px"
    >
      <el-form :model="generateForm" :rules="generateRules" ref="generateFormRef" label-width="100px">
        <el-form-item label="报告类型" prop="reportType">
          <el-select v-model="generateForm.reportType" placeholder="请选择报告类型" style="width: 100%">
            <el-option label="季度报告" :value="1" />
            <el-option label="年度报告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份" prop="year">
          <el-select v-model="generateForm.year" placeholder="请选择年份" style="width: 100%">
            <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="季度" prop="quarter" v-if="generateForm.reportType === 1">
          <el-select v-model="generateForm.quarter" placeholder="请选择季度" style="width: 100%">
            <el-option label="第一季度" :value="1" />
            <el-option label="第二季度" :value="2" />
            <el-option label="第三季度" :value="3" />
            <el-option label="第四季度" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitGenerate" :loading="submitLoading">生成报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Document, Printer, CircleCheck, Warning, Tools } from '@element-plus/icons-vue'
import { getReportList, getReportDetail, generateReports } from '@/api/report'

const loading = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const generateDialogVisible = ref(false)
const generateFormRef = ref<FormInstance>()

const currentYear = new Date().getFullYear()
const yearOptions = reactive([
  currentYear,
  currentYear - 1,
  currentYear - 2,
  currentYear - 3
])

const searchForm = reactive({
  reportNo: '',
  reportType: null as number | null,
  year: null as number | null,
  supplierName: '',
  grade: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentReport = ref<any>({})

const generateForm = reactive({
  reportType: 1,
  year: currentYear - 1,
  quarter: 1
})

const generateRules: FormRules = {
  reportType: [{ required: true, message: '请选择报告类型', trigger: 'change' }],
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
    const res = await getReportList(params)
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
  searchForm.reportNo = ''
  searchForm.reportType = null
  searchForm.year = null
  searchForm.supplierName = ''
  searchForm.grade = null
  handleSearch()
}

const handleView = async (row: any) => {
  try {
    const res = await getReportDetail(row.id)
    currentReport.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleGenerate = () => {
  generateForm.reportType = 1
  generateForm.year = currentYear - 1
  generateForm.quarter = 1
  generateDialogVisible.value = true
}

const handleSubmitGenerate = async () => {
  if (!generateFormRef.value) return
  
  await generateFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await generateReports(generateForm)
        ElMessage.success('绩效报告生成完成')
        generateDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handlePrint = () => {
  const printContent = document.querySelector('.report-content')
  if (printContent) {
    const printWindow = window.open('', '_blank')
    if (printWindow) {
      printWindow.document.write(`
        <html>
          <head>
            <title>绩效报告 - ${currentReport.value.reportNo}</title>
            <style>
              body { font-family: 'Microsoft YaHei', Arial, sans-serif; padding: 20px; }
              .report-header { text-align: center; border-bottom: 2px solid #409EFF; padding-bottom: 20px; }
              .report-header h2 { margin: 0; color: #303133; }
              .report-meta { margin-top: 10px; color: #909399; font-size: 14px; }
              .report-meta span { margin: 0 15px; }
              .score-card { text-align: center; background: #f5f7fa; border-radius: 8px; }
              .score-title { font-size: 14px; color: #909399; }
              .score-value { font-size: 32px; font-weight: bold; margin-top: 10px; }
              .score-subtitle { font-size: 12px; color: #909399; }
              .analysis-card { margin-bottom: 20px; }
              .card-header { display: flex; align-items: center; gap: 8px; font-weight: bold; }
              .analysis-content { padding: 10px; line-height: 1.8; }
              table { width: 100%; border-collapse: collapse; margin: 10px 0; }
              th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
              th { background: #f5f7fa; }
            </style>
          </head>
          <body>
            ${printContent.innerHTML}
          </body>
        </html>
      `)
      printWindow.document.close()
      printWindow.print()
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.report-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.score-card {
  text-align: center;
}

.score-title {
  font-size: 14px;
  color: #909399;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  margin-top: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.analysis-card {
  margin-bottom: 20px;
}

.analysis-content {
  line-height: 1.8;
  color: #606266;
}

:deep(.el-descriptions__label) {
  width: 150px;
}
</style>
