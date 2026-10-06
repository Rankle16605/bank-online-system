<template>
  <div class="transfer-page">
    <PageHeader title="转账汇款" subtitle="安全快捷的资金转移服务" />

    <a-row :gutter="[24, 24]">
      <!-- 左侧：转账表单 -->
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" class="transfer-card">
          <template #title>
            <div class="transfer-card__title">
              <span class="transfer-card__title-bar"></span>
              <span>转账信息</span>
            </div>
          </template>

          <a-form
            :model="formState"
            :rules="rules"
            layout="vertical"
            @finish="handleTransfer"
            class="transfer-form"
          >
            <!-- 转出账户 -->
            <a-form-item name="fromAccount" label="转出账户">
              <a-select
                v-model:value="formState.fromAccount"
                placeholder="请选择转出账户"
                size="large"
                class="transfer-select"
              >
                <a-select-option
                  v-for="acc in accounts"
                  :key="acc.accountNo"
                  :value="acc.accountNo"
                >
                  <span class="select-option__prefix">ICBC</span>
                  {{ maskAccountNo(acc.accountNo) }} — ¥{{ formatMoney(acc.availableBalance) }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <!-- 转入账户 -->
            <a-form-item name="toAccount" label="转入账户">
              <a-input
                v-model:value="formState.toAccount"
                size="large"
                placeholder="请输入转入账户号"
              >
                <template #prefix>
                  <BankOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>

            <!-- 转账金额 -->
            <a-form-item name="amount" label="转账金额">
              <a-input-number
                v-model:value="formState.amount"
                size="large"
                :min="0.01"
                :max="50000"
                :precision="2"
                style="width: 100%"
                placeholder="请输入转账金额（单笔限额50,000元）"
                class="transfer-amount"
              >
                <template #prefix>
                  <span class="amount-prefix">¥</span>
                </template>
              </a-input-number>
            </a-form-item>

            <!-- 附言 -->
            <a-form-item name="remark" label="附言（选填）">
              <a-textarea
                v-model:value="formState.remark"
                :rows="2"
                placeholder="转账备注信息"
              />
            </a-form-item>

            <!-- 按钮 -->
            <a-form-item>
              <div class="transfer-form__actions">
                <a-button
                  type="primary"
                  html-type="submit"
                  size="large"
                  :loading="transferring"
                  class="btn-transfer"
                  block
                >
                  确认转账
                </a-button>
                <a-button
                  size="large"
                  @click="resetForm"
                  class="btn-reset"
                  block
                >
                  重置
                </a-button>
              </div>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <!-- 右侧：安全提示 -->
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" class="tips-card">
          <template #title>
            <div class="tips-card__title">
              <SafetyCertificateOutlined class="tips-card__title-icon" />
              <span>安全提示</span>
            </div>
          </template>

          <div class="tips-list">
            <div class="tips-item">
              <div class="tips-item__icon tips-item__icon--red">
                <InfoCircleOutlined />
              </div>
              <div class="tips-item__text">
                <span class="tips-item__label">单笔转账限额</span>
                <span class="tips-item__value tips-item__value--red">¥50,000.00</span>
              </div>
            </div>

            <div class="tips-item">
              <div class="tips-item__icon tips-item__icon--red">
                <InfoCircleOutlined />
              </div>
              <div class="tips-item__text">
                <span class="tips-item__label">单日累计限额</span>
                <span class="tips-item__value tips-item__value--red">¥200,000.00</span>
              </div>
            </div>

            <div class="tips-item">
              <div class="tips-item__icon tips-item__icon--gold">
                <ClockCircleOutlined />
              </div>
              <div class="tips-item__text">
                <span class="tips-item__label">行内转账</span>
                <span class="tips-item__desc">实时到账</span>
              </div>
            </div>

            <div class="tips-item">
              <div class="tips-item__icon tips-item__icon--gold">
                <ClockCircleOutlined />
              </div>
              <div class="tips-item__text">
                <span class="tips-item__label">跨行转账</span>
                <span class="tips-item__desc">2小时内到账</span>
              </div>
            </div>

            <div class="tips-item tips-item--last">
              <div class="tips-item__icon tips-item__icon--green">
                <SafetyCertificateOutlined />
              </div>
              <div class="tips-item__text">
                <span class="tips-item__label">资金安全</span>
                <span class="tips-item__desc">银行级加密保障</span>
              </div>
            </div>
          </div>

          <!-- 底部安全保障徽章 -->
          <div class="tips-badge">
            <SafetyCertificateOutlined />
            <span>ICBC 安全保障</span>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAccounts, transfer } from '@/api/index'
