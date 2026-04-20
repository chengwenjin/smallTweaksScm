<template>
  <div class="qualification-management">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable />
        </el-form-item>
        <el-form-item label="资质类型">
          <el-select v-model="searchForm.qualificationType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="营业执照" value="BUSINESS_LICENSE" />
            <el-option label="税务登记证" value="TAX_REGISTRATION" />
            <el-option label="组织机构代码证" value="ORG_CODE" />
            <el-option label="产品认证" value="PRODUCT_CERT" />
            <el-option label="质量认证" value="QUALITY_CERT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="审核中" :value="1" />
            <el-option label="审核通过" :value="2" />
            <el-option label="审核拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警状态">
          <el-select v-model="searchForm.alertStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="即将到期" :value="1" />
            <el-option label="已过期" :value="2" />
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
          新增资质
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="qualificationType" label="资质类型" width="120">
          <template #default="{ row }">
            {{ getQualificationTypeName(row.qualificationType) }}
          </template>
        </el-table-column>
        <el-table-column prop="qualificationName" label="资质名称" width="150" />
        <el-table-column prop="certificateNo" label="证书编号" width="150" />
        <el-table-column prop="issueDate" label="发证日期" width="110" />
        <el-table-column prop="expiryDate" label="有效期至" width="110" />
        <el-table-column prop="isLongTerm" label="长期有效" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isLongTerm === 1 ? 'success' : 'info'">
              {{ row.isLongTerm === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertStatus" label="预警状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isLongTerm === 1" type="info">无</el-tag>
            <el-tag v-else :type="getAlertType(row.alertStatus)">
              {{ getAlertName(row.alertStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditType(row.auditStatus)">
              {{ getAuditName(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button link type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleAudit(row)"
              v-if="row.auditStatus === 0 || row.auditStatus === 1"
            >
              审核
            </el-button>
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
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width: 100%">
            <el-option
              v-for="supplier in supplierList"
              :key="supplier.id"
              :label="supplier.supplierName"
              :value="supplier.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资质类型" prop="qualificationType">
          <el-select v-model="form.qualificationType" placeholder="请选择" style="width: 100%">
            <el-option label="营业执照" value="BUSINESS_LICENSE" />
            <el-option label="税务登记证" value="TAX_REGISTRATION" />
            <el-option label="组织机构代码证" value="ORG_CODE" />
            <el-option label="产品认证" value="PRODUCT_CERT" />
            <el-option label="质量认证" value="QUALITY_CERT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="资质名称" prop="qualificationName">
          <el-input v-model="form.qualificationName" placeholder="请输入资质名称" />
        </el-form-item>
        <el-form-item label="证书编号" prop="certificateNo">
          <el-input v-model="form.certificateNo" placeholder="请输入证书编号" />
        </el-form-item>
        <el-form-item label="发证机关" prop="issuingAuthority">
          <el-input v-model="form.issuingAuthority" placeholder="请输入发证机关" />
        </el-form-item>
        <el-form-item label="发证日期" prop="issueDate">
          <el-date-picker
            v-model="form.issueDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="长期有效" prop="isLongTerm">
          <el-switch v-model="form.isLongTerm" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="有效期至" prop="expiryDate" v-if="form.isLongTerm !== 1">
          <el-date-picker
            v-model="form.expiryDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="附件上传" prop="fileUrls">
          <el-upload
            class="upload-demo"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :file-list="fileList"
            multiple
            :limit="5"
            list-type="text"
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf/word/excel格式，最多上传5个文件</div>
            </template>
          </el-upload>
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

    <el-dialog
      v-model="auditDialogVisible"
      title="资质审核"
      width="500px"
    >
      <el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="100px">
        <el-form-item label="审核状态" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :value="2">审核通过</el-radio>
            <el-radio :value="3">审核拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditSubmit" :loading="auditSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="资质详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商">{{ viewData.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="资质类型">{{ getQualificationTypeName(viewData.qualificationType) }}</el-descriptions-item>
        <el-descriptions-item label="资质名称">{{ viewData.qualificationName }}</el-descriptions-item>
        <el-descriptions-item label="证书编号">{{ viewData.certificateNo }}</el-descriptions-item>
        <el-descriptions-item label="发证机关">{{ viewData.issuingAuthority }}</el-descriptions-item>
        <el-descriptions-item label="发证日期">{{ viewData.issueDate }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ viewData.isLongTerm === 1 ? '长期有效' : viewData.expiryDate }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditType(viewData.auditStatus)">{{ getAuditName(viewData.auditStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警状态">
          <el-tag v-if="viewData.isLongTerm === 1" type="info">无</el-tag>
          <el-tag v-else :type="getAlertType(viewData.alertStatus)">{{ getAlertName(viewData.alertStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核人">{{ viewData.auditBy }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark }}</el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
          <div v-if="viewData.fileUrls && viewData.fileUrls.length > 0">
            <div v-for="(url, index) in viewData.fileUrls" :key="index" style="margin-bottom: 5px">
              <el-link :href="url" target="_blank" type="primary">查看附件{{ index + 1 }}</el-link>
            </div>
          </div>
          <span v-else>暂无附件</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadProps } from 'element-plus'
import { getQualificationList, createQualification, updateQualification, deleteQualification, auditQualification } from '@/api/qualification'
import { getSupplierList } from '@/api/supplier'
import { uploadFiles } from '@/api/file'

const loading = ref(false)
const submitLoading = ref(false)
const auditSubmitLoading = ref(false)
const dialogVisible = ref(false)
const auditDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const auditFormRef = ref<FormInstance>()

const supplierList = ref<any[]>([])
const fileList = ref<UploadFile[]>([])

const searchForm = reactive({
  supplierName: '',
  qualificationType: '',
  auditStatus: null as number | null,
  alertStatus: null as number | null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const form = reactive({
  id: null as number | null,
  supplierId: null as number | null,
  qualificationType: '',
  qualificationName: '',
  certificateNo: '',
  issuingAuthority: '',
  issueDate: '',
  expiryDate: '',
  isLongTerm: 0,
  fileUrls: [] as string[],
  remark: ''
})

const auditForm = reactive({
  id: null as number | null,
  auditStatus: 2,
  auditRemark: ''
})

const viewData = ref<any>({})

const token = localStorage.getItem('accessToken')

const uploadAction = computed(() => '/api/scm/files/upload')

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${token}`
}))

const rules: FormRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  qualificationType: [{ required: true, message: '请选择资质类型', trigger: 'change' }],
  qualificationName: [{ required: true, message: '请输入资质名称', trigger: 'blur' }]
}

const auditRules: FormRules = {
  auditStatus: [{ required: true, message: '请选择审核状态', trigger: 'change' }]
}

const qualificationTypeMap: Record<string, string> = {
  'BUSINESS_LICENSE': '营业执照',
  'TAX_REGISTRATION': '税务登记证',
  'ORG_CODE': '组织机构代码证',
  'PRODUCT_CERT': '产品认证',
  'QUALITY_CERT': '质量认证',
  'OTHER': '其他'
}

const auditStatusMap: Record<number, string> = {
  0: '待审核',
  1: '审核中',
  2: '审核通过',
  3: '审核拒绝'
}

const alertStatusMap: Record<number, string> = {
  0: '正常',
  1: '即将到期',
  2: '已过期'
}

const getQualificationTypeName = (type: string) => qualificationTypeMap[type] || type
const getAuditName = (status: number) => auditStatusMap[status] || '未知'
const getAlertName = (status: number) => alertStatusMap[status] || '未知'

const getAuditType = (status: number) => {
  switch (status) {
    case 2: return 'success'
    case 3: return 'danger'
    case 1: return 'warning'
    default: return 'info'
  }
}

const getAlertType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'warning'
    case 2: return 'danger'
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
    const res = await getQualificationList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchSuppliers = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000
    }
    const res = await getSupplierList(params)
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
  searchForm.supplierName = ''
  searchForm.qualificationType = ''
  searchForm.auditStatus = null
  searchForm.alertStatus = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增资质'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑资质'
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    supplierId: row.supplierId,
    qualificationType: row.qualificationType,
    qualificationName: row.qualificationName,
    certificateNo: row.certificateNo,
    issuingAuthority: row.issuingAuthority,
    issueDate: row.issueDate || '',
    expiryDate: row.expiryDate || '',
    isLongTerm: row.isLongTerm || 0,
    fileUrls: row.fileUrls || [],
    remark: row.remark || ''
  })
  fileList.value = (row.fileUrls || []).map((url: string, index: number) => ({
    name: `附件${index + 1}`,
    url: url,
    status: 'success'
  }))
  dialogVisible.value = true
}

const handleView = (row: any) => {
  viewData.value = {
    ...row,
    fileUrls: row.fileUrls ? row.fileUrls.split(',').filter((u: string) => u.trim()) : []
  }
  viewDialogVisible.value = true
}

const handleAudit = (row: any) => {
  auditForm.id = row.id
  auditForm.auditStatus = 2
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该资质吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteQualification(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleUploadSuccess: UploadProps['onSuccess'] = (response: any, uploadFile: UploadFile) => {
  if (response.code === 200) {
    const urls = response.data || []
    urls.forEach((url: string) => {
      form.fileUrls.push(url)
    })
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData = {
          ...form,
          fileUrls: form.fileUrls
        }
        if (isEdit.value) {
          await updateQualification(form.id!, submitData)
          ElMessage.success('更新成功')
        } else {
          await createQualification(submitData)
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

const handleAuditSubmit = async () => {
  if (!auditFormRef.value) return
  
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      auditSubmitLoading.value = true
      try {
        await auditQualification(auditForm.id!, {
          auditStatus: auditForm.auditStatus,
          auditRemark: auditForm.auditRemark
        })
        ElMessage.success('审核成功')
        auditDialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error(error)
      } finally {
        auditSubmitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    supplierId: null,
    qualificationType: '',
    qualificationName: '',
    certificateNo: '',
    issuingAuthority: '',
    issueDate: '',
    expiryDate: '',
    isLongTerm: 0,
    fileUrls: [],
    remark: ''
  })
  fileList.value = []
}

onMounted(() => {
  fetchData()
  fetchSuppliers()
})
</script>

<style scoped>
.qualification-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
