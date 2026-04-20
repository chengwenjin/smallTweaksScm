<template>
  <div class="dashboard">
    <el-card>
      <h2>欢迎使用RBAC管理后台</h2>
      <p style="margin-top: 20px; color: #666;">
        这是一个基于Spring Boot 3 + Vue 3的权限管理系统。
      </p>
      
      <el-row :gutter="20" style="margin-top: 30px;">
        <el-col :span="6">
          <el-statistic title="用户数" :value="100" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="角色数" :value="10" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="菜单数" :value="25" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="在线用户" :value="5" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.dashboard {
  padding: 20px;
}
</style>
