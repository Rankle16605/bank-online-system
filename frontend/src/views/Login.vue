<template>
  <div class="login-page">
    <!-- 左侧 ICBC 品牌区 - 纯红背景 -->
    <div class="login-brand">
      <div class="brand-inner">
        <!-- 装饰线条 -->
        <div class="brand-ornament"></div>

        <!-- Logo — 空心铜钱·招财进宝 -->
        <div class="brand-logo animate-fadeInUp">
          <div class="brand-logo-icon coin-wrapper">
            <div class="coin-outer-ring"></div>
            <div class="coin-inner-hole"></div>
            <span class="coin-char coin-t">招</span>
            <span class="coin-char coin-b">宝</span>
            <span class="coin-char coin-l">财</span>
            <span class="coin-char coin-r">进</span>
          </div>
        </div>

        <!-- 标题 -->
        <h1 class="brand-system-name animate-fadeInUp delay-1">ICBC智能在线银行</h1>

        <!-- 金色金句 - 毛笔书法体，单行带标点 -->
        <p class="brand-motto animate-fadeInUp delay-2">工于至诚，行以致远</p>

        <!-- 分隔线 -->
        <div class="brand-line animate-fadeInUp delay-2"></div>

        <!-- 特色 -->
        <div class="brand-features">
          <div class="feature-item animate-fadeInUp delay-2">
            <div class="feature-icon"><SafetyCertificateOutlined /></div>
            <span>银行级安全加密</span>
          </div>
          <div class="feature-item animate-fadeInUp delay-3">
            <div class="feature-icon"><ThunderboltOutlined /></div>
            <span>毫秒级极速交易</span>
          </div>
          <div class="feature-item animate-fadeInUp delay-4">
            <div class="feature-icon"><CustomerServiceOutlined /></div>
            <span>小工AI · 7×24小时在线</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-area">
      <div class="login-card animate-zoomIn">
        <div class="card-header">
          <h2>欢迎登录</h2>
          <p>登录账户，体验智能金融服务</p>
        </div>

        <a-form
          :model="formState"
          :rules="rules"
          layout="vertical"
          @finish="handleLogin"
          autocomplete="off"
        >
          <a-form-item name="username">
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="请输入用户名"
              class="icbc-input"
            >
              <template #prefix><UserOutlined class="prefix-icon" /></template>
            </a-input>
          </a-form-item>

          <a-form-item name="password">
            <a-input-password
              v-model:value="formState.password"
              size="large"
              placeholder="请输入密码"
              class="icbc-input"
            >
              <template #prefix><LockOutlined class="prefix-icon" /></template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="login-btn"
            >
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="card-footer">
          <span>还没有账户？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>

    <!-- 答辩演示跳转按钮 -->
    <a href="/presentation/ProjectPresentation.html" target="_blank" class="demo-entry">
      <span class="demo-icon"><FundProjectionScreenOutlined /></span>
      <span class="demo-text">答辩演示</span>
    </a>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { message } from 'ant-design-vue'
