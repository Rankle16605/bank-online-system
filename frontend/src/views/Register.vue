<template>
  <div class="register-page">
    <div class="register-card animate-zoomIn">
      <!-- 顶部 ICBC 品牌标识 -->
      <div class="register-header">
        <div class="header-logo coin-wrapper">
          <div class="coin-outer-ring"></div>
          <div class="coin-inner-hole"></div>
          <span class="coin-char coin-t">招</span>
          <span class="coin-char coin-b">宝</span>
          <span class="coin-char coin-l">财</span>
          <span class="coin-char coin-r">进</span>
        </div>
        <h2>注册ICBC智能在线银行</h2>
        <p>创建您的账户，开启智能金融服务之旅</p>
      </div>

      <a-form
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleRegister"
        class="register-form"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item name="username" label="用户名">
              <a-input
                v-model:value="formState.username"
                placeholder="3-50个字符"
                class="icbc-input"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="realName" label="真实姓名">
              <a-input
                v-model:value="formState.realName"
                placeholder="请输入真实姓名"
                class="icbc-input"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item name="password" label="密码">
              <a-input-password
                v-model:value="formState.password"
                placeholder="6-50个字符"
                class="icbc-input"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="confirmPassword" label="确认密码">
              <a-input-password
                v-model:value="formState.confirmPassword"
                placeholder="再次输入密码"
                class="icbc-input"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item name="idCard" label="身份证号">
          <a-input
            v-model:value="formState.idCard"
            placeholder="18位身份证号码"
            maxlength="18"
            class="icbc-input"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item name="phone" label="手机号">
              <a-input
                v-model:value="formState.phone"
                placeholder="11位手机号码"
                maxlength="11"
                class="icbc-input"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item name="email" label="邮箱（选填）">
              <a-input
                v-model:value="formState.email"
                placeholder="请输入邮箱地址"
                class="icbc-input"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
            class="register-btn"
          >
            注 册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-link">
        已有账户？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  idCard: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value) => {
  if (value !== formState.password) {
    return Promise.reject('两次密码输入不一致')
  }
  return Promise.resolve()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在6-50之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function handleRegister() {
  loading.value = true
  try {
    const { confirmPassword, ...data } = formState
    await userStore.doRegister(data)
    message.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.register-page {
  min-height: 100vh;
  background: #F5F6FA;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  font-family: var(--font-family);
}

/* ===== 卡片容器 ===== */
.register-card {
  width: 620px;
  max-width: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: 48px;
  box-shadow: var(--shadow-lg);
}

/* ===== 顶部品牌标识 — 空心铜钱·招财进宝 ===== */
.coin-wrapper {
  width: 90px;
  height: 90px;
  margin: 0 auto 20px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: radial-gradient(circle, transparent 46%, #F5E6A0 49%, #D4B87A 58%, #C4A265 70%, #B8934E 83%, #8B6914 100%);
  box-shadow: 0 3px 20px rgba(0,0,0,.12), 0 0 0 2px rgba(180,140,60,.2);
  animation: coinSpinSlow 20s linear infinite;
}
.coin-inner-hole {
  width: 20px; height: 20px;
  border: 1.5px solid #B8934E;
  border-radius: 2px;
  background: #fff;
  z-index: 2;
}
.coin-char {
  position: absolute;
  font-family: 'STKaiti','KaiTi','STSong',serif;
  font-size: 12px;
  font-weight: 700;
  color: #3D1C00;
  z-index: 3;
  text-shadow: 0 1px 1px rgba(255,255,255,.3);
}
.coin-t { top: 13px; }
.coin-b { bottom: 13px; }
.coin-l { left: 18px; }
.coin-r { right: 18px; }

@keyframes coinSpinSlow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.register-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.register-header p {
  color: var(--text-tertiary);
  font-size: 14px;
}

/* ===== 表单 ===== */
.register-form {
  margin-top: 8px;
}

/* 表单标签 */
.register-form :deep(.ant-form-item-label > label) {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
}

/* 输入框 */
.icbc-input :deep(.ant-input),
.icbc-input :deep(.ant-input-affix-wrapper) {
  border-radius: var(--radius-md);
  border-color: var(--border-light);
  transition: all var(--transition-base);
}
.icbc-input :deep(.ant-input:hover),
.icbc-input :deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--icbc-red);
}
.icbc-input :deep(.ant-input:focus),
.icbc-input :deep(.ant-input-affix-wrapper:focus),
.icbc-input :deep(.ant-input-affix-wrapper-focused) {
  border-color: var(--icbc-red);
  box-shadow: 0 0 0 3px rgba(200, 16, 46, 0.1);
}

/* ===== 注册按钮 ===== */
.register-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  letter-spacing: 6px;
  border: none;
  background: #C8102E !important;
  box-shadow: 0 2px 6px rgba(0,0,0,.08);
  transition: all var(--transition-base);
  margin-top: 4px;
}
.register-btn:hover {
  background: #9B0023 !important;
  box-shadow: 0 2px 8px rgba(0,0,0,.12);
}
.register-btn:active {
  transform: translateY(0);
}
.register-btn :deep(.ant-btn-loading-icon) {
  color: #fff;
}

/* ===== 底部链接 ===== */
.login-link {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
  margin-top: 8px;
}
.login-link a {
  color: var(--icbc-red);
  font-weight: 600;
  margin-left: 4px;
  transition: color var(--transition-base);
}
.login-link a:hover {
  color: var(--icbc-red-dark);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .register-page {
    padding: 20px;
  }
  .register-card {
    padding: 32px 24px;
    box-shadow: none;
  }
}
</style>
