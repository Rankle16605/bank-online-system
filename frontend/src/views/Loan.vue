<template>
  <div class="loan-page">
    <PageHeader title="信用卡借款" subtitle="灵活借款，快速到账，轻松分期还款" />

    <!-- 信用卡选择 -->
    <a-row :gutter="[16, 16]">
      <a-col :span="24">
        <a-card :bordered="false" class="card-loan card-hover">
          <div class="card-title-decorated">选择信用卡账户</div>
          <a-spin :spinning="loading">
            <div v-if="creditCards.length > 0">
              <a-radio-group v-model:value="selectedCard" style="width:100%">
                <div v-for="card in creditCards" :key="card.id" class="card-item" :class="{ 'card-selected': selectedCard === card.id }">
                  <a-radio :value="card.id">
                    <div class="card-info-row">
                      <div class="card-icon">
                        <CreditCardOutlined />
                      </div>
                      <div class="card-detail">
                        <div class="card-no">{{ maskAccountNo(card.accountNo) }}</div>
                        <div class="card-limit">
                          信用额度: ¥{{ formatMoney(card.creditLimit) }} | 可用: ¥{{ formatMoney(card.availableCredit) }}
                        </div>
                      </div>
                    </div>
                  </a-radio>
                </div>
              </a-radio-group>
            </div>
            <a-empty v-else description="暂无信用卡账户，请先申请信用卡" />
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <!-- 借款申请表单 -->
    <a-row :gutter="[16, 16]" style="margin-top:16px">
      <a-col :xs="24" :lg="14">
        <a-card :bordered="false" class="card-form card-hover">
          <div class="card-title-decorated">借款申请</div>
          <a-form :model="form" layout="vertical" @finish="handleApply" :rules="rules">
            <a-form-item label="借款金额 (¥)" name="loanAmount">
              <a-input-number v-model:value="form.loanAmount" :min="1000" :max="500000" :step="1000"
                style="width:100%" :formatter="v => `¥ ${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                :parser="v => v.replace(/¥\s?|(,*)/g, '')" size="large" placeholder="请输入借款金额" />
            </a-form-item>
            <a-form-item label="借款期限" name="loanTerm">
              <a-select v-model:value="form.loanTerm" size="large" placeholder="请选择借款期限">
                <a-select-option :value="3">3个月 (年利率 8.0%)</a-select-option>
                <a-select-option :value="6">6个月 (年利率 8.0%)</a-select-option>
                <a-select-option :value="12">12个月 (年利率 10.0%)</a-select-option>
                <a-select-option :value="24">24个月 (年利率 12.0%)</a-select-option>
                <a-select-option :value="36">36个月 (年利率 15.0%)</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="借款用途">
              <a-textarea v-model:value="form.remark" :rows="3" placeholder="请描述借款用途（选填）" />
            </a-form-item>

            <!-- 还款预估 -->
            <div v-if="form.loanAmount > 0 && form.loanTerm > 0" class="estimate-box">
              <div class="estimate-title">还款预估</div>
              <a-row :gutter="16">
                <a-col :span="8"><div class="estimate-item"><span class="est-label">月还款额</span><span class="est-value">¥{{ formatMoney(estimatedMonthly) }}</span></div></a-col>
                <a-col :span="8"><div class="estimate-item"><span class="est-label">总还款额</span><span class="est-value">¥{{ formatMoney(estimatedTotal) }}</span></div></a-col>
                <a-col :span="8"><div class="estimate-item"><span class="est-label">总利息</span><span class="est-value">¥{{ formatMoney(estimatedTotal - form.loanAmount) }}</span></div></a-col>
              </a-row>
            </div>

            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block class="btn-submit" :loading="submitting">
                提交借款申请
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card :bordered="false" class="card-tips card-hover">
          <div class="card-title-decorated">借款须知</div>
          <div class="tips-list">
            <div class="tip-item"><SafetyCertificateOutlined class="tip-icon" /> 年化利率8%-15%，根据期限浮动</div>
            <div class="tip-item"><CheckCircleOutlined class="tip-icon" /> 等额本息还款，每月固定金额</div>
            <div class="tip-item"><ClockCircleOutlined class="tip-icon" /> 支持3/6/12/24/36个月分期</div>
            <div class="tip-item"><InfoCircleOutlined class="tip-icon" /> 提前还款免收违约金</div>
            <div class="tip-item"><StarOutlined class="tip-icon" /> 信用良好可享优惠利率</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 我的借款记录 -->
    <a-row style="margin-top:16px">
      <a-col :span="24">
        <a-card :bordered="false" class="card-history card-hover">
          <div class="card-title-decorated">我的借款记录</div>
          <a-table :columns="columns" :data-source="loans" :pagination="false" row-key="id" size="middle">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="statusColor(record.status)">{{ record.statusText }}</a-tag>
              </template>
              <template v-if="column.key === 'amount'">
                <span class="amount-text">¥{{ formatMoney(record.loanAmount) }}</span>
              </template>
              <template v-if="column.key === 'monthly'">
                ¥{{ formatMoney(record.monthlyPayment) }}
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" size="small" @click="showDetail(record)" v-if="record.status === 3">还款明细</a-button>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 还款明细弹窗 -->
    <a-modal v-model:open="repaymentVisible" title="还款明细" width="700px" :footer="null">
      <a-table :columns="rpColumns" :data-source="repayments" :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'orange'">{{ record.statusText }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button v-if="record.status === 0" type="primary" size="small" @click="handleRepay(record)" :loading="repayLoading">立即还款</a-button>
            <span v-else class="paid-text">已还款</span>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { getAccounts, getMyLoans, applyLoan, getRepayments, repayLoan } from '@/api/index'
