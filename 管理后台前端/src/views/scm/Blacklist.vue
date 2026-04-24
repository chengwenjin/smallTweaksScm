<template>
  <div class="blacklist-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商编码">
          <el-input v-model="searchForm.supplierCode" placeholder="请输入供应商编码" clearable />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="黑名单类型">
          <el-select v-model="searchForm.blacklistType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="严重违约" :value="1" />
            <el-option label="质量问题" :value="2" />
            <el-option label="欺诈行为" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="在黑名单" :value="1" />
            <el-option label="已移除" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="handleAddBlacklist">
          <el-icon><Plus /></el-icon>
          列入黑名单
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierCode" label="供应商编码" width="120" />
        <el-table-column prop="supplierName" label="供应商名称" width="180" />
        <el-table-column prop="blacklistType" label="黑名单类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBlacklistTypeTagType(row.blacklistType)">
              {{ getBlacklistTypeName(row.blacklistType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="blacklistReason" label="列入原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="blacklistDate" label="列入日期" width="120" />
        <el-table-column prop="isPermanent" label="是否永久" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPermanent === 1 ? 'danger' : 'warning'">
              {{ row.isPermanent === 1 ? '永久' : '临时' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日期" width="120">
          <template #default="{ row }">
            <span>{{ row.isPermanent === 1 ? '-' : row.expireDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'info'">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="removeReason" label="移除原因" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.status === 2">{{ row.removeReason }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="removeDate" label="移除日期" width="120">
          <template #default="{ row }">
            <span>{{ row.status === 2 ? row.removeDate : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleRemoveBlacklist(row)"
              v-if="row.status === 1"
            >
              移除
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
      v-model="detailDialogVisible"
      title="黑名单详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商编码">{{ detailData.supplierCode }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ detailData.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="黑名单类型">
          <el-tag :type="getBlacklistTypeTagType(detailData.blacklistType)">
            {{ getBlacklistTypeName(detailData.blacklistType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否永久">
          <el-tag :type="detailData.isPermanent === 1 ? 'danger' : 'warning'">
            {{ detailData.isPermanent === 1 ? '永久' : '临时' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="列入日期">{{ detailData.blacklistDate }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ detailData.isPermanent === 1 ? '-' : detailData.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="detailData.status === 1 ? 'danger' : 'info'">
            {{ getStatusName(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="列入原因" :span="2">
          {{ detailData.blacklistReason }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2" v-if="detailData.remark">
          {{ detailData.remark }}
        </el-descriptions-item>
        <el-descriptions-item label="移除原因" :span="2" v-if="detailData.status === 2">
          {{ detailData.removeReason }}
        </el-descriptions-item>
        <el-descriptions-item label="移除日期" v-if="detailData.status === 2">
          {{ detailData.removeDate }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detailData.createTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="addDialogVisible"
      title="列入黑名单"
      width="500px"
      @close="handleAddDialogClose"
    >
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select 
            v-model="addForm.supplierId" 
            placeholder="请选择供应商" 
            filterable
            style="width: 100%"
            :disabled="isEditSupplier"
          >
            <el-option
              v-for="s in supplierList"
              :key="s.id"
              :label="s.supplierName + ' (' + s.supplierCode + ')'"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="黑名单类型" prop="blacklistType">
          <el-select v-model="addForm.blacklistType" placeholder="请选择" style="width: 100%">
            <el-option label="严重违约" :value="1" />
            <el-option label="质量问题" :value="2" />
            <el-option label="欺诈行为" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="列入原因" prop="blacklistReason">
          <el-input 
            v-model="addForm.blacklistReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入列入黑名单的原因"
          />
        </el-form-item>
        <el-form-item label="是否永久" prop="isPermanent">
          <el-radio-group v-model="addForm.isPermanent">
            <el-radio :value="1">永久</el-radio>
            <el-radio :value="0">临时</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="到期日期" prop="expireDate" v-if="addForm.isPermanent === 0">
          <el-date-picker
            v-model="addForm.expireDate"
            type="date"
            placeholder="请选择到期日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="addForm.remark" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="removeDialogVisible"
      title="移除黑名单"
      width="450px"
    >
      <el-form :model="removeForm" :rules="removeRules" ref="removeFormRef" label-width="100px">
        <el-form-item label="移除原因" prop="removeReason">
          <el-input 
            v-model="removeForm.removeReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入移除黑名单的原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="removeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRemove" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBlacklistList, addToBlacklist, removeFromBlacklist } from '@/api/blacklist'
import { getSupplierList } from '@/api/supplier'

const loading = ref(false)
const submitLoading = ref(false)
const detailDialogVisible = ref(false)
const addDialogVisible = ref(false)
const removeDialogVisible = ref(false)
const isEditSupplier = ref(false)
const addFormRef = ref<FormInstance>()
const removeFormRef = ref<FormInstance>()

const searchForm = reactive({
  supplierCode: '',
  supplierName: '',
  blacklistType: null as number | null,
  status: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])
const detailData = ref<any>({})
const supplierList = ref<any[]>([])

const addForm = reactive({
  supplierId: null as number | null,
  blacklistType: 1,
  blacklistReason: '',
  isPermanent: 0,
  expireDate: '',
  remark: ''
})

const removeForm = reactive({
  removeReason: '',
  targetId: null as number | null
})

const addRules: FormRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  blacklistType: [{ required: true, message: '请选择黑名单类型', trigger: 'change' }],
  blacklistReason: [{ required: true, message: '请输入列入原因', trigger: 'blur' }]
}

const removeRules: FormRules = {
  removeReason: [{ required: true, message: '请输入移除原因', trigger: 'blur' }]
}

const blacklistTypeMap: Record<number, string> = {
  1: '严重违约',
  2: '质量问题',
  3: '欺诈行为',
  4: '其他'
}

const blacklistTypeTagTypeMap: Record<number, string> = {
  1: 'danger',
  2: 'warning',
  3: 'danger',
  4: 'info'
}

const statusMap: Record<number, string> = {
  1: '在黑名单',
  2: '已移除'
}

const getBlacklistTypeName = (type: number) => blacklistTypeMap[type] || '未知'
const getBlacklistTypeTagType = (type: number) => blacklistTypeTagTypeMap[type] || 'info'
const getStatusName = (status: number) => statusMap[status] || '未知'

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getBlacklistList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchSupplierList = async () => {
  try {
    const res = await getSupplierList({ pageNum: 1, pageSize: 1000, status: 1 })
    supplierList.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.supplierCode = ''
  searchForm.supplierName = ''
  searchForm.blacklistType = null
  searchForm.status = null
  handleSearch()
}

const handleViewDetail = (row: any) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

const handleAddBlacklist = (row?: any) => {
  if (row) {
    isEditSupplier.value = true
    addForm.supplierId = row.id
  } else {
    isEditSupplier.value = false
    addForm.supplierId = null
  }
  addForm.blacklistType = 1
  addForm.blacklistReason = ''
  addForm.isPermanent = 0
  addForm.expireDate = ''
  addForm.remark = ''
  addDialogVisible.value = true
}

const handleAddDialogClose = () => {
  addFormRef.value?.resetFields()
  isEditSupplier.value = false
}

const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data: any = {
          supplierId: addForm.supplierId,
          blacklistType: addForm.blacklistType,
          blacklistReason: addForm.blacklistReason,
          isPermanent: addForm.isPermanent,
          remark: addForm.remark
        }
        if (addForm.isPermanent === 0 && addForm.expireDate) {
          data.expireDate = addForm.expireDate
        }
        await addToBlacklist(data)
        ElMessage.success('列入黑名单成功')
        addDialogVisible.value = false
        fetchData()
      } catch (error: any) {
        ElMessage.error(error.msg || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleRemoveBlacklist = (row: any) => {
  removeForm.targetId = row.id
  removeForm.removeReason = ''
  removeDialogVisible.value = true
}

const handleSubmitRemove = async () => {
  if (!removeFormRef.value || !removeForm.targetId) return
  
  await removeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await removeFromBlacklist(removeForm.targetId, {
          removeReason: removeForm.removeReason
        })
        ElMessage.success('移除黑名单成功')
        removeDialogVisible.value = false
        fetchData()
      } catch (error: any) {
        ElMessage.error(error.msg || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchData()
  fetchSupplierList()
})
</script>

<style scoped>
.blacklist-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
