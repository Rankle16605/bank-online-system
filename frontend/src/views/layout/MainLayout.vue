<template>
  <a-layout class="main-layout">
    <!-- 侧边栏 -->
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      :trigger="null" 
      collapsible
      :width="siderWidth"
      :collapsed-width="80"
      class="layout-sider"
    >
      <!-- Logo区域 -->
      <div class="logo-container">
        <div class="logo-icon">
          <DollarCircleOutlined />
        </div>
        <div v-if="!collapsed" class="logo-text">
          <div class="logo-title">ICBC</div>
          <div class="logo-subtitle">中国工商银行</div>
        </div>
      </div>
      
      <!-- 导航菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        theme="light"
        class="nav-menu"
        @click="handleMenuClick"
      >
        <a-menu-item key="dashboard">
          <template #icon><DashboardOutlined /></template>
          <span>首页概览</span>
        </a-menu-item>
        <a-menu-item key="accounts">
          <template #icon><IdcardOutlined /></template>
          <span>我的账户</span>
        </a-menu-item>
        <a-menu-item key="transfer">
          <template #icon><TransactionOutlined /></template>
          <span>转账汇款</span>
        </a-menu-item>
        <a-menu-item key="loan">
          <template #icon><CreditCardOutlined /></template>
          <span>信用卡借款</span>
        </a-menu-item>
        <a-menu-item key="history">
          <template #icon><OrderedListOutlined /></template>
          <span>交易记录</span>
        </a-menu-item>
        <a-menu-item key="bills">
          <template #icon><FileTextOutlined /></template>
          <span>我的账单</span>
        </a-menu-item>
        <a-menu-item key="ai-chat">
          <template #icon><CustomerServiceOutlined /></template>
          <span>AI智能客服</span>
        </a-menu-item>

        <!-- 管理员菜单分隔 -->
        <a-menu-divider v-if="userStore.userInfo?.role === 'ADMIN'" />
        <a-menu-item-group v-if="userStore.userInfo?.role === 'ADMIN'" key="admin-group" title="系统管理">
          <a-menu-item key="admin/dashboard">
            <template #icon><DashboardOutlined /></template>
            <span>数据大屏</span>
          </a-menu-item>
          <a-menu-item key="admin/users">
            <template #icon><TeamOutlined /></template>
            <span>用户管理</span>
          </a-menu-item>
          <a-menu-item key="admin/accounts">
            <template #icon><IdcardOutlined /></template>
            <span>账户管理</span>
          </a-menu-item>
          <a-menu-item key="admin/loans">
            <template #icon><FundOutlined /></template>
            <span>借款管理</span>
          </a-menu-item>
          <a-menu-item key="admin/checks">
            <template #icon><AuditOutlined /></template>
            <span>支票管理</span>
          </a-menu-item>
        </a-menu-item-group>
      </a-menu>

      <!-- 侧边栏底部品牌语 -->
      <div v-if="!collapsed" class="sider-footer">
        <!-- 金句 - 毛笔书法体 -->
        <div class="footer-motto font-calligraphy">工于至诚，行以致远</div>
        <div class="footer-brand">
          <SafetyCertificateOutlined class="footer-icon" />
          <span>安全 · 稳健 · 值得信赖</span>
        </div>
      </div>
    </a-layout-sider>

    <!-- 拖拽手柄 -->
    <div 
      v-show="!collapsed"
      class="resize-handle"
      @mousedown.prevent="startResize"
    >
      <div class="resize-grip">
        <span></span><span></span><span></span>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <a-layout class="main-area">
      <!-- 顶部导航栏 -->
      <a-layout-header class="layout-header">
        <div class="header-left">
          <component
            :is="collapsed ? 'MenuUnfoldOutlined' : 'MenuFoldOutlined'"
            class="trigger"
            @click="collapsed = !collapsed"
          />
          <a-breadcrumb class="header-breadcrumb">
            <a-breadcrumb-item><HomeOutlined /></a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <a-button class="api-doc-btn" @click="openApiDoc">
            <template #icon><ApiOutlined /></template>
            API文档
          </a-button>

          <a-badge :count="3" :overflow-count="99" class="header-badge">
            <BellOutlined class="bell-icon" />
          </a-badge>
          
          <a-dropdown>
            <div class="user-dropdown">
              <a-avatar style="background-color: #C8102E" :size="32">
                {{ userStore.username?.charAt(0)?.toUpperCase() }}
              </a-avatar>
              <span class="user-name">{{ userStore.username }}</span>
              <DownOutlined class="dropdown-arrow" />
            </div>
            
            <template #overlay>
              <a-menu @click="handleUserMenu">
                <a-menu-item key="profile">
                  <UserOutlined /> 个人中心
                </a-menu-item>
                <a-menu-item key="settings">
                  <SettingOutlined /> 安全设置
                </a-menu-item>
                <a-menu-item key="badge" class="menu-badge-item" disabled>
                  <CrownOutlined class="crown-icon" /> ICBC尊享
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" danger>
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <!-- 内容区域 -->
      <a-layout-content class="layout-content">
        <div class="content-bg-pattern"></div>
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  HomeOutlined, IdcardOutlined, TransactionOutlined, OrderedListOutlined,
  FileTextOutlined, CustomerServiceOutlined, BellOutlined,
  UserOutlined, SettingOutlined, LogoutOutlined, DownOutlined,
  MenuFoldOutlined, MenuUnfoldOutlined, ApiOutlined,
  DashboardOutlined, DollarCircleOutlined, SafetyCertificateOutlined,
  CrownOutlined, CreditCardOutlined, TeamOutlined, FundOutlined, AuditOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)