import {
  CreditCardOutlined, SafetyCertificateOutlined, CheckCircleOutlined,
  ClockCircleOutlined, InfoCircleOutlined, StarOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const submitting = ref(false)
const repayLoading = ref(false)
const selectedCard = ref(null)
const creditCards = ref([])
const loans = ref([])
const repaymentVisible = ref(false)
const repayments = ref([])
const currentLoan = ref(null)

const form = reactive({
  loanAmount: null,
  loanTerm: null,
  remark: ''
})

const rules = {
  loanAmount: [{ required: true, message: '请输入借款金额', trigger: 'blur' }],
  loanTerm: [{ required: true, message: '请选择借款期限', trigger: 'change' }]
}

const columns = [
  { title: '借款编号', dataIndex: 'loanNo', key: 'loanNo' },
  { title: '金额', key: 'amount', width: 120 },
  { title: '期限(月)', dataIndex: 'loanTerm', width: 80 },
  { title: '月还款', key: 'monthly', width: 110 },
  { title: '状态', key: 'status', width: 100 },
  { title: '申请日期', dataIndex: 'applyDate', width: 110 },
  { title: '操作', key: 'action', width: 100 }
]

const rpColumns = [
  { title: '期数', dataIndex: 'periodNo', width: 60 },
  { title: '应还金额', dataIndex: 'amount', width: 100, customRender: ({ text }) => '¥' + formatMoney(text) },
  { title: '本金', dataIndex: 'principal', width: 100, customRender: ({ text }) => '¥' + formatMoney(text) },
  { title: '利息', dataIndex: 'interest', width: 90, customRender: ({ text }) => '¥' + formatMoney(text) },
  { title: '计划还款日', dataIndex: 'scheduledDate', width: 110 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 100 }
]

const estimatedMonthly = computed(() => {
  if (!form.loanAmount || !form.loanTerm) return 0
  const rate = getRate(form.loanTerm) / 1200
  const p = form.loanAmount
  const n = form.loanTerm
  if (rate === 0) return p / n
  const pow = Math.pow(1 + rate, n)
  return p * rate * pow / (pow - 1)
})

const estimatedTotal = computed(() => estimatedMonthly.value * (form.loanTerm || 1))

function getRate(term) {
  if (term <= 6) return 8.0
  if (term <= 12) return 10.0
  if (term <= 24) return 12.0
  return 15.0
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getAccounts()
    creditCards.value = (res.data || []).filter(a => a.accountType === 2)
    if (creditCards.value.length > 0) selectedCard.value = creditCards.value[0].id
  } catch (e) { /* handled */ }
  finally { loading.value = false }
  await loadLoans()
})

async function loadLoans() {
  try {
    const res = await getMyLoans()
    loans.value = res.data || []
  } catch (e) { /* handled */ }
}

