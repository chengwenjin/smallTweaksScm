<template>
  <div class="menu-management">
    <el-card>
      <!-- 操作按钮 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon>
          新增菜单
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getMenuTypeTag(row.menuType)" size="small">
              {{ getMenuTypeName(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissionKey" label="权限标识" width="180" />
        <el-table-column prop="path" label="路由地址" width="150" />
        <el-table-column prop="component" label="组件路径" width="200" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="isVisible" label="显示" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isVisible === 1 ? 'success' : 'info'" size="small">
              {{ row.isVisible === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdd(row)">添加子项</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="row.isSystem === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'menuName', value: 'id' }"
            placeholder="请选择上级菜单"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permissionKey" v-if="form.menuType !== 1">
          <el-input v-model="form.permissionKey" placeholder="例如：system:user:add" />
        </el-form-item>
        <el-form-item label="路由地址" prop="path" v-if="form.menuType !== 3">
          <el-input v-model="form.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="form.menuType === 2">
          <el-input v-model="form.component" placeholder="例如：system/user/index" />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="form.menuType !== 3">
          <el-input v-model="form.icon" placeholder="请输入Element Plus图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否显示" prop="isVisible" v-if="form.menuType !== 3">
          <el-radio-group v-model="form.isVisible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const tableData = ref<any[]>([])

const form = reactive({
  id: null as number | null,
  parentId: 0,
  menuName: '',
  menuType: 2,
  permissionKey: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  isVisible: 1,
  isCached: 0,
  isExternal: 0,
  status: 1
})

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// 菜单树选项（用于选择上级菜单）
const menuTreeOptions = computed(() => {
  return [{ id: 0, menuName: '顶级菜单', children: tableData.value }]
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取菜单类型标签
const getMenuTypeTag = (type: number) => {
  const tags: Record<number, string> = { 1: '', 2: 'success', 3: 'warning' }
  return tags[type] || ''
}

// 获取菜单类型名称
const getMenuTypeName = (type: number) => {
  const names: Record<number, string> = { 1: '目录', 2: '菜单', 3: '按钮' }
  return names[type] || '未知'
}

// 新增
const handleAdd = (row: any) => {
  dialogTitle.value = row ? `添加子菜单 (${row.menuName})` : '新增菜单'
  form.parentId = row ? row.id : 0
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error(error)
    }
  })
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await updateMenu(form)
          ElMessage.success('更新成功')
        } else {
          await createMenu(form)
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

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    parentId: 0,
    menuName: '',
    menuType: 2,
    permissionKey: '',
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    isVisible: 1,
    isCached: 0,
    isExternal: 0,
    status: 1
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.menu-management {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
