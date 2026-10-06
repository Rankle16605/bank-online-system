<template>
  <div class="admin-dashboard">
    <!-- 顶栏 -->
    <div class="dash-topbar">
      <div class="topbar-left">
        <div class="logo-icon"><BankOutlined /></div>
        <div>
          <div class="topbar-title">ICBC 数据运营中心</div>
          <div class="topbar-subtitle">Industrial and Commercial Bank of China · 管理控制台</div>
        </div>
      </div>
      <div class="topbar-right">
        <a-button type="link" style="color:#fff; border-color:rgba(255,255,255,.3)" @click="exportDashboardPDF">
          <template #icon><FilePdfOutlined /></template>导出报表
        </a-button>
        <span class="topbar-time">{{ currentTime }}</span>
        <a-tag color="red">系统管理员</a-tag>
      </div>
    </div>

    <!-- 核心指标 KPI 卡片 -->
    <a-row :gutter="[16, 16]" class="kpi-row">
      <a-col :xs="12" :sm="6" :lg="4">
        <div class="kpi-card">
          <div class="kpi-ring">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#f0f0f0" stroke-width="6"/><circle cx="50" cy="50" r="42" fill="none" stroke="#C8102E" stroke-width="6" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="264-(264*(data.totalUsers||0)/50)"/></svg>
            <span class="kpi-ring-val">{{ data.totalUsers || 0 }}</span>
          </div>
          <div class="kpi-label">注册用户</div>
          <div class="kpi-sub">总用户数</div>
        </div>
      </a-col>
      <a-col :xs="12" :sm="6" :lg="4">
        <div class="kpi-card">
          <div class="kpi-ring">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#f0f0f0" stroke-width="6"/><circle cx="50" cy="50" r="42" fill="none" stroke="#1565C0" stroke-width="6" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="264-(264*(data.totalAccounts||0)/70)"/></svg>
            <span class="kpi-ring-val" style="color:#1565C0">{{ data.totalAccounts || 0 }}</span>
          </div>
          <div class="kpi-label">银行账户</div>
          <div class="kpi-sub">总开户数</div>
        </div>
      </a-col>
      <a-col :xs="12" :sm="6" :lg="4">
        <div class="kpi-card">
          <div class="kpi-ring">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#f0f0f0" stroke-width="6"/><circle cx="50" cy="50" r="42" fill="none" stroke="#2E7D32" stroke-width="6" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="120"/></svg>
            <span class="kpi-ring-val" style="color:#2E7D32">¥{{ fmtWan(data.totalBalance) }}</span>
          </div>
          <div class="kpi-label">资产总额</div>
          <div class="kpi-sub">所有账户余额合计</div>
        </div>
      </a-col>
      <a-col :xs="12" :sm="6" :lg="4">
        <div class="kpi-card">
          <div class="kpi-ring">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#f0f0f0" stroke-width="6"/><circle cx="50" cy="50" r="42" fill="none" stroke="#FA8C16" stroke-width="6" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="264-(264*(data.totalTransactions||0)/100)"/></svg>
            <span class="kpi-ring-val" style="color:#FA8C16">{{ data.totalTransactions || 0 }}</span>
          </div>
          <div class="kpi-label">交易笔数</div>
          <div class="kpi-sub">累计交易量</div>
        </div>
      </a-col>
      <a-col :xs="12" :sm="6" :lg="4">
        <div class="kpi-card">
          <div class="kpi-ring">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#f0f0f0" stroke-width="6"/><circle cx="50" cy="50" r="42" fill="none" stroke="#722ED1" stroke-width="6" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="264-(264*(data.totalLoans||0)/30)"/></svg>
            <span class="kpi-ring-val" style="color:#722ED1">{{ data.totalLoans || 0 }}</span>
          </div>
          <div class="kpi-label">借款申请</div>
          <div class="kpi-sub">信用卡借款总计</div>
        </div>
      </a-col>
      <a-col :xs="12" :sm="6" :lg="4">
        <div class="kpi-card">
          <div class="kpi-ring">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#f0f0f0" stroke-width="6"/><circle cx="50" cy="50" r="42" fill="none" stroke="#13C2C2" stroke-width="6" stroke-linecap="round" :stroke-dasharray="264" :stroke-dashoffset="140"/></svg>
            <span class="kpi-ring-val" style="color:#13C2C2">¥{{ fmtWan(data.totalLoanAmount) }}</span>
          </div>
          <div class="kpi-label">借款总额</div>
          <div class="kpi-sub">授信金额合计</div>
        </div>
      </a-col>
    </a-row>

    <!-- 今日实时数据 -->
    <a-row :gutter="[16, 16]" style="margin-top:16px">
      <a-col :xs="8" :sm="4">
        <div class="realtime-card">
          <div class="rt-icon" style="background:#E6F7FF"><UserSwitchOutlined style="color:#1890FF;font-size:20px"/></div>
          <div class="rt-val">{{ data.activeUsersToday || 0 }}</div>
          <div class="rt-lbl">今日活跃用户</div>
        </div>
      </a-col>
      <a-col :xs="8" :sm="4">
        <div class="realtime-card">
          <div class="rt-icon" style="background:#FFF7E6"><SwapOutlined style="color:#FA8C16;font-size:20px"/></div>
          <div class="rt-val">{{ data.transactionsToday || 0 }}</div>
          <div class="rt-lbl">今日交易笔数</div>
        </div>
      </a-col>
      <a-col :xs="8" :sm="4">
        <div class="realtime-card">
          <div class="rt-icon" style="background:#F6FFED"><AccountBookOutlined style="color:#52C41A;font-size:20px"/></div>
          <div class="rt-val">¥{{ fmtWan(data.amountToday) }}</div>
          <div class="rt-lbl">今日交易额</div>
        </div>
      </a-col>
      <a-col :xs="8" :sm="4">
        <div class="realtime-card">
          <div class="rt-icon" style="background:#FFF1F0"><SafetyOutlined style="color:#C8102E;font-size:20px"/></div>
          <div class="rt-val" style="color:#2E7D32">100%</div>
          <div class="rt-lbl">系统健康度</div>
        </div>
      </a-col>
      <a-col :xs="8" :sm="4">
        <div class="realtime-card">
          <div class="rt-icon" style="background:#F9F0FF"><ThunderboltOutlined style="color:#722ED1;font-size:20px"/></div>
          <div class="rt-val">{{ data.avgResponse || '120' }}ms</div>
          <div class="rt-lbl">平均响应时间</div>
        </div>
      </a-col>
      <a-col :xs="8" :sm="4">
        <div class="realtime-card">
          <div class="rt-icon" style="background:#E6FFFB"><RiseOutlined style="color:#13C2C2;font-size:20px"/></div>
          <div class="rt-val">99.9%</div>
          <div class="rt-lbl">服务可用率</div>
        </div>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="[16, 16]" style="margin-top:16px">
      <!-- 近7天交易趋势 -->
      <a-col :xs="24" :lg="14">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">近7天交易趋势</span>
            <span class="panel-badge">笔</span>
          </div>
          <div class="chart-area">
            <div class="chart-y-axis">
              <span v-for="v in yAxisLabels" :key="v">{{ v }}</span>
            </div>
            <div class="chart-bars">
              <div v-for="(day, idx) in (data.transactionTrend || [])" :key="idx" class="cbar-group">
                <div class="cbar-val">{{ day.count }}</div>
                <div class="cbar-track"><div class="cbar-fill" :style="{height:getPct(day.count)+'%'}"></div></div>
                <div class="cbar-date">{{ day.date }}</div>
              </div>
            </div>
          </div>
          <div class="chart-legend">
            <span class="legend-dot"></span> 交易量趋势
          </div>
        </div>
      </a-col>

      <!-- 账户类型 & 借款状态 -->
      <a-col :xs="24" :lg="10">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">账户类型分布</span>
          </div>
          <div v-for="(item, idx) in (data.accountTypeDistribution || [])" :key="idx" class="type-row">
            <div class="type-dot" :style="{background:item.type===1?'#C8102E':'#C4A265'}"></div>
            <div class="type-name">{{ item.name }}</div>
            <div class="type-bar-bg"><div class="type-bar" :style="{width:getTypePct(item.count)+'%',background:item.type===1?'#C8102E':'#C4A265'}"></div></div>
            <div class="type-num">{{ item.count }}</div>
          </div>
        </div>
        <div class="panel" style="margin-top:16px">
          <div class="panel-header">
            <span class="panel-title">借款状态看板</span>
          </div>
          <div class="loan-grid">
            <div v-for="(item, idx) in (data.loanStatusDistribution || [])" :key="idx" class="loan-cell">
              <div class="loan-cell-val" :style="{color:lsColors[item.status]||'#666'}">{{ item.count }}</div>
              <div class="loan-cell-name">{{ item.name }}</div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top:16px">
      <!-- TOP5 用户资产 -->
      <a-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">TOP5 用户资产排名</span>
          </div>
          <div class="top-list">
            <div v-for="(u, idx) in (data.topUsers || [])" :key="idx" class="top-item">
              <div class="top-rank" :class="'r'+(idx+1)">{{ idx+1 }}</div>
              <div class="top-avatar">{{ u.username?.[0] }}</div>
              <div class="top-mid">
                <div class="top-uname">{{ u.username }}</div>
                <div class="top-bar-wrap"><div class="top-bar" :style="{width:getTopPct(u.totalBalance)+'%'}"></div></div>
              </div>
              <div class="top-amount">¥{{ fmtWan(u.totalBalance) }}</div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 最近交易 -->
      <a-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">最近交易动态</span>
            <span class="panel-badge">实时</span>
          </div>
          <div class="tx-list">
            <div v-for="(tx, idx) in (data.recentTransactions || [])" :key="idx" class="tx-item">
              <div class="tx-dot" :class="idx<5?'dot-new':'dot-old'"></div>
              <div class="tx-mid">
                <div class="tx-no">{{ tx.transactionNo }}</div>
                <div class="tx-time">{{ tx.createdAt }}</div>
              </div>
              <div class="tx-amount" :class="tx.amount>5000?'big':''">¥{{ formatMoney(tx.amount) }}</div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { getAdminDashboard } from '@/api/index'