async function handleApply() {
  if (!selectedCard.value) { message.warning('请选择信用卡账户'); return }
  const card = creditCards.value.find(c => c.id === selectedCard.value)
  if (form.loanAmount > parseFloat(card.availableCredit || 0)) {
    message.warning('借款金额不能超过可用额度'); return
  }
  submitting.value = true
  try {
    await applyLoan({ accountNo: card.accountNo, loanAmount: form.loanAmount, loanTerm: form.loanTerm, remark: form.remark })
    message.success('借款申请已提交！')
    form.loanAmount = null
    form.loanTerm = null
    form.remark = ''
    await loadLoans()
  } catch (e) { message.error(e?.response?.data?.message || '申请失败') }
  finally { submitting.value = false }
}

async function showDetail(record) {
  currentLoan.value = record
  try {
    const res = await getRepayments(record.loanNo)
    repayments.value = res.data || []
    repaymentVisible.value = true
  } catch (e) { message.error('获取还款明细失败') }
}

async function handleRepay(record) {
  repayLoading.value = true
  try {
    await repayLoan(record.id)
    message.success('还款成功！')
    await showDetail(currentLoan.value)
    await loadLoans()
  } catch (e) { message.error(e?.response?.data?.message || '还款失败') }
  finally { repayLoading.value = false }
}

function statusColor(status) {
  const map = { 0: 'red', 1: 'orange', 2: 'blue', 3: 'cyan', 4: 'green', 5: 'magenta' }
  return map[status] || 'default'
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
.loan-page { max-width: 1400px; }
.card-title-decorated {
  font-size: 16px; font-weight: 700; color: #1A1A1A; margin-bottom: 20px;
  padding-left: 14px; position: relative;
}
.card-title-decorated::before {
  content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
  width: 4px; height: 18px; background: #C8102E; border-radius: 2px;
}
.card-hover {
  border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.04);
  transition: box-shadow .3s ease;
}
.card-hover:hover { box-shadow: 0 2px 8px rgba(0,0,0,.06); }

.card-item {
  padding: 16px; border: 2px solid #F0F0F0; border-radius: 10px; margin-bottom: 10px;
  transition: all .3s ease; cursor: pointer;
}
.card-item:hover { border-color: #F5A0A9; background: #FFFAFA; }
.card-selected { border-color: #C8102E !important; background: #FFF1F0; }
.card-info-row { display: flex; align-items: center; gap: 12px; }
.card-icon {
  width: 44px; height: 44px; background: #C8102E; border-radius: 8px;
  display: flex; align-items: center; justify-content: center; color: #FFF; font-size: 20px; flex-shrink: 0;
}
.card-no { font-size: 15px; font-weight: 600; color: #1A1A1A; letter-spacing: 1px; }
.card-limit { font-size: 12px; color: #888; margin-top: 2px; }

.estimate-box {
  background: #FFF1F0; border-radius: 10px; padding: 20px; margin-bottom: 16px;
}
.estimate-title { font-size: 14px; font-weight: 600; color: #C8102E; margin-bottom: 12px; }
.estimate-item { text-align: center; }
.est-label { display: block; font-size: 12px; color: #888; margin-bottom: 4px; }
.est-value { font-size: 18px; font-weight: 700; color: #1A1A1A; }

.tips-list { padding: 0; }
.tip-item {
  padding: 12px 0; border-bottom: 1px solid #F5F5F5; font-size: 14px; color: #555;
  display: flex; align-items: center; gap: 10px;
}
.tip-item:last-child { border-bottom: none; }
.tip-icon { color: #C8102E; font-size: 16px; flex-shrink: 0; }

.btn-submit {
  height: 48px !important; border-radius: 10px !important; font-size: 16px !important;
  font-weight: 600 !important; background: #C8102E !important; border: none !important;
  box-shadow: 0 2px 8px rgba(200,16,46,.25) !important; transition: all .25s ease !important;
}
.btn-submit:hover { box-shadow: 0 4px 14px rgba(200,16,46,.35) !important; transform: translateY(-1px); }

.amount-text { font-weight: 600; color: #1A1A1A; }
.paid-text { color: #52c41a; font-size: 13px; }
</style>
