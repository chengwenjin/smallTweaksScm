<template>
  <div class="alert-management">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleMarkAllRead" :disabled="unreadCount === 0">
          全部标记已读
        </el-button>
        <el-tag type="danger" style="margin-left: 20px">未读: {{ unreadCount }}</el-tag>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="alertType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertType === 2 ? 'danger' : 'warning'">
              {{ row.alertType === 2 ? '已过期' : '即将到期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="qualificationName" label="资质名称" width="150" />
        <el-table-column prop="alertTitle" label="预警标题" min-width="200" />
        <el-table-column prop="alertContent" label="预警内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="daysBeforeExpiry" label="剩余天数" width="100">
          <template #default="{ row }">
            <span v-if="row.alertType === 1" :style="{ color: row.daysBeforeExpiry <= 7 ? 'red' : 'orange' }">
              {{ row.daysBeforeExpiry }}天
            </span>
            <span v-else style="color: red">已过期</span>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'info' : 'danger'">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertDate" label="预警日期" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleMarkAsRead(row)"
              v-if="row.isRead !== 1"
            >
              标记已读
            </el-button>
            <el-button link type="success" size="small" @click="handleViewDetail(row)">
              查看
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
      title="预警详情"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="供应商">{{ detailData.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="资质名称">{{ detailData.qualificationName }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">
          <el-tag :type="detailData.alertType === 2 ? 'danger' : 'warning'">
            {{ detailData.alertType === 2 ? '已过期' : '即将到期' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警标题">{{ detailData.alertTitle }}</el-descriptions-item>
        <el-descriptions-item label="预警内容">{{ detailData.alertContent }}</el-descriptions-item>
        <el-descriptions-item label="预警日期">{{ detailData.alertDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.isRead === 1 ? 'info' : 'danger'">
            {{ detailData.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlertList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/alert'

const loading = ref(false)
const unreadCount = ref(0)
const detailDialogVisible = ref(false)
const detailData = ref<any>({})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getAlertList(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.unreadCount || 0
  } catch (error) {
    console.error(error)
  }
}

const handleMarkAsRead = async (row: any) => {
  try {
    await markAsRead(row.id)
    ElMessage.success('标记成功')
    fetchData()
    fetchUnreadCount()
  } catch (error) {
    console.error(error)
  }
}

const handleMarkAllRead = () => {
  ElMessageBox.confirm('确定要将所有预警标记为已读吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await markAllAsRead()
      ElMessage.success('操作成功')
      fetchData()
      fetchUnreadCount()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleViewDetail = (row: any) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
  if (row.isRead !== 1) {
    handleMarkAsRead(row)
  }
}

onMounted(() => {
  fetchData()
  fetchUnreadCount()
})
</script>

<style scoped>
.alert-management {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
