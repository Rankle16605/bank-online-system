<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <PageHeader :title="greeting + '，' + userStore.username" subtitle="欢迎使用ICBC智能在线银行" />

    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="账户总数" :value="stats.accountCount" :icon="WalletOutlined" color="red" />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="总资产" :value="stats.totalBalance" prefix="¥" :icon="DollarOutlined" color="green" :precision="2" :trend="5.2" />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="本月交易" :value="stats.monthlyTransactions" :icon="SwapOutlined" color="gold" />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="AI服务次数" :value="stats.aiServiceCount" :icon="RobotOutlined" color="red" />
      </a-col>
    </a-row>

    <!-- 快捷操作 + 账户列表 -->
    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :xs="24" :lg="8">
        <a-card :bordered="false" class="quick-actions-card card-hover">
          <div class="card-title-decorated">快捷操作</div>
          <a-space direction="vertical" :size="12" style="width: 100%">
            <a-button type="primary" block size="large" class="btn-transfer" @click="$router.push('/transfer')">
              <SwapOutlined /> 转账汇款
            </a-button>
            <a-button block size="large" class="btn-outline-icbc" @click="$router.push('/accounts')">
              <WalletOutlined /> 账户查询
            </a-button>
            <a-button block size="large" class="btn-outline-icbc" @click="$router.push('/bills')">
              <FileTextOutlined /> 查看账单
            </a-button>
            <a-button block size="large" class="btn-outline-icbc" @click="$router.push('/ai-chat')">
              <RobotOutlined /> AI智能客服
            </a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="16">
        <a-card :bordered="false" class="account-list-card card-hover">
          <div class="card-title-decorated">我的账户</div>
          <a-spin :spinning="loading">
            <div v-if="accounts.length > 0">
              <div v-for="account in accounts" :key="account.id" class="account-item">
                <div class="account-info">
                  <div class="account-icon">
                    <WalletOutlined />
                  </div>
                  <div class="account-detail">
                    <div class="account-no">{{ maskAccountNo(account.accountNo) }}</div>
                    <div class="account-type">{{ account.accountTypeName }}</div>
                  </div>
                </div>
                <div class="account-balance">
                  <div class="balance-label">可用余额</div>
                  <div class="balance-amount animate-countUp">
                    ¥{{ formatMoney(account.availableBalance) }}
                  </div>
                </div>
                <a-button type="link" class="btn-detail" @click="$router.push('/history')">查看流水</a-button>
              </div>
            </div>
            <a-empty v-else description="暂无账户" />
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getAccounts } from '@/api/index'
import {
  WalletOutlined, SwapOutlined, RobotOutlined, FileTextOutlined, DollarOutlined
} from '@ant-design/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'

const userStore = useUserStore()
const loading = ref(false)
const accounts = ref([])

const stats = reactive({
  accountCount: 0,
  totalBalance: 0,
  monthlyTransactions: 0,
  aiServiceCount: 0
})

const greeting = ref('')

onMounted(async () => {
  const hour = new Date().getHours()
  if (hour < 12) greeting.value = '早上好'
  else if (hour < 18) greeting.value = '下午好'
  else greeting.value = '晚上好'

  loading.value = true
  try {
    const res = await getAccounts()
    accounts.value = res.data || []
    stats.accountCount = accounts.value.length
    stats.totalBalance = accounts.value.reduce((sum, a) => sum + (parseFloat(a.availableBalance) || 0), 0)
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
})

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
.dashboard {
  max-width: 1400px;
}

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

/* ===== 快捷操作卡片 ===== */
.quick-actions-card {
  border-radius: 12px;
  height: 100%;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  transition: box-shadow .3s ease, transform .3s ease;
}
.quick-actions-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.quick-actions-card :deep(.ant-card-body) {
  padding: 24px;
}

/* 转账按钮 - ICBC 主红色 */
.btn-transfer {
  height: 44px !important;
  border-radius: 8px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  background: #C8102E !important;
  border: none !important;
  box-shadow: 0 2px 6px rgba(0,0,0,.08) !important;
  transition: all .25s ease !important;
}
.btn-transfer:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.12) !important;
}

/* outline 风格按钮 */
.btn-outline-icbc {
  height: 44px !important;
  border-radius: 8px !important;
  font-size: 15px !important;
  font-weight: 500 !important;
  color: #1A1A1A !important;
  border: 1.5px solid #E8E8E8 !important;
  background: #FFF !important;
  transition: all .3s ease !important;
}
.btn-outline-icbc:hover {
  color: #C8102E !important;
  border-color: #C8102E !important;
  background: #FFF1F0 !important;
}

/* ===== 账户列表卡片 ===== */
.account-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
  transition: box-shadow .3s ease, transform .3s ease;
}
.account-list-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.account-list-card :deep(.ant-card-body) {
  padding: 24px;
}

.account-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #F0F0F0;
  transition: all .3s ease;
  border-radius: 8px;
}
.account-item:last-child {
  border-bottom: none;
}
.account-item:hover {
  background: #FFF1F0;
}

.account-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.account-icon {
  width: 44px;
  height: 44px;
  background: #C8102E;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFF;
  font-size: 20px;
  flex-shrink: 0;
}

.account-no {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A1A;
  letter-spacing: 1px;
}
.account-type {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.account-balance {
  text-align: right;
  flex: 1;
  margin: 0 24px;
}
.balance-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.balance-amount {
  font-size: 20px;
  font-weight: 800;
  color: #1A1A1A;
  font-variant-numeric: tabular-nums;
}

.btn-detail {
  color: #C8102E !important;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 6px;
  transition: all .3s ease;
}
.btn-detail:hover {
  background: #FFF1F0 !important;
}

/* ===== 动画 ===== */
.animate-fadeInUp {
  animation: fadeInUp .5s ease both;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-countUp {
  animation: countUp .6s ease both;
}
@keyframes countUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 延迟动画 */
.delay-1 { animation-delay: .1s; }
.delay-2 { animation-delay: .2s; }
.delay-3 { animation-delay: .3s; }
.delay-4 { animation-delay: .4s; }
.delay-5 { animation-delay: .5s; }

/* 响应式 */
@media (max-width: 768px) {
  .account-item {
    flex-wrap: wrap;
    gap: 12px;
  }
  .account-balance {
    margin: 0;
    text-align: left;
  }
}
</style>
