<template>
  <div class="history-page animate-fadeInUp">
    <!-- 页面头部 -->
    <PageHeader title="交易记录" subtitle="查看所有账户流水明细" />

    <!-- 筛选区域 -->
    <a-card :bordered="false" class="history-card">
      <div class="filter-bar">
        <a-space :size="16" wrap>
          <a-select
            v-model:value="filterAccount"
            placeholder="选择账户"
            style="width: 220px"
            @change="fetchHistory"
          >
            <a-select-option v-for="acc in accounts" :key="acc.accountNo" :value="acc.accountNo">
              {{ maskAccountNo(acc.accountNo) }}
            </a-select-option>
          </a-select>
          <a-range-picker v-model:value="dateRange" @change="fetchHistory" />
          <a-button @click="fetchHistory">
            <ReloadOutlined /> 刷新
          </a-button>
          <a-button
            class="btn-export"
            :loading="exportLoading"
            @click="handleExportPdf"
          >
            <DownloadOutlined /> 导出PDF
          </a-button>
        </a-space>
      </div>

      <!-- 交易列表 -->
      <div ref="tableRef">
        <a-table
          :columns="columns"
          :data-source="transactions"
          :loading="loading"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
          class="history-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag
                :class="record.status === 1 ? 'status-badge active' : record.status === 2 ? 'status-badge processing' : ''"
                :color="record.status === 1 ? 'green' : record.status === 2 ? 'blue' : 'red'"
              >
                {{ record.statusName }}
              </a-tag>
            </template>
            <template v-if="column.key === 'amount'">
              <span
                :class="record.fromAccount === filterAccount ? 'amount-positive' : 'amount-negative'"
              >
                {{ record.fromAccount === filterAccount ? '-' : '+' }}¥{{ formatMoney(record.amount) }}
              </span>
            </template>
            <template v-if="column.key === 'type'">
              <a-tag>{{ record.transactionTypeName }}</a-tag>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAccounts, getTransactionHistory } from '@/api/index'
import { ReloadOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { exportTransactionPdf } from '@/utils/pdf'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const exportLoading = ref(false)
const accounts = ref([])
const transactions = ref([])
const filterAccount = ref('')
const dateRange = ref(null)
const tableRef = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: total => `共 ${total} 条`
})

const columns = [
  { title: '交易流水号', dataIndex: 'transactionNo', key: 'no', ellipsis: true },
  { title: '交易时间', dataIndex: 'createdAt', key: 'time', width: 180 },
  { title: '类型', key: 'type', width: 100 },
  { title: '对方账户', key: 'counterparty', width: 180, ellipsis: true },
  { title: '金额', key: 'amount', width: 150 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
]

onMounted(async () => {
  try {
    const res = await getAccounts()
    accounts.value = res.data || []
    if (accounts.value.length > 0) {
      filterAccount.value = accounts.value[0].accountNo
      await fetchHistory()
    }
  } catch (e) {}
})

async function fetchHistory() {
  if (!filterAccount.value) return
  loading.value = true
  try {
    const res = await getTransactionHistory(
      filterAccount.value,
      pagination.current - 1,
      pagination.pageSize
    )
    transactions.value = (res.data || []).map(t => ({
      ...t,
      counterparty: t.fromAccount === filterAccount.value ? t.toAccount : t.fromAccount
    }))
    pagination.total = res.total || transactions.value.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchHistory()
}

async function handleExportPdf() {
  if (!tableRef.value || transactions.value.length === 0) return
  exportLoading.value = true
  try {
    const dateStr = dateRange.value
      ? `${dateRange.value[0]?.format('YYYY-MM-DD')} ~ ${dateRange.value[1]?.format('YYYY-MM-DD')}`
      : '全部'
    await exportTransactionPdf({
      element: tableRef.value,
      accountNo: maskAccountNo(filterAccount.value),
      dateRange: dateStr,
      transactions: transactions.value
    })
  } catch (e) {
    console.error('PDF导出失败:', e)
  } finally {
    exportLoading.value = false
  }
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
.history-page {
  max-width: 1400px;
}

/* ========== 卡片容器 ========== */
.history-card {
  border-radius: var(--radius-lg, 12px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
  border: 1px solid #EBEEF5;
  overflow: hidden;
}

/* ========== 筛选栏 ========== */
.filter-bar {
  margin-bottom: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #EBEEF5;
}

.filter-bar :deep(.ant-select-selector),
.filter-bar :deep(.ant-picker) {
  border-radius: 8px;
}

/* ========== 导出按钮 ========== */
.btn-export {
  border-color: #C8102E;
  color: #C8102E;
  border-radius: 8px;
  font-weight: 500;
  transition: all .3s ease;
}

.btn-export:hover {
  color: #fff;
  background: #C8102E;
  border-color: #C8102E;
  box-shadow: 0 4px 14px rgba(200, 16, 46, .25);
}

/* ========== 表格 ========== */
.history-table {
  margin-top: 4px;
}

.history-table :deep(.ant-table-thead > tr > th) {
  background: #FAFAFA;
  color: #1A1A1A;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 2px solid #C8102E;
  padding: 14px 16px;
}

.history-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #FFF1F0 !important;
}

.history-table :deep(.ant-table-tbody > tr > td) {
  padding: 12px 16px;
  border-bottom: 1px solid #EBEEF5;
  color: #1A1A1A;
  font-size: 13px;
}

.history-table :deep(.ant-pagination) {
  margin-top: 16px;
}

/* ========== 金额样式 ========== */
.amount-positive {
  color: #FF4D4F;
  font-weight: 700;
  font-size: 14px;
}

.amount-negative {
  color: #52C41A;
  font-weight: 700;
  font-size: 14px;
}

/* ========== 状态标签 ========== */
.status-badge {
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 12px;
}

/* ========== PDF导出时隐藏分页和按钮 ========== */
:deep(.pdf-exporting) .ant-pagination,
:deep(.pdf-exporting) .btn-export {
  display: none !important;
}
</style>
