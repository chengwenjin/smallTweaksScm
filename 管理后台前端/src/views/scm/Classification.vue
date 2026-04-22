<template>
  <div class="classification-management">
    <el-card>
      <div class="page-header">
        <h3>供应商分级分类管理</h3>
      </div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="供应商分级" name="supplier">
          <div class="toolbar">
            <el-button type="primary" @click="handleBatchSet">
              <el-icon><Plus /></el-icon>
              批量设置分级分类
            </el-button>
          </div>

          <el-table 
            :data="supplierTableData" 
            border 
            stripe 
            v-loading="supplierLoading"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="supplierCode" label="供应商编码" width="120" />
            <el-table-column prop="supplierName" label="供应商名称" width="180" />
            <el-table-column prop="materialCategory" label="物资类别" width="120">
              <template #default="{ row }">
                <el-tag :type="getMaterialCategoryType(row.materialCategory)">
                  {{ getMaterialCategoryName(row.materialCategory) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="cooperationLevel" label="合作分级" width="120">
              <template #default="{ row }">
                <el-tag :type="getCooperationLevelType(row.cooperationLevel)">
                  {{ getCooperationLevelName(row.cooperationLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getSupplierStatusType(row.status)">
                  {{ getSupplierStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEditSupplier(row)">
                  编辑分级
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="supplierPagination.pageNum"
            v-model:page-size="supplierPagination.pageSize"
            :total="supplierPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchSupplierData"
            @current-change="fetchSupplierData"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>

        <el-tab-pane label="变更记录" name="logs">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="供应商ID">
              <el-input v-model="searchForm.supplierId" placeholder="请输入供应商ID" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="logTableData" border stripe v-loading="logLoading">
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
            v-model:current-page="logPagination.pageNum"
            v-model:page-size="logPagination.pageSize"
            :total="logPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchLogData"
            @current-change="fetchLogData"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="editDialogVisible"
      :title="editDialogTitle"
      width="500px"
      @close="handleEditDialogClose"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="供应商">
          <el-input v-model="currentSupplierName" disabled />
        </el-form-item>
        <el-form-item label="物资类别" prop="materialCategory">
          <el-select v-model="editForm.materialCategory" placeholder="请选择" style="width: 100%">
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作分级" prop="cooperationLevel">
          <el-select v-model="editForm.cooperationLevel" placeholder="请选择" style="width: 100%">
            <el-option label="战略" :value="1" />
            <el-option label="合格" :value="2" />
            <el-option label="潜在" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因" prop="changeReason">
          <el-input v-model="editForm.changeReason" type="textarea" :rows="3" placeholder="请输入变更原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchDialogVisible"
      title="批量设置分级分类"
      width="600px"
      @close="handleBatchDialogClose"
    >
      <el-form :model="batchForm" :rules="batchRules" ref="batchFormRef" label-width="100px">
        <el-form-item label="已选供应商">
          <el-tag v-for="supplier in selectedSuppliers" :key="supplier.id" style="margin-right: 5px; margin-bottom: 5px">
            {{ supplier.supplierName }}
          </el-tag>
          <el-tag v-if="selectedSuppliers.length === 0" type="info">
            请在列表中选择供应商
          </el-tag>
        </el-form-item>
        <el-form-item label="物资类别" prop="materialCategory">
          <el-select v-model="batchForm.materialCategory" placeholder="请选择（不修改则不选）" clearable style="width: 100%">
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作分级" prop="cooperationLevel">
          <el-select v-model="batchForm.cooperationLevel" placeholder="请选择（不修改则不选）" clearable style="width: 100%">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getClassificationLogs, setClassification } from '@/api/classification'
import { getSupplierList, updateSupplier } from '@/api/supplier'

const activeTab = ref('supplier')

const supplierLoading = ref(false)
const logLoading = ref(false)
const editSubmitLoading = ref(false)
const batchSubmitLoading = ref(false)

const editDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const batchFormRef = ref<FormInstance>()
const currentSupplierName = ref('')

const selectedSuppliers = ref<any[]>([])

const searchForm = reactive({
  supplierId: ''
})

const supplierPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const logPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const supplierTableData = ref<any[]>([])
const logTableData = ref<any[]>([])

const editForm = reactive({
  supplierId: null as number | null,
  materialCategory: null as number | null,
  cooperationLevel: null as number | null,
  changeReason: ''
})

const batchForm = reactive({
  materialCategory: null as number | null,
  cooperationLevel: null as number | null,
  changeReason: ''
})

const editRules: FormRules = {
  materialCategory: [{ required: true, message: '请选择物资类别', trigger: 'change' }],
  cooperationLevel: [{ required: true, message: '请选择合作分级', trigger: 'change' }],
  changeReason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
}

const batchRules: FormRules = {
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

const supplierStatusMap: Record<number, string> = {
  0: '待准入',
  1: '已准入',
  2: '已冻结',
  3: '已淘汰'
}

const editDialogTitle = computed(() => '编辑分级分类')

const getMaterialCategoryName = (category: number | null | undefined) => {
  if (category === null || category === undefined) return '未设置'
  return materialCategoryMap[category] || '未知'
}

const getCooperationLevelName = (level: number | null | undefined) => {
  if (level === null || level === undefined) return '未设置'
  return cooperationLevelMap[level] || '未知'
}

const getSupplierStatusName = (status: number | null | undefined) => {
  if (status === null || status === undefined) return '未知'
  return supplierStatusMap[status] || '未知'
}

const getMaterialCategoryType = (category: number | null | undefined) => {
  if (category === null || category === undefined) return 'info'
  const types: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'primary' }
  return types[category] || 'info'
}

const getCooperationLevelType = (level: number | null | undefined) => {
  if (level === null || level === undefined) return 'info'
  const types: Record<number, string> = { 1: 'danger', 2: 'success', 3: 'warning' }
  return types[level] || 'info'
}

const getSupplierStatusType = (status: number | null | undefined) => {
  if (status === null || status === undefined) return 'info'
  const types: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

const fetchSupplierData = async () => {
  supplierLoading.value = true
  try {
    const params = {
      pageNum: supplierPagination.pageNum,
      pageSize: supplierPagination.pageSize
    }
    const res = await getSupplierList(params)
    supplierTableData.value = res.data.records || []
    supplierPagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    supplierLoading.value = false
  }
}

const fetchLogData = async () => {
  logLoading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: logPagination.pageNum,
      pageSize: logPagination.pageSize
    }
    const res = await getClassificationLogs(params)
    logTableData.value = res.data.records || []
    logPagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    logLoading.value = false
  }
}

const handleSearch = () => {
  logPagination.pageNum = 1
  fetchLogData()
}

const handleReset = () => {
  searchForm.supplierId = ''
  handleSearch()
}

const handleSelectionChange = (selection: any[]) => {
  selectedSuppliers.value = selection
}

const handleEditSupplier = (row: any) => {
  currentSupplierName.value = row.supplierName
  editForm.supplierId = row.id
  editForm.materialCategory = row.materialCategory
  editForm.cooperationLevel = row.cooperationLevel
  editForm.changeReason = ''
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitLoading.value = true
      try {
        const data = {
          supplierIds: [editForm.supplierId],
          materialCategory: editForm.materialCategory,
          cooperationLevel: editForm.cooperationLevel,
          changeReason: editForm.changeReason
        }
        await setClassification(data)
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        fetchSupplierData()
        fetchLogData()
      } catch (error) {
        console.error(error)
      } finally {
        editSubmitLoading.value = false
      }
    }
  })
}

const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
  Object.assign(editForm, {
    supplierId: null,
    materialCategory: null,
    cooperationLevel: null,
    changeReason: ''
  })
}

const handleBatchSet = () => {
  if (selectedSuppliers.value.length === 0) {
    ElMessage.warning('请先在列表中选择供应商')
    return
  }
  batchForm.materialCategory = null
  batchForm.cooperationLevel = null
  batchForm.changeReason = ''
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  if (!batchFormRef.value) return
  
  if (batchForm.materialCategory === null && batchForm.cooperationLevel === null) {
    ElMessage.warning('请至少选择物资类别或合作分级其中一项进行修改')
    return
  }
  
  await batchFormRef.value.validate(async (valid) => {
    if (valid) {
      batchSubmitLoading.value = true
      try {
        const data = {
          supplierIds: selectedSuppliers.value.map((s: any) => s.id),
          materialCategory: batchForm.materialCategory,
          cooperationLevel: batchForm.cooperationLevel,
          changeReason: batchForm.changeReason
        }
        await setClassification(data)
        ElMessage.success('批量设置成功')
        batchDialogVisible.value = false
        fetchSupplierData()
        fetchLogData()
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
    materialCategory: null,
    cooperationLevel: null,
    changeReason: ''
  })
}

onMounted(() => {
  fetchSupplierData()
  fetchLogData()
})
</script>

<style scoped>
.classification-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
