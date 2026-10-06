<template>
  <div class="admin-loans">
    <PageHeader title="借款管理" subtitle="审批借款申请，查看所有借款记录">
      <template #extra>
        <a-button @click="exportLoansPDF" :icon="h(FilePdfOutlined)">导出PDF</a-button>
      </template>
    </PageHeader>

    <a-card :bordered="false" class="card-main">
      <a-table :columns="columns" :data-source="loans" :loading="loading"
        :pagination="{ pageSize: 10 }" row-key="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span class="amount-text">¥{{ formatMoney(record.loanAmount) }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.statusText }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button v-if="record.status === 1" type="primary" size="small" style="margin-right:8px"
              @click="handleApprove(record)">审批通过</a-button>
            <a-button v-if="record.status === 1" size="small" danger @click="handleReject(record)">拒绝</a-button>
            <span v-else class="text-muted">-</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getAdminLoans, adminApproveLoan, adminRejectLoan } from '@/api/index'
import PageHeader from '@/components/PageHeader.vue'
import { FilePdfOutlined } from '@ant-design/icons-vue'
import { exportICBCReport, fmt, genFilename } from '@/utils/pdfExport'

const loading = ref(false); const loans = ref([])

const columns = [
  { title: '借款编号', dataIndex: 'loanNo', width: 120 },
  { title: '用户', dataIndex: 'username', width: 90 },
  { title: '金额', key: 'amount', width: 120 },
  { title: '期限(月)', dataIndex: 'loanTerm', width: 75 },
  { title: '利率(%)', dataIndex: 'interestRate', width: 75 },
  { title: '月还款', dataIndex: 'monthlyPayment', width: 110, customRender: ({ text }) => '¥' + formatMoney(text) },
  { title: '状态', key: 'status', width: 80 },
  { title: '申请日期', dataIndex: 'applyDate', width: 100 },
  { title: '操作', key: 'action', width: 160 }
]

onMounted(() => loadLoans())

async function loadLoans() {
  loading.value = true
  try { const res = await getAdminLoans(); loans.value = res.data || [] } catch (e) { message.error('加载借款列表失败') }
  finally { loading.value = false }
}

async function handleApprove(record) {
  Modal.confirm({
    title: '确认审批',
    content: `确认批准借款 ${record.loanNo}，金额 ¥${formatMoney(record.loanAmount)}？`,
    okText: '确认批准',
    cancelText: '取消',
    onOk: async () => {
      try {
        await adminApproveLoan(record.loanNo)
        message.success('借款已批准！')
        await loadLoans()
      } catch (e) { message.error(e?.response?.data?.message || '审批失败') }
    }
  })
}

async function handleReject(record) {
  Modal.confirm({
    title: '确认拒绝',
    content: `确认拒绝借款 ${record.loanNo}？`,
    okText: '确认拒绝',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await adminRejectLoan(record.loanNo, '管理员驳回')
        message.success('借款已拒绝！')
        await loadLoans()
      } catch (e) { message.error(e?.response?.data?.message || '操作失败') }
    }
  })
}

function statusColor(s) { const m = { 0:'red',1:'orange',2:'blue',3:'cyan',4:'green',5:'magenta' }; return m[s] || 'default' }
function formatMoney(val) { const num = parseFloat(val) || 0; return num.toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2}) }

// ==================== PDF 导出 ====================
async function exportLoansPDF() {
  try {
    const cols = ['借款编号', '用户', '金额(元)', '期限(月)', '利率(%)', '月还款(元)', '状态', '申请日期']
    const rows = loans.value.map(l => [
      l.loanNo, l.username, fmt(l.loanAmount),
      String(l.loanTerm), String(l.interestRate),
      fmt(l.monthlyPayment), l.statusText, l.applyDate || '-'
    ])
    const totalAmount = loans.value.reduce((s, l) => s + (parseFloat(l.loanAmount) || 0), 0)
    const pendingCount = loans.value.filter(l => l.status === 1).length
    const approvedCount = loans.value.filter(l => l.status === 2).length
    const rejectedCount = loans.value.filter(l => l.status === 0).length
    await exportICBCReport({
      title: '借款管理报表',
      orientation: 'landscape',
      columns: cols,
      rows,
      summaryItems: [
        { label: '借款总数：', value: `${loans.value.length} 笔` },
        { label: '借款总金额：', value: fmt(totalAmount) },
        { label: '待审批：', value: `${pendingCount} 笔` },
        { label: '已批准：', value: `${approvedCount} 笔` },
        { label: '已拒绝：', value: `${rejectedCount} 笔` }
      ],
      filename: genFilename('借款管理报表')
    })
  } catch (e) {
    console.error('PDF导出失败:', e)
    message.error('PDF导出失败，请重试')
  }
}
</script>

<style scoped>
.admin-loans { max-width: 1600px; }
.card-main { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.card-main :deep(.ant-card-body) { padding: 24px; }
.amount-text { font-weight: 600; color: #1A1A1A; }
.text-muted { color: #ccc; }
</style>
