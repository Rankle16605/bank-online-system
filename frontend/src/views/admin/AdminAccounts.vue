<template>
  <div class="admin-accounts">
    <PageHeader title="账户管理" subtitle="查看所有用户账户，管理充值">
      <template #extra>
        <a-button @click="exportAccountsPDF" :icon="h(FilePdfOutlined)">导出PDF</a-button>
      </template>
    </PageHeader>

    <a-card :bordered="false" class="card-main">
      <a-row :gutter="16" style="margin-bottom:16px">
        <a-col :span="6">
          <a-input v-model:value="searchText" placeholder="搜索账户号/用户名" allow-clear />
        </a-col>
        <a-col :span="4">
          <a-select v-model:value="typeFilter" placeholder="账户类型" allow-clear style="width:100%">
            <a-select-option :value="null">全部</a-select-option>
            <a-select-option :value="1">储蓄卡</a-select-option>
            <a-select-option :value="2">信用卡</a-select-option>
          </a-select>
        </a-col>
      </a-row>

      <a-table :columns="columns" :data-source="filteredAccounts" :loading="loading"
        :pagination="{ pageSize: 10 }" row-key="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="record.accountType === 1 ? 'blue' : 'gold'">{{ record.accountTypeName }}</a-tag>
          </template>
          <template v-if="column.key === 'balance'">
            <span class="amount-text">¥{{ formatMoney(record.balance) }}</span>
          </template>
          <template v-if="column.key === 'credit'">
            <span v-if="record.accountType === 2">总额¥{{ formatMoney(record.creditLimit) }} / 可用¥{{ formatMoney(record.availableCredit) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '正常' : '冻结' }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="primary" size="small" @click="openRecharge(record)">充值</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="rechargeVisible" title="账户充值" @ok="handleRecharge" :confirmLoading="rechargeLoading" width="420px">
      <a-form layout="vertical">
        <a-form-item label="账户号"><a-input :value="rechargeAccount?.accountNo" disabled /></a-form-item>
        <a-form-item label="用户名"><a-input :value="rechargeAccount?.username" disabled /></a-form-item>
        <a-form-item label="当前余额"><a-input :value="'¥' + formatMoney(rechargeAccount?.balance)" disabled /></a-form-item>
        <a-form-item label="充值金额" required>
          <a-input-number v-model:value="rechargeAmount" :min="1" :max="99999999" style="width:100%" placeholder="请输入充值金额" size="large" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, h } from 'vue'
import { message } from 'ant-design-vue'
import { getAdminAccounts, adminRecharge } from '@/api/index'
import PageHeader from '@/components/PageHeader.vue'
import { FilePdfOutlined } from '@ant-design/icons-vue'
import { exportICBCReport, fmt, genFilename } from '@/utils/pdfExport'

const loading = ref(false); const rechargeLoading = ref(false)
const accounts = ref([]); const searchText = ref(''); const typeFilter = ref(null)
const rechargeVisible = ref(false); const rechargeAccount = ref(null); const rechargeAmount = ref(null)

const columns = [
  { title: '账户号', dataIndex: 'accountNo', width: 170 },
  { title: '用户名', dataIndex: 'username', width: 100 },
  { title: '类型', key: 'type', width: 80 },
  { title: '余额', key: 'balance', width: 140 },
  { title: '额度(总额/可用)', key: 'credit', width: 190 },
  { title: '账单日', dataIndex: 'billingDay', width: 65 },
  { title: '还款日', dataIndex: 'dueDay', width: 65 },
  { title: '状态', key: 'status', width: 65 },
  { title: '操作', key: 'action', width: 80 }
]

const filteredAccounts = computed(() => {
  let list = accounts.value
  if (typeFilter.value) list = list.filter(a => a.accountType === typeFilter.value)
  if (searchText.value) {
    const q = searchText.value.toLowerCase()
    list = list.filter(a => (a.accountNo||'').toLowerCase().includes(q) || (a.username||'').toLowerCase().includes(q))
  }
  return list
})

onMounted(async () => {
  loading.value = true
  try { const res = await getAdminAccounts(); accounts.value = res.data || [] } catch (e) { message.error('加载账户失败') }
  finally { loading.value = false }
})

function openRecharge(r) { rechargeAccount.value = r; rechargeAmount.value = null; rechargeVisible.value = true }

async function handleRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) { message.warning('请输入充值金额'); return }
  rechargeLoading.value = true
  try {
    await adminRecharge({ accountNo: rechargeAccount.value.accountNo, amount: rechargeAmount.value, remark: '管理员充值' })
    message.success('充值成功！'); rechargeVisible.value = false
    loading.value = true
    const res = await getAdminAccounts(); accounts.value = res.data || []
  } catch (e) { message.error(e?.response?.data?.message || '充值失败') }
  finally { rechargeLoading.value = false; loading.value = false }
}

function formatMoney(val) { const num = parseFloat(val) || 0; return num.toLocaleString('zh-CN',{minimumFractionDigits:2,maximumFractionDigits:2}) }

// ==================== PDF 导出 ====================
async function exportAccountsPDF() {
  try {
    const list = filteredAccounts.value
    const cols = ['账户号', '用户名', '类型', '余额(元)', '信用额度', '可用额度', '账单日', '还款日', '状态']
    const rows = list.map(a => [
      a.accountNo, a.username,
      a.accountType === 1 ? '储蓄卡' : '信用卡',
      fmt(a.balance),
      a.accountType === 2 ? fmt(a.creditLimit) : '-',
      a.accountType === 2 ? fmt(a.availableCredit) : '-',
      a.billingDay || '-', a.dueDay || '-',
      a.status === 1 ? '正常' : '冻结'
    ])
    const totalBal = list.reduce((s, a) => s + (parseFloat(a.balance) || 0), 0)
    const savingCount = list.filter(a => a.accountType === 1).length
    const creditCount = list.filter(a => a.accountType === 2).length
    await exportICBCReport({
      title: '账户管理报表',
      orientation: 'landscape',
      columns: cols,
      rows,
      summaryItems: [
        { label: '账户总数：', value: `${list.length} 个` },
        { label: '储蓄卡：', value: `${savingCount} 个` },
        { label: '信用卡：', value: `${creditCount} 个` },
        { label: '总余额：', value: fmt(totalBal) }
      ],
      filename: genFilename('账户管理报表')
    })
  } catch (e) {
    console.error('PDF导出失败:', e)
    message.error('PDF导出失败，请重试')
  }
}
</script>

<style scoped>
.admin-accounts { max-width: 1600px; }
.card-main { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.card-main :deep(.ant-card-body) { padding: 24px; }
.amount-text { font-weight: 600; color: #1A1A1A; }
.text-muted { color: #ccc; }
</style>