import {
  UserOutlined, LockOutlined,
  SafetyCertificateOutlined, ThunderboltOutlined, CustomerServiceOutlined,
  FundProjectionScreenOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  loading.value = true
  try {
    await userStore.doLogin({
      username: formState.username,
      password: formState.password
    })
    message.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
  font-family: var(--font-family);
}

/* ===== 左侧品牌区 - 纯色ICBC红 ===== */
.login-brand {
  flex: 1;
  background: var(--icbc-red);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-inner {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #ffffff;
  padding: 60px 48px;
  max-width: 440px;
}

/* 顶部装饰线 */
.brand-ornament {
  width: 40px;
  height: 2px;
  background: var(--icbc-gold);
  margin: 0 auto 40px;
  opacity: 0.7;
}

/* Logo — 空心铜钱·招财进宝 */
.brand-logo {
  margin-bottom: 32px;
}
.coin-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  /* 空心金环 */
  background: radial-gradient(circle, transparent 48%, #F5E6A0 51%, #D4B87A 60%, #C4A265 72%, #B8934E 85%, #8B6914 100%);
  box-shadow: 0 4px 24px rgba(0,0,0,.25), 0 0 0 2px rgba(255,215,0,.15);
  animation: coinSpinSlow 20s linear infinite;
}
/* 内方孔 */
.coin-inner-hole {
  width: 22px; height: 22px;
  border: 1.5px solid #B8934E;
  border-radius: 2px;
  background: #C8102E;
  z-index: 2;
}
/* 四个字 */
.coin-char {
  position: absolute;
  font-family: 'STKaiti','KaiTi','STSong',serif;
  font-size: 13px;
  font-weight: 700;
  color: #3D1C00;
  z-index: 3;
  text-shadow: 0 1px 1px rgba(255,255,255,.3);
}
.coin-t { top: 16px; }
.coin-b { bottom: 16px; }
.coin-l { left: 20px; }
.coin-r { right: 20px; }

@keyframes coinSpinSlow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 系统名称 */
.brand-system-name {
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 4px;
  margin-bottom: 24px;
  color: #fff;
}

/* === 金句 - 毛笔书法体，单行 === */
.brand-motto {
  font-family: var(--font-calligraphy);
  font-size: 22px;
  font-weight: 400;
  color: var(--icbc-gold);
  letter-spacing: 5px;
  margin-bottom: 28px;
  line-height: 1.4;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
}

/* 分隔线 */
.brand-line {
  width: 48px;
  height: 1px;
  background: var(--icbc-gold);
  margin: 0 auto 36px;
  opacity: 0.5;
}

/* 特色列表 */
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: background var(--transition-base);
}
.feature-item:hover {
  background: rgba(255, 255, 255, 0.14);
}
.feature-icon {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

/* ===== 右侧表单区 ===== */
.login-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 40px;
}

.login-card {
  width: 400px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: 44px 40px;
  box-shadow: var(--shadow-lg);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}
.card-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.card-header p {
  color: var(--text-tertiary);
  font-size: 14px;
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
  box-shadow: 0 0 0 2px rgba(200, 16, 46, 0.1);
}
.prefix-icon {
  color: var(--text-disabled);
  transition: color var(--transition-base);
}
.icbc-input :deep(.ant-input-affix-wrapper:focus-within) .prefix-icon,
.icbc-input :deep(.ant-input-affix-wrapper-focused) .prefix-icon {
  color: var(--icbc-red);
}

/* 登录按钮 - 纯红色、无渐变 */
.login-btn {
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  letter-spacing: 6px;
  border: none;
  background: var(--icbc-red) !important;
  box-shadow: 0 2px 6px rgba(200, 16, 46, 0.2);
  transition: all var(--transition-base);
  margin-top: 4px;
}
.login-btn:hover {
  background: var(--icbc-red-dark) !important;
  box-shadow: 0 4px 12px rgba(200, 16, 46, 0.3);
}

/* 底部 */
.card-footer {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 14px;
  margin-top: 8px;
}
.register-link {
  color: var(--icbc-red);
  margin-left: 4px;
  font-weight: 500;
  transition: opacity var(--transition-base);
}
.register-link:hover {
  opacity: 0.8;
}

/* ===== 答辩演示入口按钮 ===== */
.demo-entry {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--bg-card);
  border: 1px solid var(--icbc-gold);
  border-radius: var(--radius-xl);
  color: var(--icbc-red);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-base);
  backdrop-filter: blur(8px);
}
.demo-entry:hover {
  background: var(--icbc-red);
  color: #fff;
  border-color: var(--icbc-red);
  box-shadow: 0 6px 20px rgba(200, 16, 46, 0.35);
  transform: translateY(-2px);
}
.demo-icon {
  font-size: 18px;
  display: flex;
  align-items: center;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .login-brand {
    display: none;
  }
  .login-form-area {
    padding: 20px;
  }
  .login-card {
    width: 100%;
    max-width: 400px;
    padding: 32px 24px;
  }
  .demo-entry {
    bottom: 16px;
    right: 16px;
    padding: 8px 14px;
    font-size: 13px;
  }
}
</style>
