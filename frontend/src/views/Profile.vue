<template>
  <div class="profile-page">
    <PageHeader title="个人中心" subtitle="管理您的账户信息与安全设置" />

    <a-row :gutter="[24, 24]">
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" class="profile-card card-hover">
          <div class="profile-header">
            <div class="profile-avatar">
              <span class="avatar-text">{{ userStore.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <h3 class="profile-name">{{ userStore.userInfo?.realName || userStore.username }}</h3>
            <div class="profile-badge">
              <CrownOutlined /> 中国工商银行尊贵客户
            </div>
            <p class="profile-phone">{{ userStore.userInfo?.phone }}</p>
          </div>

          <a-divider />

          <a-menu mode="inline" :selected-keys="[activeTab]" @click="({ key }) => activeTab = key" class="profile-menu">
            <a-menu-item key="info">
              <template #icon><UserOutlined /></template>
              基本信息
            </a-menu-item>
            <a-menu-item key="security">
              <template #icon><SafetyCertificateOutlined /></template>
              安全设置
            </a-menu-item>
          </a-menu>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="16">
        <!-- 基本信息 -->
        <a-card v-if="activeTab === 'info'" :bordered="false" class="info-card card-hover">
          <div class="card-title-decorated">基本信息</div>
          <a-descriptions :column="2" bordered size="middle" class="icbc-descriptions">
            <a-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</a-descriptions-item>
            <a-descriptions-item label="真实姓名">{{ userStore.userInfo?.realName }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ userStore.userInfo?.phone }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '未设置' }}</a-descriptions-item>
            <a-descriptions-item label="账户状态">
              <a-tag :color="userStore.userInfo?.status === 1 ? 'green' : 'red'">
                {{ userStore.userInfo?.status === 1 ? '正常' : '已禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="注册时间">{{ userStore.userInfo?.createdAt }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- 安全设置 -->
        <a-card v-if="activeTab === 'security'" :bordered="false" class="security-card card-hover">
          <div class="card-title-decorated">安全设置</div>
          <a-form :model="passwordForm" layout="vertical" @finish="handleChangePassword" class="password-form">
            <a-form-item name="oldPassword" label="原密码">
              <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" class="icbc-input" />
            </a-form-item>
            <a-form-item name="newPassword" label="新密码">
              <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码（6-50个字符）" class="icbc-input" />
            </a-form-item>
            <a-form-item name="confirmPassword" label="确认新密码">
              <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" class="icbc-input" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="changing" class="btn-change-password" size="large">
                <SafetyCertificateOutlined /> 修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import { UserOutlined, SafetyCertificateOutlined, CrownOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const userStore = useUserStore()
const activeTab = ref('info')
const changing = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function handleChangePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次新密码输入不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    message.error('新密码长度至少6个字符')
    return
  }
  changing.value = true
  setTimeout(() => {
    message.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    changing.value = false
  }, 1000)
}
</script>

<style scoped>
.profile-page { max-width: 1400px; }

/* ===== 卡片标题装饰条 ===== */
.card-title-decorated {
  font-size: 16px;
  font-weight: 700;
  color: #1A1A1A;
  margin-bottom: 20px;
  padding-left: 14px;
  position: relative;
}
.card-title-decorated::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #C8102E;
  border-radius: 2px;
}

/* ===== 左侧个人信息卡片 ===== */
.profile-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  transition: box-shadow .3s ease, transform .3s ease;
}
.profile-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.profile-card :deep(.ant-card-body) {
  padding: 24px;
}

.profile-header {
  text-align: center;
  padding: 16px 0;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #C8102E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,.08);
}
.avatar-text {
  color: #FFF;
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 8px;
}

.profile-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #C4A265;
  background: #FFFBF0;
  padding: 4px 14px;
  border-radius: 20px;
  font-weight: 500;
  margin-bottom: 12px;
  border: 1px solid rgba(196,162,101,.25);
}

.profile-phone {
  color: #999;
  font-size: 13px;
  margin: 0;
}

/* 左侧菜单 */
.profile-menu {
  border-right: none;
}
.profile-menu :deep(.ant-menu-item) {
  border-radius: 8px;
  margin: 4px 0;
  height: 42px;
  line-height: 42px;
  transition: all .3s ease;
  font-size: 14px;
}
.profile-menu :deep(.ant-menu-item:hover) {
  background-color: #FFF1F0;
  color: #C8102E;
}
.profile-menu :deep(.ant-menu-item-selected) {
  background: #FFF1F0 !important;
  color: #C8102E !important;
  font-weight: 600;
}
.profile-menu :deep(.ant-menu-item .anticon) {
  font-size: 18px;
}

/* ===== 右侧信息卡片 ===== */
.info-card, .security-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  transition: box-shadow .3s ease, transform .3s ease;
}
.info-card:hover, .security-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.info-card :deep(.ant-card-body), .security-card :deep(.ant-card-body) {
  padding: 24px;
}

/* Descriptions 表格美化 */
.icbc-descriptions :deep(.ant-descriptions-item-label) {
  background: #FAFAFA !important;
  font-weight: 500;
  color: #666;
}
.icbc-descriptions :deep(.ant-descriptions-item-content) {
  background: #FFF;
  color: #1A1A1A;
}

/* ===== 密码表单 ===== */
.password-form {
  max-width: 460px;
}
.password-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #1A1A1A;
}

.icbc-input {
  border-radius: 8px;
}
.icbc-input:deep(.ant-input),
.icbc-input:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
  transition: all .3s ease;
}
.icbc-input:deep(.ant-input-affix-wrapper:focus),
.icbc-input:deep(.ant-input-affix-wrapper-focused) {
  border-color: #C8102E;
  box-shadow: 0 0 0 2px rgba(200,16,46,.12);
}

/* 修改密码按钮 */
.btn-change-password {
  border-radius: 8px !important;
  font-weight: 600 !important;
  font-size: 15px !important;
  height: 44px !important;
  background: #C8102E !important;
  border: none !important;
  box-shadow: 0 2px 6px rgba(0,0,0,.08) !important;
  transition: all .25s ease !important;
}
.btn-change-password:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.12) !important;
}

/* ===== 动画 ===== */
.animate-fadeInUp {
  animation: fadeInUp .5s ease both;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式 */
@media (max-width: 992px) {
  .password-form {
    max-width: 100%;
  }
}
</style>