import { exportICBCReport, fmt, genFilename } from '@/utils/pdfExport'
import {
  BankOutlined, UserSwitchOutlined, SwapOutlined, AccountBookOutlined,
  SafetyOutlined, ThunderboltOutlined, RiseOutlined, FilePdfOutlined
} from '@ant-design/icons-vue'

const data = reactive({
  totalUsers: 0, totalAccounts: 0, totalTransactions: 0, totalBalance: 0,
  totalLoans: 0, totalLoanAmount: 0, activeUsersToday: 0, transactionsToday: 0, amountToday: 0,
  transactionTrend: [], accountTypeDistribution: [], loanStatusDistribution: [],
  topUsers: [], recentTransactions: [], recentLoans: [],
  avgResponse: 120
})

const lsColors = ['#f5222d','#fa8c16','#1890ff','#13c2c2','#52c41a','#722ed1']
const currentTime = ref('')
let timer = null

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  try {
    const res = await getAdminDashboard()
    Object.assign(data, res.data || {})
  } catch (e) { /* use defaults */ }
})

onUnmounted(() => clearInterval(timer))

function updateTime() {
  const now = new Date()
  currentTime.value = now.getFullYear()+'-'+pad(now.getMonth()+1)+'-'+pad(now.getDate())+' '+pad(now.getHours())+':'+pad(now.getMinutes())+':'+pad(now.getSeconds())
}

