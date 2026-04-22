<template>
  <div class="supplier-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商编码">
          <el-input v-model="searchForm.supplierCode" placeholder="请输入供应商编码" clearable />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待准入" :value="0" />
            <el-option label="已准入" :value="1" />
            <el-option label="已冻结" :value="2" />
            <el-option label="已淘汰" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="物资类别">
          <el-select v-model="searchForm.materialCategory" placeholder="请选择" clearable style="width: 120px">
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作分级">
          <el-select v-model="searchForm.cooperationLevel" placeholder="请选择" clearable style="width: 120px">
            <el-option label="战略" :value="1" />
            <el-option label="合格" :value="2" />
            <el-option label="潜在" :value="3" />
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
          新增供应商
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierCode" label="供应商编码" width="120" />
        <el-table-column prop="supplierName" label="供应商名称" width="180" />
        <el-table-column prop="supplierType" label="类型" width="100">
          <template #default="{ row }">
            {{ getSupplierTypeName(row.supplierType) }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="等级" width="80">
          <template #default="{ row }">
            {{ getGradeName(row.grade) }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="materialCategory" label="物资类别" width="100">
          <template #default="{ row }">
            {{ getMaterialCategoryName(row.materialCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="cooperationLevel" label="合作分级" width="100">
          <template #default="{ row }">
            {{ getCooperationLevelName(row.cooperationLevel) }}
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
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="form.supplierCode" :disabled="isEdit" placeholder="请输入供应商编码" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="供应商类型" prop="supplierType">
          <el-select v-model="form.supplierType" placeholder="请选择" style="width: 100%">
            <el-option label="生产型" :value="1" />
            <el-option label="贸易型" :value="2" />
            <el-option label="服务型" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" prop="grade">
          <el-select v-model="form.grade" placeholder="请选择" style="width: 100%">
            <el-option label="A级" :value="1" />
            <el-option label="AA级" :value="2" />
            <el-option label="AAA级" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">待准入</el-radio>
            <el-radio :value="1">已准入</el-radio>
            <el-radio :value="2">已冻结</el-radio>
            <el-radio :value="3">已淘汰</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="物资类别" prop="materialCategory">
          <el-select v-model="form.materialCategory" placeholder="请选择" style="width: 100%">
            <el-option label="原材料" :value="1" />
            <el-option label="辅料" :value="2" />
            <el-option label="设备" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作分级" prop="cooperationLevel">
          <el-select v-model="form.cooperationLevel" placeholder="请选择" style="width: 100%">
            <el-option label="战略" :value="1" />
            <el-option label="合格" :value="2" />
            <el-option label="潜在" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSupplierList, createSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  supplierCode: '',
  supplierName: '',
  status: null as number | null,
  materialCategory: null as number | null,
  cooperationLevel: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const form = reactive({
  id: null as number | null,
  supplierCode: '',
  supplierName: '',
  supplierType: 1,
  grade: 1,
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  status: 0,
  materialCategory: null as number | null,
  cooperationLevel: null as number | null,
  remark: ''
})

const rules: FormRules = {
  supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
}

const supplierTypeMap: Record<number, string> = {
  1: '生产型',
  2: '贸易型',
  3: '服务型'
}

const gradeMap: Record<number, string> = {
  1: 'A级',
  2: 'AA级',
  3: 'AAA级'
}

const statusMap: Record<number, string> = {
  0: '待准入',
  1: '已准入',
  2: '已冻结',
  3: '已淘汰'
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

const getSupplierTypeName = (type: number) => supplierTypeMap[type] || '未知'
const getGradeName = (grade: number) => gradeMap[grade] || '未知'
const getStatusName = (status: number) => statusMap[status] || '未知'
const getMaterialCategoryName = (category: number) => materialCategoryMap[category] || '未知'
const getCooperationLevelName = (level: number) => cooperationLevelMap[level] || '未知'

const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
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
    const res = await getSupplierList(params)
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
  searchForm.supplierCode = ''
  searchForm.supplierName = ''
  searchForm.status = null
  searchForm.materialCategory = null
  searchForm.cooperationLevel = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增供应商'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑供应商'
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该供应商吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSupplier(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateSupplier(form.id!, form)
          ElMessage.success('更新成功')
        } else {
          await createSupplier(form)
          ElMessage.success('创建成功')
        }
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
    supplierCode: '',
    supplierName: '',
    supplierType: 1,
    grade: 1,
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    address: '',
    status: 0,
    materialCategory: null,
    cooperationLevel: null,
    remark: ''
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.supplier-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
