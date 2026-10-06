import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    component: () => import('../views/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeOutlined' }
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: () => import('../views/Accounts.vue'),
        meta: { title: '我的账户', icon: 'WalletOutlined' }
      },
      {
        path: 'transfer',
        name: 'Transfer',
        component: () => import('../views/Transfer.vue'),
        meta: { title: '转账汇款', icon: 'SwapOutlined' }
      },
      {
        path: 'loan',
        name: 'Loan',
        component: () => import('../views/Loan.vue'),
        meta: { title: '信用卡借款', icon: 'CreditCardOutlined' }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('../views/History.vue'),
        meta: { title: '交易记录', icon: 'UnorderedListOutlined' }
      },
      {
        path: 'bills',
        name: 'Bills',
        component: () => import('../views/Bills.vue'),
        meta: { title: '我的账单', icon: 'FileTextOutlined' }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('../views/AiChat.vue'),
        meta: { title: 'AI智能客服', icon: 'RobotOutlined' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', icon: 'UserOutlined' }
      },
      // 管理员路由
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue'),
        meta: { title: '数据大屏', icon: 'DashboardOutlined', role: 'ADMIN' }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue'),
        meta: { title: '用户管理', icon: 'TeamOutlined', role: 'ADMIN' }
      },
      {
        path: 'admin/accounts',
        name: 'AdminAccounts',
        component: () => import('../views/admin/AdminAccounts.vue'),
        meta: { title: '账户管理', icon: 'IdcardOutlined', role: 'ADMIN' }
      },
      {
        path: 'admin/loans',
        name: 'AdminLoans',
        component: () => import('../views/admin/AdminLoans.vue'),
        meta: { title: '借款管理', icon: 'FundOutlined', role: 'ADMIN' }
      },
      {
        path: 'admin/checks',
        name: 'AdminChecks',
        component: () => import('../views/admin/AdminChecks.vue'),
        meta: { title: '支票管理', icon: 'AuditOutlined', role: 'ADMIN' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.token

  if (to.path === '/login' || to.path === '/register') {
    if (isLoggedIn) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!isLoggedIn) {
      next('/login')
    } else {
      // 管理员路由权限检查
      if (to.meta.role === 'ADMIN' && userStore.userInfo?.role !== 'ADMIN') {
        next('/dashboard')
      } else {
        next()
      }
    }
  }
})

export default router