function pad(n) { return n<10 ? '0'+n : ''+n }

const maxCount = computed(() => {
  const arr = data.transactionTrend || []
  return arr.length ? Math.max(...arr.map(d => d.count || 0), 1) : 1
})

const totalAcc = computed(() => {
  const arr = data.accountTypeDistribution || []
  return arr.reduce((s, i) => s + (i.count || 0), 0) || 1
})

const maxTopB = computed(() => {
  const arr = data.topUsers || []
  return arr.length ? Math.max(...arr.map(u => parseFloat(u.totalBalance) || 0), 1) : 1
})

const yAxisLabels = computed(() => {
  const mx = maxCount.value
  if (mx <= 5) return [5,4,3,2,1,0]
  if (mx <= 10) return [10,8,6,4,2,0]
  const step = Math.ceil(mx / 5)
  return [step*5, step*4, step*3, step*2, step, 0]
})

function getPct(count) { return Math.max(2, (count / maxCount.value) * 100) }
function getTypePct(count) { return Math.max(2, (count / totalAcc.value) * 100) }
function getTopPct(balance) { return Math.max(2, (parseFloat(balance || 0) / maxTopB.value) * 100) }

function fmtWan(val) {
  const num = parseFloat(val) || 0
  if (num >= 100000000) return (num / 100000000).toFixed(2) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

function formatMoney(val) {
  const num = parseFloat(val) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// ==================== PDF 导出 ====================
async function exportDashboardPDF() {
  try {
    // 核心HTML：汇总指标 + 多段表格
    let tablesHTML = ''

    // 核心指标
    tablesHTML += `
      <div style="margin:10px 0;padding:10px 14px;background:#FAFAFC;border-left:3px solid #C8102E;border-radius:4px;display:flex;flex-wrap:wrap;gap:6px 28px;font-size:12px;">
        <span><b>注册用户：</b>${data.totalUsers || 0} 人</span>
        <span><b>账户数：</b>${data.totalAccounts || 0} 个</span>
        <span><b>资产总额：</b>${fmt(data.totalBalance)}</span>
        <span><b>交易笔数：</b>${data.totalTransactions || 0} 笔</span>
        <span><b>借款申请：</b>${data.totalLoans || 0} 笔</span>
        <span><b>借款总额：</b>${fmt(data.totalLoanAmount)}</span>
        <span><b>今日活跃：</b>${data.activeUsersToday || 0} 人</span>
        <span><b>今日交易：</b>${data.transactionsToday || 0} 笔</span>
        <span><b>今日交易额：</b>${fmt(data.amountToday)}</span>
      </div>
    `

    // 7天趋势表
    if (data.transactionTrend?.length) {
      const header = '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">日期</th>' +
        '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">交易笔数</th>'
      const rows = data.transactionTrend.map((d, i) =>
        `<tr style="background:${i%2?'#FAFAFC':'#fff'}"><td style="padding:5px;font-size:9px;text-align:center;border-bottom:1px solid #eee;">${d.date}</td><td style="padding:5px;font-size:9px;text-align:right;border-bottom:1px solid #eee;">${d.count}</td></tr>`
      ).join('')
      tablesHTML += `<div style="margin-top:12px;font-size:14px;font-weight:600;color:#1A1A2E;">近7天交易趋势</div>
      <table style="width:100%;border-collapse:collapse;margin-top:4px;"><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table>`
    }

    // 账户类型分布
    if (data.accountTypeDistribution?.length) {
      const header = '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">类型</th>' +
        '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">数量</th>'
      const rows = data.accountTypeDistribution.map((d, i) =>
        `<tr style="background:${i%2?'#FAFAFC':'#fff'}"><td style="padding:5px;font-size:9px;text-align:center;border-bottom:1px solid #eee;">${d.name}</td><td style="padding:5px;font-size:9px;text-align:right;border-bottom:1px solid #eee;">${d.count}</td></tr>`
      ).join('')
      tablesHTML += `<div style="margin-top:12px;font-size:14px;font-weight:600;color:#1A1A2E;">账户类型分布</div>
      <table style="width:100%;border-collapse:collapse;margin-top:4px;"><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table>`
    }

    // 借款状态
    if (data.loanStatusDistribution?.length) {
      const header = '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">状态</th>' +
        '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">数量</th>'
      const rows = data.loanStatusDistribution.map((d, i) =>
        `<tr style="background:${i%2?'#FAFAFC':'#fff'}"><td style="padding:5px;font-size:9px;text-align:center;border-bottom:1px solid #eee;">${d.name}</td><td style="padding:5px;font-size:9px;text-align:right;border-bottom:1px solid #eee;">${d.count}</td></tr>`
      ).join('')
      tablesHTML += `<div style="margin-top:12px;font-size:14px;font-weight:600;color:#1A1A2E;">借款状态看板</div>
      <table style="width:100%;border-collapse:collapse;margin-top:4px;"><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table>`
    }

    // TOP5
    if (data.topUsers?.length) {
      const header = '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">排名</th>' +
        '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">用户名</th>' +
        '<th style="padding:8px;background:#C8102E;color:#fff;font-size:10px;text-align:center;border:1px solid #C8102E;">资产总额</th>'
      const rows = data.topUsers.map((u, i) =>
        `<tr style="background:${i%2?'#FAFAFC':'#fff'}"><td style="padding:5px;font-size:9px;text-align:center;border-bottom:1px solid #eee;">No.${i+1}</td><td style="padding:5px;font-size:9px;text-align:center;border-bottom:1px solid #eee;">${u.username}</td><td style="padding:5px;font-size:9px;text-align:right;border-bottom:1px solid #eee;">${fmt(u.totalBalance)}</td></tr>`
      ).join('')
      tablesHTML += `<div style="margin-top:12px;font-size:14px;font-weight:600;color:#1A1A2E;">TOP5 用户资产排名</div>
      <table style="width:100%;border-collapse:collapse;margin-top:4px;"><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table>`
    }

    await exportICBCReport({
      title: '数据运营中心 · 运营报表',
      orientation: 'portrait',
      columns: ['指标', '数值'],
      rows: [],
      filename: genFilename('数据大屏运营报表'),
      customHTML: tablesHTML
    })
  } catch (e) {
    console.error('PDF导出失败:', e)
    window.$message?.error('PDF导出失败，请重试')
  }
}
</script>

<style scoped>
.admin-dashboard {
  max-width: 1600px;
  padding: 0 0 24px 0;
}

/* ----- 顶栏 ----- */
.dash-topbar {
  display: flex; align-items: center; justify-content: space-between;
  background: linear-gradient(135deg, #1A1A2E, #16213E);
  border-radius: 12px; padding: 16px 24px; margin-bottom: 16px;
  color: #FFF; box-shadow: 0 2px 12px rgba(0,0,0,.15);
}
.topbar-left { display: flex; align-items: center; gap: 14px; }
.logo-icon {
  width: 44px; height: 44px; border-radius: 10px;
  background: linear-gradient(135deg, #C8102E, #E87080);
  display: flex; align-items: center; justify-content: center; font-size: 22px; color: #FFF;
}
.topbar-title { font-size: 18px; font-weight: 700; letter-spacing: 1px; }
.topbar-subtitle { font-size: 12px; color: rgba(255,255,255,.55); margin-top: 2px; }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.topbar-time { font-size: 14px; font-weight: 500; color: rgba(255,255,255,.75); font-variant-numeric: tabular-nums; }

/* ----- KPI 环形卡片 ----- */
.kpi-card {
  background: #FFF; border-radius: 12px; padding: 20px 12px;
  text-align: center; box-shadow: 0 1px 6px rgba(0,0,0,.04);
  transition: all .3s; cursor: pointer; border: 1px solid #F0F0F0;
}
.kpi-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.08); transform: translateY(-2px); }

.kpi-ring { position: relative; width: 80px; height: 80px; margin: 0 auto 10px; }
.kpi-ring svg { width: 80px; height: 80px; transform: rotate(-90deg); }
.kpi-ring-val {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  font-size: 16px; font-weight: 800; color: #C8102E;
}
.kpi-label { font-size: 13px; font-weight: 600; color: #1A1A1A; }
.kpi-sub { font-size: 11px; color: #999; margin-top: 2px; }

/* ----- 实时数据 ----- */
.realtime-card {
  background: #FFF; border-radius: 10px; padding: 16px 12px; text-align: center;
  border: 1px solid #F0F0F0; transition: .3s;
}
.realtime-card:hover { border-color: #C8102E; box-shadow: 0 2px 8px rgba(200,16,46,.08); }
.rt-icon {
  width: 40px; height: 40px; border-radius: 10px; display: inline-flex;
  align-items: center; justify-content: center; margin-bottom: 8px;
}
.rt-val { font-size: 20px; font-weight: 700; color: #1A1A1A; }
.rt-lbl { font-size: 11px; color: #888; margin-top: 2px; }

/* ----- 面板 ----- */
.panel {
  background: #FFF; border-radius: 12px; padding: 20px 24px;
  box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #F0F0F0;
}
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-title { font-size: 15px; font-weight: 700; color: #1A1A1A; border-left: 3px solid #C8102E; padding-left: 10px; }
.panel-badge { font-size: 11px; background: #C8102E; color: #FFF; padding: 2px 8px; border-radius: 10px; }

/* ----- 柱状图 ----- */
.chart-area { display: flex; height: 200px; }
.chart-y-axis { display: flex; flex-direction: column; justify-content: space-between; width: 32px; font-size: 10px; color: #999; text-align: right; padding-right: 8px; }
.chart-bars { flex: 1; display: flex; align-items: flex-end; gap: 10px; padding: 0 8px; }
.cbar-group { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end; }
.cbar-val { font-size: 10px; font-weight: 600; color: #C8102E; margin-bottom: 4px; }
.cbar-track { width: 100%; max-width: 48px; flex: 1; background: #F5F5F5; border-radius: 6px 6px 0 0; overflow: hidden; display: flex; align-items: flex-end; }
.cbar-fill { width: 100%; background: linear-gradient(180deg, #C8102E, #E87080); border-radius: 6px 6px 0 0; transition: height .5s ease; min-height: 2px; }
.cbar-date { font-size: 10px; color: #999; margin-top: 6px; }
.chart-legend { display: flex; align-items: center; gap: 6px; margin-top: 12px; font-size: 11px; color: #888; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; background: #C8102E; }

/* ----- 账户类型分布 ----- */
.type-row { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.type-row:last-child { margin-bottom: 0; }
.type-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.type-name { font-size: 13px; color: #555; width: 56px; flex-shrink: 0; }
.type-bar-bg { flex: 1; height: 8px; background: #F5F5F5; border-radius: 4px; overflow: hidden; }
.type-bar { height: 100%; border-radius: 4px; transition: width .5s; }
.type-num { font-size: 13px; font-weight: 600; color: #1A1A1A; width: 28px; text-align: right; }

/* ----- 借款状态网格 ----- */
.loan-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.loan-cell { background: #FAFAFA; border-radius: 8px; padding: 14px 10px; text-align: center; }
.loan-cell-val { font-size: 22px; font-weight: 700; }
.loan-cell-name { font-size: 11px; color: #888; margin-top: 4px; }

/* ----- TOP5 ----- */
.top-list { max-height: 330px; overflow-y: auto; }
.top-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #F5F5F5; }
.top-item:last-child { border-bottom: none; }
.top-rank { width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; color: #FFF; flex-shrink: 0; }
.r1 { background: linear-gradient(135deg, #FFD700, #FF8C00); }
.r2 { background: linear-gradient(135deg, #C0C0C0, #909090); }
.r3 { background: linear-gradient(135deg, #CD7F32, #A0522D); }
.r4, .r5 { background: #DDD; color: #666; }
.top-avatar {
  width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #C8102E, #E87080);
  display: flex; align-items: center; justify-content: center; color: #FFF; font-size: 14px; font-weight: 600; flex-shrink: 0;
}
.top-mid { flex: 1; }
.top-uname { font-size: 14px; font-weight: 500; color: #1A1A1A; margin-bottom: 4px; }
.top-bar-wrap { height: 5px; background: #F0F0F0; border-radius: 3px; overflow: hidden; }
.top-bar { height: 100%; background: linear-gradient(90deg, #C8102E, #E87080); border-radius: 3px; transition: width .5s; }
.top-amount { font-size: 14px; font-weight: 700; color: #1A1A1A; white-space: nowrap; }

/* ----- 最近交易 ----- */
.tx-list { max-height: 330px; overflow-y: auto; }
.tx-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #F5F5F5; }
.tx-item:last-child { border-bottom: none; }
.tx-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-new { background: #52C41A; box-shadow: 0 0 6px rgba(82,196,26,.4); }
.dot-old { background: #DDD; }
.tx-mid { flex: 1; }
.tx-no { font-size: 12px; color: #666; font-family: monospace; margin-bottom: 2px; }
.tx-time { font-size: 11px; color: #BBB; }
.tx-amount { font-size: 14px; font-weight: 600; color: #1A1A1A; }
.tx-amount.big { color: #C8102E; font-weight: 700; }
</style>
