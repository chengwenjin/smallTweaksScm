<template>
  <div class="classification-management">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleBatchSet">
          <el-icon><Plus /></el-icon>
          批量设置分级分类
        </el-button>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商ID">
          <el-input v-model="searchForm.supplierId" placeholder="请输入供应商ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierCode" label="供应商编码" width="120" />
        <el-table-column prop="supplierName" label="供应商名称" width="180" />
        <el-table-column prop="oldMaterialCategory" label="原物资类别" width="120">
          <template #default="{ row }">
            {{ getMaterialCategoryName(row.oldMaterialCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="newMaterialCategory" label="新物资类别" width="120">
          <template #default="{ row }">
            {{ getMaterialCategoryName(row.newMaterialCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="oldCooperationLevel" label="原合作分级" width="120">
          <template #default="{ row }">
            {{ getCooperationLevelName(row.oldCooperationLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="newCooperationLevel" label="新合作分级" width="120">
          <template #default="{ row }">
            {{ getCooperationLevelName(row.newCooperationLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="changeReason" label="变更原因" min-width="150" />
        <el-table-column prop="createBy" label="操作人" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
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
      v-model="batchDialogVisible"
      title="批量设置分级分类"
      width="600px"
      @close="handleBatchDialogClose"
    >
      <el-form :model="batchForm" :rules="batchRules" ref="batchFormRef" label-width="100px">
        <el-form-item label="供应商ID列表" prop="supplierIds">
          <el-input v-model="batchForm.supplierIds" type="textarea" :rows="3" placeholder="请输入供应商ID，多个用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="物资类别" prop="materialCategory">
          <el-select v-model="batchForm.materialCategory" placeholder="请选择" style="width: 100%">
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作分级" prop="cooperationLevel">
          <el-select v-model="batchForm.cooperationLevel" placeholder="请选择" style="width: 100%">
            <el-option label="战略" :value="1" />
            <el-option label="合格" :value="2" />
            <el-option label="潜在" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因" prop="changeReason">
          <el-input v-model="batchForm.changeReason" type="textarea" :rows="3" placeholder="请输入变更原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="batchSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getClassificationLogs, setClassification } from '@/api/classification'

const loading = ref(false)
batchSubmitLoading = ref(false)
const batchDialogVisible = ref(false)
const batchFormRef = ref<FormInstance>()

const searchForm = reactive({
  supplierId: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const batchForm = reactive({
  supplierIds: '',
  materialCategory: null as number | null,
  cooperationLevel: null as number | null,
  changeReason: ''
})

const batchRules: FormRules = {
  supplierIds: [{ required: true, message: '请输入供应商ID列表', trigger: 'blur' }],
  changeReason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
}

const materialCategoryMap: Record<number, string> = {
  1: '原材料',
  2: '辅料',
  3: '设备'
}

const cooperationLevelMap: Record<number, string> = {
  1: '战略',
  2: '合格',
  3: '潜在'
}

const getMaterialCategoryName = (category: number) => materialCategoryMap[category] || '未知'
const getCooperationLevelName = (level: number) => cooperationLevelMap[level] || '未知'

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getClassificationLogs(params)
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
  searchForm.supplierId = ''
  handleSearch()
}

const handleBatchSet = () => {
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  if (!batchFormRef.value) return
  
  await batchFormRef.value.validate(async (valid) => {
    if (valid) {
      batchSubmitLoading.value = true
      try {
        const supplierIdStr = batchForm.supplierIds
        const supplierIds = supplierIdStr.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
        
        if (supplierIds.length === 0) {
          ElMessage.error('请输入有效的供应商ID')
          return
        }
        
        const data = {
          supplierIds,
          materialCategory: batchForm.materialCategory,
          cooperationLevel: batchForm.cooperationLevel,
          changeReason: batchForm.changeReason
        }
        
        await setClassification(data)
        ElMessage.success('设置成功')
        batchDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        batchSubmitLoading.value = false
      }
    }
  })
}

const handleBatchDialogClose = () => {
  batchFormRef.value?.resetFields()
  Object.assign(batchForm, {
    supplierIds: '',
    materialCategory: null,
    cooperationLevel: null,
    changeReason: ''
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.classification-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