function getRouteKey() {
  const name = route.name
  const map = {
    Dashboard: 'dashboard', Accounts: 'accounts', Transfer: 'transfer',
    Loan: 'loan', History: 'history', Bills: 'bills', AiChat: 'ai-chat',
    Profile: 'profile', AdminDashboard: 'admin/dashboard', AdminUsers: 'admin/users',
    AdminAccounts: 'admin/accounts', AdminLoans: 'admin/loans', AdminChecks: 'admin/checks'
  }
  return map[name] || 'dashboard'
}

const selectedKeys = ref([getRouteKey()])
const siderWidth = ref(240)

// ---- 侧边栏拖拽 ----
const isResizing = ref(false)

function startResize(e) {
  isResizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

function onResize(e) {
  if (!isResizing.value) return
  const newWidth = Math.min(400, Math.max(200, e.clientX))
  siderWidth.value = newWidth
}

function stopResize() {
  isResizing.value = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})

const currentTitle = computed(() => {
  const titles = {
    dashboard: '首页概览',
    accounts: '我的账户',
    transfer: '转账汇款',
    loan: '信用卡借款',
    history: '交易记录',
    bills: '我的账单',
    'ai-chat': 'AI智能客服',
    profile: '个人中心',
    'admin/dashboard': '数据大屏',
    'admin/users': '用户管理',
    'admin/accounts': '账户管理',
    'admin/loans': '借款管理',
    'admin/checks': '支票管理'
  }
  return titles[selectedKeys.value[0]] || '首页'
})

function handleMenuClick({ key }) {
  selectedKeys.value = [key]
  router.push(`/${key}`)
}

function handleUserMenu({ key }) {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (key === 'profile') {
    router.push('/profile')
  }
}

