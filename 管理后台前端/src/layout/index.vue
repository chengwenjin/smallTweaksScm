<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">RBAC管理</span>
        <span v-else>R</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">用户管理</el-menu-item>
          <el-menu-item index="/system/roles">角色管理</el-menu-item>
          <el-menu-item index="/system/menus">菜单管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/log">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>日志管理</span>
          </template>
          <el-menu-item index="/system/operation-logs">操作日志</el-menu-item>
          <el-menu-item index="/system/login-logs">登录日志</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/scm">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>供应商全生命周期管理</span>
          </template>
          <el-sub-menu index="/scm/access">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>准入与分级管理</span>
            </template>
            <el-menu-item index="/scm/supplier">供应商管理</el-menu-item>
            <el-menu-item index="/scm/qualification">资质审核</el-menu-item>
            <el-menu-item index="/scm/classification">分级分类</el-menu-item>
            <el-menu-item index="/scm/alert">预警管理</el-menu-item>
            <el-menu-item index="/scm/blacklist">黑名单管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/scm/inquiry-tender">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>询价与招投标</span>
            </template>
            <el-menu-item index="/scm/requirement">采购需求单</el-menu-item>
            <el-menu-item index="/scm/inquiry">一键询价</el-menu-item>
            <el-menu-item index="/scm/comparison">智能比价</el-menu-item>
            <el-menu-item index="/scm/tender">招投标管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/scm/performance">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>绩效考核体系</span>
            </template>
            <el-menu-item index="/scm/kpi">多维考核</el-menu-item>
            <el-menu-item index="/scm/report">考核报告</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>

        <el-sub-menu index="/collaboration">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>采购全流程协同管理</span>
          </template>
          <el-sub-menu index="/collaboration/purchase-req-plan">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>采购需求与计划</span>
            </template>
            <el-menu-item index="/scm/purchase-request">采购申请</el-menu-item>
            <el-menu-item index="/scm/demand-summary">需求汇总</el-menu-item>
            <el-menu-item index="/scm/purchase-plan">智能补货</el-menu-item>
            <el-menu-item index="/scm/oa-approval">审批联动</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      router.push('/login')
    })
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4c;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
