<template>
  <div class="accounts-page">
    <PageHeader title="我的账户" subtitle="管理您的银行账户">
      <template #extra>
        <a-button type="primary" @click="showCreateModal = true">
          <PlusOutlined /> 开设新账户
        </a-button>
      </template>
    </PageHeader>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]">
        <a-col v-for="account in accounts" :key="account.id" :xs="24" :sm="12" :lg="8">
          <div
            class="bank-card"
            :class="account.accountType === 1 ? 'bank-card--savings' : 'bank-card--credit'"
            @click="viewDetail(account)"
          >
            <!-- ICBC水印装饰 -->
            <div class="bank-card__watermark">
              <div class="watermark-ring watermark-ring--1"></div>
              <div class="watermark-ring watermark-ring--2"></div>
            </div>

            <!-- 卡片内容 -->
            <div class="bank-card__content">
              <!-- 顶部：类型 + 状态 -->
              <div class="bank-card__header">
                <div class="bank-card__type">
                  <WalletOutlined class="bank-card__type-icon" />
                  <span class="bank-card__type-name">{{ account.accountTypeName }}</span>
                </div>
                <a-tag
                  :color="account.status === 1 ? 'success' : 'error'"
                  class="bank-card__status"
                >
                  {{ account.status === 1 ? '正常' : '冻结' }}
                </a-tag>
              </div>

              <!-- 中间：脱敏卡号 -->
              <div class="bank-card__number">
                {{ maskAccountNo(account.accountNo) }}
              </div>

              <!-- 底部：余额信息 -->
              <div class="bank-card__balance">
                <div class="bank-card__balance-item">
                  <span class="bank-card__balance-label">可用余额</span>
                  <span class="bank-card__balance-value animate-countUp">
                    ¥{{ formatMoney(account.availableBalance) }}
                  </span>
                </div>
                <div v-if="account.frozenAmount > 0" class="bank-card__balance-item">
                  <span class="bank-card__balance-label">冻结金额</span>
                  <span class="bank-card__balance-value bank-card__balance-value--frozen">
                    ¥{{ formatMoney(account.frozenAmount) }}
                  </span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="bank-card__divider"></div>
              <div class="bank-card__actions">
                <a-button type="link" class="bank-card__action-btn" @click.stop="viewDetail(account)">
                  <EyeOutlined /> 详情
                </a-button>
                <a-button type="link" class="bank-card__action-btn" @click.stop="$router.push('/history')">
                  <UnorderedListOutlined /> 流水
                </a-button>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-spin>

    <a-empty v-if="!loading && accounts.length === 0" description="暂无账户，请先开设账户" />

    <!-- 创建账户弹窗 -->
    <a-modal
      v-model:open="showCreateModal"
      title="开设新账户"
      @ok="handleCreateAccount"
      :confirm-loading="creating"
      ok-text="确认开户"
      cancel-text="取消"
      :width="480"
      wrap-class-name="icbc-modal"
    >
      <a-form layout="vertical">
        <a-form-item label="选择账户类型">
          <a-radio-group v-model:value="createAccountType" class="account-type-radio">
            <div
              class="account-type-option"
              :class="{ 'account-type-option--active': createAccountType === 1 }"
              @click="createAccountType = 1"
            >
              <div class="account-type-option__preview account-type-option__preview--savings">
                <WalletOutlined />
                <span>储蓄账户</span>
              </div>
              <a-radio :value="1" class="sr-only">储蓄账户</a-radio>
            </div>
            <div
              class="account-type-option"
              :class="{ 'account-type-option--active': createAccountType === 2 }"
              @click="createAccountType = 2"
            >
              <div class="account-type-option__preview account-type-option__preview--credit">
                <WalletOutlined />
                <span>信用账户</span>
              </div>
              <a-radio :value="2" class="sr-only">信用账户</a-radio>
            </div>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 账户详情弹窗 -->
    <a-modal
      v-model:open="showDetailModal"
      title="账户详情"
      :footer="null"
      width="520px"
      wrap-class-name="icbc-modal"
    >
      <a-descriptions v-if="selectedAccount" :column="1" bordered size="middle" class="detail-descriptions">
        <a-descriptions-item label="账户号">
          <span class="detail-mono">{{ selectedAccount.accountNo }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="账户类型">
          {{ selectedAccount.accountTypeName }}
        </a-descriptions-item>
        <a-descriptions-item label="余额">
          ¥{{ formatMoney(selectedAccount.balance) }}
        </a-descriptions-item>
        <a-descriptions-item label="冻结金额">
          ¥{{ formatMoney(selectedAccount.frozenAmount) }}
        </a-descriptions-item>
        <a-descriptions-item label="可用余额">
          <span class="detail-available">
            ¥{{ formatMoney(selectedAccount.availableBalance) }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="selectedAccount.status === 1 ? 'success' : 'error'">
            {{ selectedAccount.status === 1 ? '正常' : '冻结' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="开户时间">
          {{ selectedAccount.createdAt }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAccounts, createAccount } from '@/api/index'
import { message } from 'ant-design-vue'
import {
  WalletOutlined, PlusOutlined, EyeOutlined, UnorderedListOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const accounts = ref([])
const showCreateModal = ref(false)
const showDetailModal = ref(false)
const creating = ref(false)
const createAccountType = ref(1)
const selectedAccount = ref(null)

onMounted(async () => {
  await fetchAccounts()
})

async function fetchAccounts() {
  loading.value = true
  try {
    const res = await getAccounts()
    accounts.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleCreateAccount() {
  creating.value = true
  try {
    await createAccount(createAccountType.value)
    message.success('开户成功')
    showCreateModal.value = false
    await fetchAccounts()
  } finally {
    creating.value = false
  }
}

function viewDetail(account) {
  selectedAccount.value = account
  showDetailModal.value = true
}

function maskAccountNo(no) {
  if (!no) return ''
  return no.substring(0, 4) + ' **** **** ' + no.substring(no.length - 4)
}

function formatMoney(val) {
  const num = parseFloat(val) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.accounts-page {
  max-width: 1400px;
}

/* ===== 银行卡样式 ===== */
.bank-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
  min-height: 220px;
}

.bank-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, .06);
}

/* 储蓄账户：ICBC深红 */
.bank-card--savings {
  background: #9B0023;
  color: #fff;
}

.bank-card--savings:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.12);
}

/* 信用账户：深金 */
.bank-card--credit {
  background: #8B6914;
  color: #fff;
}

.bank-card--credit:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.12);
}

/* ===== ICBC水印装饰 ===== */
.bank-card__watermark {
  position: absolute;
  top: 0;
  right: 0;
  width: 180px;
  height: 180px;
  pointer-events: none;
  opacity: .08;
}

.watermark-ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid #fff;
}