import { message } from 'ant-design-vue'
import {
  InfoCircleOutlined, ClockCircleOutlined, SafetyCertificateOutlined,
  BankOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const accounts = ref([])
const transferring = ref(false)

const formState = reactive({
  fromAccount: undefined,
  toAccount: '',
  amount: undefined,
  remark: ''
})

const rules = {
  fromAccount: [{ required: true, message: '请选择转出账户' }],
  toAccount: [{ required: true, message: '请输入转入账户号' }],
  amount: [
    { required: true, message: '请输入转账金额' },
    { type: 'number', min: 0.01, message: '金额必须大于0' }
  ]
}

onMounted(async () => {
  try {
    const res = await getAccounts()
    accounts.value = res.data || []
  } catch (e) {}
})

async function handleTransfer() {
  transferring.value = true
  try {
    await transfer({
      fromAccount: formState.fromAccount,
      toAccount: formState.toAccount,
      amount: formState.amount,
      remark: formState.remark
    })
    message.success('转账成功')
    resetForm()
  } finally {
    transferring.value = false
  }
}

function resetForm() {
  formState.fromAccount = undefined
  formState.toAccount = ''
  formState.amount = undefined
  formState.remark = ''
}

function maskAccountNo(no) {
  if (!no) return ''
  return no.substring(0, 4) + '****' + no.substring(no.length - 4)
}

function formatMoney(val) {
  const num = parseFloat(val) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
/* ===== 页面容器 ===== */
.transfer-page {
  max-width: 1400px;
}

/* ===== 转账表单卡片 ===== */
.transfer-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
}

.transfer-card :deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 24px;
}

.transfer-card :deep(.ant-card-body) {
  padding: 24px;
}

.transfer-card__title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1A1A1A;
}

.transfer-card__title-bar {
  display: inline-block;
  width: 3px;
  height: 18px;
  background: #C8102E;
  border-radius: 2px;
}

/* ===== 表单样式 ===== */
.transfer-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #1A1A1A;
  font-size: 14px;
}

/* 转出账户 select 选中前缀 */
.select-option__prefix {
  display: inline-block;
  background: #C8102E;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
  margin-right: 6px;
  letter-spacing: .5px;
}

/* 转入账户 input 图标 */
.input-prefix-icon {
  color: #999;
  font-size: 16px;
}

/* 金额 input-number */
.transfer-amount :deep(.ant-input-number-input) {
  font-size: 18px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.amount-prefix {
  font-size: 16px;
  font-weight: 600;
  color: #C8102E;
}

/* ===== 按钮 ===== */
.transfer-form__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.btn-transfer {
  height: 44px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 8px !important;
  background: #C8102E !important;
  border: none !important;
  transition: all .25s !important;
}

.btn-transfer:hover {
  box-shadow: 0 2px 6px rgba(0,0,0,.08) !important;
}

.btn-reset {
  height: 40px !important;
  font-size: 14px !important;
  border-radius: 8px !important;
  border-color: #d9d9d9 !important;
  color: #666 !important;
}

.btn-reset:hover {
  border-color: #C8102E !important;
  color: #C8102E !important;
}

/* ===== 右侧提示卡片 - ICBC金色风格 ===== */
.tips-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
  background: #FFFBF0;
  border: 1px solid rgba(196, 162, 101, .15);
}

.tips-card :deep(.ant-card-head) {
  border-bottom: 1px solid rgba(196, 162, 101, .15);
  padding: 16px 24px;
  background: transparent;
}

.tips-card :deep(.ant-card-body) {
  padding: 16px 24px 20px;
}

.tips-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1A1A1A;
}

.tips-card__title-icon {
  color: #C4A265;
  font-size: 18px;
}

/* ===== 提示列表 ===== */
.tips-list {
  display: flex;
  flex-direction: column;
}

.tips-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 12px;
  border-radius: 8px;
  transition: background .2s;
}

.tips-item:nth-child(even) {
  background: rgba(196, 162, 101, .06);
}

.tips-item--last {
  margin-bottom: 0;
}

.tips-item__icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.tips-item__icon--red {
  background: #FFF1F0;
  color: #C8102E;
}

.tips-item__icon--gold {
  background: #FFFBF0;
  color: #C4A265;
  border: 1px solid rgba(196, 162, 101, .2);
}

.tips-item__icon--green {
  background: #F6FFED;
  color: #52C41A;
}

.tips-item__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.tips-item__label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.tips-item__value {
  font-size: 14px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.tips-item__value--red {
  color: #C8102E;
}

.tips-item__desc {
  font-size: 13px;
  color: #999;
}

/* ===== 安全保障徽章 ===== */
.tips-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 16px;
  background: #FFFBF0;
  border: 1px solid rgba(196, 162, 101, .2);
  border-radius: 8px;
  color: #C4A265;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* ===== select 下拉选项前缀覆盖 ===== */
.transfer-select :deep(.ant-select-selection-item) {
  display: flex;
  align-items: center;
}
</style>
