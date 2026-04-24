<template>
  <div class="comparison-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="比价单编号">
          <el-input v-model="searchForm.comparisonNo" placeholder="请输入比价单编号" clearable />
        </el-form-item>
        <el-form-item label="比价单名称">
          <el-input v-model="searchForm.comparisonName" placeholder="请输入比价单名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待比价" :value="0" />
            <el-option label="比价中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
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
          新建比价单
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="comparisonNo" label="比价单编号" width="150" />
        <el-table-column prop="comparisonName" label="比价单名称" width="180" show-overflow-tooltip />
        <el-table-column prop="materialName" label="物资名称" width="120" />
        <el-table-column prop="reqQuantity" label="需求数量" width="100" />
        <el-table-column prop="recommendSupplierName" label="推荐供应商" width="150" />
        <el-table-column prop="recommendPrice" label="推荐价格" width="120">
          <template #default="{ row }">
            <span v-if="row.recommendPrice">¥{{ row.recommendPrice }}</span>
            <span v-else>-</span>
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
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleStart(row)" 
              :disabled="row.status !== 0"
            >
              开始比价
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
      title="比价单详情"
      width="900px"
    >
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="比价单编号">{{ currentComparison.comparisonNo }}</el-descriptions-item>
        <el-descriptions-item label="比价单名称">{{ currentComparison.comparisonName }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ currentComparison.materialName }}</el-descriptions-item>
        <el-descriptions-item label="需求数量">{{ currentComparison.reqQuantity }}</el-descriptions-item>
        <el-descriptions-item label="推荐供应商">{{ currentComparison.recommendSupplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="推荐价格">
          <span v-if="currentComparison.recommendPrice">¥{{ currentComparison.recommendPrice }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="推荐原因" :span="2">{{ currentComparison.recommendReason || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">报价对比</el-divider>
      
      <el-table :data="quoteList" border stripe>
        <el-table-column prop="supplierName" label="供应商名称" width="150" />
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold">¥{{ row.unitPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="140">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="交货日期" width="120" />
        <el-table-column prop="paymentTerms" label="付款条件" width="140" />
        <el-table-column prop="warranty" label="质保期" width="100" />
        <el-table-column prop="priceScore" label="价格分" width="80" />
        <el-table-column prop="deliveryScore" label="交期分" width="80" />
        <el-table-column prop="historyScore" label="历史分" width="80" />
        <el-table-column prop="totalScore" label="总分" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.isRecommended ? '#67C23A' : '' }">
              {{ row.totalScore }}
              <el-tag v-if="row.isRecommended" type="success" size="small" style="margin-left: 5px">推荐</el-tag>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="addDialogVisible"
      title="新建比价单"
      width="600px"
    >
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="120px">
        <el-form-item label="选择询价单" prop="inquiryId">
          <el-select v-model="addForm.inquiryId" placeholder="请选择询价单" style="width: 100%" filterable>
            <el-option
              v-for="item in inquiryOptions"
              :key="item.id"
              :label="`${item.inquiryNo} - ${item.inquiryName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="比价单名称" prop="comparisonName">
          <el-input v-model="addForm.comparisonName" placeholder="请输入比价单名称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getComparisonList, createComparison, startComparison } from '@/api/comparison'
import { getInquiryList } from '@/api/inquiry'

const loading = ref(false)
const submitLoading = ref(false)
const viewDialogVisible = ref(false)
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()

const searchForm = reactive({
  comparisonNo: '',
  comparisonName: '',
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const currentComparison = ref<any>({})
const quoteList = ref<any[]>([])
const inquiryOptions = ref<any[]>([])

const addForm = reactive({
  inquiryId: null as number | null,
  comparisonName: '',
  remark: ''
})

const addRules: FormRules = {
  inquiryId: [{ required: true, message: '请选择询价单', trigger: 'change' }],
  comparisonName: [{ required: true, message: '请输入比价单名称', trigger: 'blur' }]
}

const statusMap: Record<number, string> = {
  0: '待比价',
  1: '比价中',
  2: '已完成',
  3: '已取消'
}

const getStatusName = (status: number) => statusMap[status] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 2: return 'success'
    case 1: return 'warning'
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
    const res = await getComparisonList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadInquiryOptions = async () => {
  try {
    const res = await getInquiryList({ pageNum: 1, pageSize: 1000, status: 3 })
    inquiryOptions.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.comparisonNo = ''
  searchForm.comparisonName = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  addForm.inquiryId = null
  addForm.comparisonName = ''
  addForm.remark = ''
  addDialogVisible.value = true
}

const handleView = (row: any) => {
  currentComparison.value = { ...row }
  quoteList.value = []
  viewDialogVisible.value = true
}

const handleStart = (row: any) => {
  ElMessageBox.confirm('确定要开始智能比价吗？系统将自动分析各供应商报价。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await startComparison(row.id)
      ElMessage.success('比价完成')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createComparison(addForm)
        ElMessage.success('创建成功')
        addDialogVisible.value = false
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
  loadInquiryOptions()
})
</script>

<style scoped>
.comparison-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