.watermark-ring--1 {
  width: 160px;
  height: 160px;
  top: -40px;
  right: -40px;
}

.watermark-ring--2 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -60px;
}

/* ===== 卡片内容 ===== */
.bank-card__content {
  position: relative;
  z-index: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 220px;
}

/* 顶部：类型 + 状态 */
.bank-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.bank-card__type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bank-card__type-icon {
  font-size: 18px;
  opacity: .9;
}

.bank-card__type-name {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: .5px;
}

.bank-card__status {
  border: 1px solid rgba(255, 255, 255, .3);
  background: rgba(255, 255, 255, .15) !important;
  color: #fff !important;
  font-size: 12px;
  border-radius: 6px;
}

/* 卡号 */
.bank-card__number {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
  font-family: 'Courier New', 'PingFang SC', 'Microsoft YaHei', monospace;
  margin-bottom: 20px;
  opacity: .95;
}

/* 余额区域 */
.bank-card__balance {
  display: flex;
  gap: 20px;
  margin-bottom: auto;
}

.bank-card__balance-item {
  flex: 1;
}

.bank-card__balance-label {
  display: block;
  font-size: 11px;
  opacity: .7;
  margin-bottom: 4px;
  letter-spacing: .3px;
}

.bank-card__balance-value {
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.bank-card__balance-value--frozen {
  opacity: .75;
  font-size: 16px;
  font-weight: 500;
}

/* 分割线 */
.bank-card__divider {
  height: 1px;
  background: rgba(255, 255, 255, .2);
  margin: 14px 0 10px;
}

/* 操作按钮 */
.bank-card__actions {
  display: flex;
  gap: 4px;
}

.bank-card__action-btn {
  color: rgba(255, 255, 255, .85) !important;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all .2s;
}

.bank-card__action-btn:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, .12) !important;
}

/* ===== 创建账户弹窗 - 卡片预览 ===== */
.account-type-radio {
  display: flex !important;
  gap: 16px;
  width: 100%;
}

.account-type-option {
  flex: 1;
  cursor: pointer;
  border-radius: 10px;
  border: 2px solid #f0f0f0;
  overflow: hidden;
  transition: all .25s;
}

.account-type-option:hover {
  border-color: #d9d9d9;
}

.account-type-option--active {
  border-color: #C8102E !important;
  box-shadow: 0 0 0 3px rgba(200, 16, 46, .1);
}

.account-type-option__preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.account-type-option__preview--savings {
  background: #9B0023;
}

.account-type-option__preview--credit {
  background: #8B6914;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

/* ===== 详情弹窗 ===== */
.detail-descriptions :deep(.ant-descriptions-item-label) {
  background: #FAFAFA;
  font-weight: 500;
  color: #666;
  width: 120px;
}

.detail-mono {
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.detail-available {
  font-weight: 700;
  color: #C8102E;
  font-size: 15px;
}

/* ===== ICBC Modal 全局 ===== */
:deep(.icbc-modal .ant-modal-header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px 24px 16px;
}

:deep(.icbc-modal .ant-modal-title) {
  font-size: 17px;
  font-weight: 600;
  color: #1A1A1A;
}

:deep(.icbc-modal .ant-modal-body) {
  padding: 24px;
}

:deep(.icbc-modal .ant-modal-footer) {
  padding: 12px 24px 20px;
  border-top: 1px solid #f0f0f0;
}

/* ===== 动画 ===== */
.animate-countUp {
  animation: countUp 0.6s ease-out;
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