function openApiDoc() {
  window.open('http://localhost:8080/swagger-ui.html', '_blank')
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.main-layout {
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* ===== 侧边栏 - 灰色龙图腾背景 ===== */
.layout-sider {
  background: 
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 260 900'%3E%3Cg fill='none' stroke='%23888' stroke-width='.5' opacity='.06'%3E%3Cpath d='M130 50c15-10 30-5 40 10s10 30 0 45s-25 20-40 15s-25-15-20-30s20-25 35-20' /%3E%3Cpath d='M100 120c-20 5-35 20-30 40s20 30 40 25s25-20 20-35s-15-25-30-20' /%3E%3Cpath d='M160 100c10-15 30-20 45-10s15 30 5 45s-30 20-45 10s-10-30 5-40' /%3E%3Ccircle cx='130' cy='180' r='2' /%3E%3Ccircle cx='145' cy='195' r='1.5' /%3E%3Ccircle cx='115' cy='200' r='1' /%3E%3Cpath d='M80 250c-5 20 10 40 30 45s35-10 30-30s-20-35-35-25s-20 25-10 35' /%3E%3Cpath d='M150 260c15 10 35 5 45-10s0-35-15-40s-30 5-35 20s0 30 15 25' /%3E%3Cpath d='M120 350c-10 15-5 35 10 45s30 10 40-5s5-30-15-35s-25 5-20 20' /%3E%3Cpath d='M90 400c-15 5-25 20-20 35s20 25 35 15s15-25 0-35s-20-15-30-5' /%3E%3Cpath d='M160 410c5-15 20-25 35-20s20 20 15 35s-25 20-35 10s-10-25 5-30' /%3E%3Ccircle cx='130' cy='500' r='3'/%3E%3Ccircle cx='115' cy='520' r='2'/%3E%3Ccircle cx='148' cy='515' r='1.5'/%3E%3Cpath d='M70 580c-10 25 15 50 40 45s30-25 10-45s-30-25-45-5s-10 30 15 35' /%3E%3Cpath d='M170 590c15 15 35 10 45-5s5-35-15-40s-30 5-30 20s5 25 20 20' /%3E%3Cpath d='M100 680c-5 20 15 40 35 35s25-20 10-35s-25-20-35-5s-5 20 15 25' /%3E%3Cpath d='M150 690c10 15 30 15 40 0s5-30-15-35s-30 0-25 15s0 25 15 20' /%3E%3Cpath d='M130 780c-15 5-25 25-15 40s30 20 40 5s5-30-15-35s-25 10-15 25' /%3E%3Cpath d='M95 850c15 10 35 5 40-10s-5-30-20-35s-30 5-30 20s5 25 20 15' /%3E%3C/text%3E%3C/g%3E%3Cg stroke='%23999' stroke-width='.4' opacity='.04'%3E%3Cpath d='M30 60h200'/%3E%3Cpath d='M30 80h200'/%3E%3Cpath d='M50 140h160'/%3E%3Cpath d='M40 300h180'/%3E%3Cpath d='M45 440h170'/%3E%3Cpath d='M35 550h190'/%3E%3Cpath d='M40 720h180'/%3E%3Cpath d='M50 860h160'/%3E%3C/g%3E%3C/svg%3E") repeat-y center,
    #EEF0F3;
  border-right: 1px solid #E2E4E8;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);
  z-index: 20;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* Logo */
.logo-container {
  display: flex;
  align-items: center;
  padding: 20px 20px;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;
  min-height: 72px;
  position: relative;
  overflow: hidden;
}

.logo-container::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20px;
  right: 20px;
  height: 2px;
  background: #EBEEF5;
}

/* Logo 金色光晕 hover 动画 */
.logo-icon {
  width: 40px;
  height: 40px;
  background: #C8102E;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  transition: all .25s ease;
  cursor: pointer;
}

.logo-icon:hover {
  opacity: 0.9;
  box-shadow: 0 2px 6px rgba(0,0,0,.12);
}

@keyframes pulse {
  0% { transform: scale(1); }
  40% { transform: scale(1.08); }
  100% { transform: scale(1); }
}

.logo-text {
  overflow: hidden;
}

.logo-title {
  font-size: 18px;
  font-weight: 800;
  color: #C8102E;
  letter-spacing: 2px;
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 11px;
  color: #8c8c8c;
  white-space: nowrap;
  letter-spacing: 1px;
}

/* 导航菜单 - 左侧滑动指示条 */
.nav-menu {
  border-right: none;
  padding: 12px 10px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-menu :deep(.ant-menu-item) {
  border-radius: 8px;
  margin: 2px 0;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 左侧红色指示条 - hover */
.nav-menu :deep(.ant-menu-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 24px;
  background: #C8102E;
  border-radius: 0 3px 3px 0;
  transition: width .25s ease;
}

.nav-menu :deep(.ant-menu-item:hover) {
  background-color: #FFF1F0;
}

.nav-menu :deep(.ant-menu-item:hover::before) {
  width: 3px;
}

/* 左侧红色指示条 - selected */
.nav-menu :deep(.ant-menu-item-selected) {
  background: #FFF1F0;
  font-weight: 600;
}

.nav-menu :deep(.ant-menu-item-selected::before) {
  width: 3px;
}

.nav-menu :deep(.ant-menu-item .anticon) {
  font-size: 18px;
}

/* 侧边栏底部品牌语 */
.sider-footer {
  padding: 16px 20px;
  border-top: 1px solid #E2E4E8;
  background: #EAECEF;
  text-align: center;
}

/* 金句 - 毛笔书法体，小号 */
.footer-motto {
  font-family: var(--font-calligraphy);
  font-size: 15px;
  color: #C4A265;
  letter-spacing: 3px;
  margin-bottom: 10px;
  white-space: nowrap;
  opacity: 0.85;
}

.footer-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 10px;
  color: #aaa;
  white-space: nowrap;
}

.footer-icon {
  font-size: 12px;
  color: #C8102E;
  opacity: 0.5;
}

/* ===== 拖拽手柄 ===== */
.resize-handle {
  position: fixed;
  top: 0;
  bottom: 0;
  width: 6px;
  cursor: col-resize;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.resize-handle:hover,
.resize-handle:active {
  background: rgba(200, 16, 46, 0.08);
}

.resize-grip {
  display: flex;
  flex-direction: column;
  gap: 3px;
  opacity: 0;
  transition: opacity 0.2s;
}

.resize-handle:hover .resize-grip {
  opacity: 1;
}

.resize-grip span {
  width: 2px;
  height: 2px;
  border-radius: 50%;
  background: #C8102E;
  opacity: 0.5;
}

/* ===== 顶部导航 ===== */
.layout-header {
  background: #ffffff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  height: 56px;
  line-height: 56px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  color: #595959;
  transition: color 0.3s;
}

.trigger:hover {
  color: #C8102E;
}

.header-breadcrumb {
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* API文档按钮 - ICBC红边框 outline */
.api-doc-btn {
  color: #C8102E !important;
  font-size: 13px;
  border-radius: 6px;
  height: 32px;
  border: 1.5px solid #C8102E !important;
  background: transparent !important;
  transition: all 0.3s;
  font-weight: 500;
}

.api-doc-btn:hover {
  color: #FFF !important;
  background: #C8102E !important;
  border-color: #C8102E !important;
}

/* 消息铃铛 Badge - 数字跳动 */
.header-badge {
  cursor: pointer;
}

.bell-icon {
  font-size: 18px;
  color: #595959;
  transition: color .3s;
}
.header-badge:hover .bell-icon {
  color: #C8102E;
}

.header-badge :deep(.ant-badge-count) {
  animation: badgeBounce 2s ease-in-out infinite;
  box-shadow: 0 0 0 2px #fff;
}

@keyframes badgeBounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-name {
  font-size: 14px;
  color: #1f1f1f;
}

.dropdown-arrow {
  font-size: 12px;
  color: #8c8c8c;
}

/* 用户下拉 - ICBC尊享金色徽章 */
.menu-badge-item {
  cursor: default !important;
  pointer-events: none;
}
.menu-badge-item :deep(.ant-menu-title-content) {
  display: flex;
  align-items: center;
  gap: 6px;
}
.crown-icon {
  color: #C4A265;
  font-size: 14px;
}

/* ===== 内容区 ===== */
.layout-content {
  position: relative;
  padding: 24px;
  overflow-y: auto;
  height: calc(100vh - 56px);
  background: #F5F6FA;
}

/* 背景纹理 */
.content-bg-pattern {
  display: none;
}

/* 页面过渡动画 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
