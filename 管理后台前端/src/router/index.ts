import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'system/users',
        name: 'UserManagement',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/roles',
        name: 'RoleManagement',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/menus',
        name: 'MenuManagement',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理' }
      },
      {
        path: 'system/operation-logs',
        name: 'OperationLog',
        component: () => import('@/views/system/OperationLog.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'system/login-logs',
        name: 'LoginLog',
        component: () => import('@/views/system/LoginLog.vue'),
        meta: { title: '登录日志' }
      },
      {
        path: 'scm/supplier',
        name: 'SupplierManagement',
        component: () => import('@/views/scm/Supplier.vue'),
        meta: { title: '供应商管理' }
      },
      {
        path: 'scm/qualification',
        name: 'QualificationManagement',
        component: () => import('@/views/scm/Qualification.vue'),
        meta: { title: '资质审核' }
      },
      {
        path: 'scm/alert',
        name: 'AlertManagement',
        component: () => import('@/views/scm/Alert.vue'),
        meta: { title: '预警管理' }
      },
      {
        path: 'scm/classification',
        name: 'ClassificationManagement',
        component: () => import('@/views/scm/Classification.vue'),
        meta: { title: '分级分类' }
      },
      {
        path: 'scm/blacklist',
        name: 'BlacklistManagement',
        component: () => import('@/views/scm/Blacklist.vue'),
        meta: { title: '黑名单管理' }
      },
      {
        path: 'scm/requirement',
        name: 'RequirementManagement',
        component: () => import('@/views/scm/Requirement.vue'),
        meta: { title: '采购需求单' }
      },
      {
        path: 'scm/inquiry',
        name: 'InquiryManagement',
        component: () => import('@/views/scm/Inquiry.vue'),
        meta: { title: '一键询价' }
      },
      {
        path: 'scm/comparison',
        name: 'ComparisonManagement',
        component: () => import('@/views/scm/Comparison.vue'),
        meta: { title: '智能比价' }
      },
      {
        path: 'scm/tender',
        name: 'TenderManagement',
        component: () => import('@/views/scm/Tender.vue'),
        meta: { title: '招投标管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('accessToken')
  
  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
