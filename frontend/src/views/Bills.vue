<template>
  <div class="bills-page animate-fadeInUp">
    <!-- 页面头部 -->
    <PageHeader title="我的账单" subtitle="月度账单管理与导出" />

    <!-- 账单列表 -->
    <a-card :bordered="false" class="bills-card">
      <a-spin :spinning="loading">
        <a-table
          v-if="bills.length > 0"
          :columns="columns"
          :data-source="bills"
          row-key="id"
          :pagination="{ pageSize: 10, showTotal: total => `共 ${total} 条` }"
          class="bills-table"
        >
          <template #bodyCell="{ column, record }">
            <!-- 账单编号 -->
            <template v-if="column.key === 'no'">
              <span class="bill-no">{{ record.billNo }}</span>
            </template>

            <!-- 收入列 -->
            <template v-if="column.key === 'income'">
              <span class="amount-negative">+¥{{ formatMoney(record.totalIncome) }}</span>
            </template>

            <!-- 支出列 -->
            <template v-if="column.key === 'expense'">
              <span class="amount-positive">-¥{{ formatMoney(record.totalExpense) }}</span>
            </template>

            <!-- 期末余额 -->
            <template v-if="column.key === 'balance'">
              <span class="amount-balance">¥{{ formatMoney(record.endBalance) }}</span>
            </template>

            <!-- 状态 -->
            <template v-if="column.key === 'status'">
              <a-tag
                :class="record.status === 1 ? 'status-badge active' : record.status === 2 ? 'status-badge processing' : ''"
                :color="record.status === 1 ? 'green' : record.status === 2 ? 'blue' : 'default'"
              >
                {{ record.statusName }}
              </a-tag>
            </template>

            <!-- 操作 -->
            <template v-if="column.key === 'action'">
              <a-space :size="8">
                <a-button type="link" size="small" @click="viewDetail(record)">
                  <EyeOutlined /> 查看
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  :loading="exportingId === record.id"
                  class="btn-download"
                  @click="handleExportBill(record)"
                >
                  <DownloadOutlined /> 下载PDF
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
        <a-empty v-else description="暂无账单记录" />
      </a-spin>
    </a-card>

    <!-- 账单详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="账单详情"
      :footer="null"
      width="640px"
      :destroy-on-close="true"
      class="bill-detail-modal"
    >
      <div ref="billDetailRef" class="bill-detail-content">
        <a-descriptions v-if="selectedBill" :column="2" bordered size="middle" class="bill-descriptions">
          <a-descriptions-item label="账单编号" :span="2">
            <span class="detail-bill-no">{{ selectedBill.billNo }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="账单月份">
            <span class="detail-month">{{ selectedBill.billMonth }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag
              :class="selectedBill.status === 1 ? 'status-badge active' : selectedBill.status === 2 ? 'status-badge processing' : ''"
              :color="selectedBill.status === 1 ? 'green' : 'blue'"
            >
              {{ selectedBill.statusName }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="期初余额">
            <span class="detail-amount">¥{{ formatMoney(selectedBill.beginBalance) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="期末余额">
            <span class="detail-amount">¥{{ formatMoney(selectedBill.endBalance) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="总收入">
            <span class="amount-negative" style="font-weight: 700;">+¥{{ formatMoney(selectedBill.totalIncome) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="总支出">
            <span class="amount-positive" style="font-weight: 700;">-¥{{ formatMoney(selectedBill.totalExpense) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="生成时间" :span="2">
            {{ selectedBill.generatedAt || selectedBill.createdAt }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 弹窗底部导出按钮 -->
      <div class="modal-footer">
        <a-button
          type="primary"
          class="btn-print-pdf"
          :loading="detailExportLoading"
          @click="handleExportDetailPdf"
        >
          <PrinterOutlined /> 打印/导出PDF
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBills } from '@/api/index'
import { DownloadOutlined, EyeOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { exportBillPdf } from '@/utils/pdf'
import PageHeader from '@/components/PageHeader.vue'

const loading = ref(false)
const bills = ref([])
const detailVisible = ref(false)
const selectedBill = ref(null)
const billDetailRef = ref(null)
const exportingId = ref(null)
const detailExportLoading = ref(false)

const columns = [
  { title: '账单编号', dataIndex: 'billNo', key: 'no', ellipsis: true, width: 200 },
  { title: '账单月份', dataIndex: 'billMonth', key: 'month', width: 120 },
  { title: '收入', key: 'income', width: 140 },
  { title: '支出', key: 'expense', width: 140 },
  { title: '期末余额', key: 'balance', width: 140 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await getBills()
    bills.value = res.data || []
  } finally {
    loading.value = false
  }
})

function viewDetail(bill) {
  selectedBill.value = bill
  detailVisible.value = true
}

async function handleExportBill(record) {
  if (!record) return
  exportingId.value = record.id
  try {
    // 先用modal展示详情，然后导出
    selectedBill.value = record
    await new Promise(resolve => setTimeout(resolve, 100))
    if (billDetailRef.value) {
      // 创建一个临时wrapper
      const wrapper = document.createElement('div')
      wrapper.innerHTML = billDetailRef.value.innerHTML
      document.body.appendChild(wrapper)
      try {
        await exportBillPdf({
          element: wrapper,
          billInfo: {
            billNo: record.billNo,
            billMonth: record.billMonth,
            accountNo: record.accountNo || selectedBill.value?.accountNo || '-'
          }
        })
      } finally {
        if (wrapper.parentNode) {
          document.body.removeChild(wrapper)
        }
      }
    }
  } catch (e) {
    console.error('账单PDF导出失败:', e)
  } finally {
    exportingId.value = null
  }
}

async function handleExportDetailPdf() {
  if (!billDetailRef.value || !selectedBill.value) return
  detailExportLoading.value = true
  try {
    await exportBillPdf({
      element: billDetailRef.value,
      billInfo: {
        billNo: selectedBill.value.billNo,
        billMonth: selectedBill.value.billMonth,
        accountNo: selectedBill.value.accountNo || '-'
      }
    })
  } catch (e) {
    console.error('账单详情PDF导出失败:', e)
  } finally {
    detailExportLoading.value = false
  }
}

function formatMoney(val) {
  const num = parseFloat(val) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
.bills-page {
  max-width: 1400px;
}

/* ========== 卡片容器 ========== */
.bills-card {
  border-radius: var(--radius-lg, 12px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, .04);
  border: 1px solid #EBEEF5;
  overflow: hidden;
}

/* ========== 表格 ========== */
.bills-table {
  margin-top: 0;
}

.bills-table :deep(.ant-table-thead > tr > th) {
  background: #FAFAFA;
  color: #1A1A1A;
  font-weight: 600;
  font-size: 13px;
  border-top: 2px solid #C8102E;
  border-bottom: 1px solid #EBEEF5;
  padding: 14px 16px;
}

.bills-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #FFF1F0 !important;
}

.bills-table :deep(.ant-table-tbody > tr > td) {
  padding: 12px 16px;
  border-bottom: 1px solid #EBEEF5;
  color: #1A1A1A;
  font-size: 13px;
}

.bills-table :deep(.ant-pagination) {
  margin-top: 16px;
}

/* ========== 账单编号 ========== */
.bill-no {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 12px;
  color: #666;
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

.amount-balance {
  color: #1A1A1A;
  font-weight: 600;
  font-size: 14px;
}

/* ========== 下载按钮 ========== */
.btn-download {
  color: #C8102E !important;
  font-weight: 500;
}

.btn-download:hover {
  color: #9B0023 !important;
}

/* ========== 状态标签 ========== */
.status-badge {
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 12px;
}

/* ========== Modal 详情弹窗 ========== */
.bill-detail-modal :deep(.ant-modal-header) {
  border-bottom: 2px solid #C8102E;
  padding: 20px 24px 16px;
}

.bill-detail-modal :deep(.ant-modal-title) {
  font-size: 18px;
  font-weight: 700;
  color: #1A1A1A;
}

.bill-detail-modal :deep(.ant-modal-body) {
  padding: 24px;
}

.bill-detail-content {
  margin-bottom: 0;
}

.bill-descriptions :deep(.ant-descriptions-item-label) {
  background: #FAFAFA;
  font-weight: 600;
  color: #666;
}

.bill-descriptions :deep(.ant-descriptions-item-content) {
  color: #1A1A1A;
}

.detail-bill-no {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 13px;
  color: #C8102E;
  font-weight: 600;
}

.detail-month {
  font-weight: 600;
  color: #1A1A1A;
}

.detail-amount {
  font-weight: 600;
  color: #1A1A1A;
}

/* ========== Modal 底部导出按钮 ========== */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
}

.btn-print-pdf {
  background: #C8102E;
  border-color: #C8102E;
  border-radius: 8px;
  font-weight: 500;
  height: 38px;
  padding: 0 20px;
  transition: all .3s ease;
}

.btn-print-pdf:hover {
  background: #9B0023;
  border-color: #9B0023;
  box-shadow: 0 4px 14px rgba(200, 16, 46, .25);
}

/* ========== PDF导出时隐藏非必要元素 ========== */
:deep(.pdf-exporting) .ant-pagination,
:deep(.pdf-exporting) .btn-print-pdf,
:deep(.pdf-exporting) .modal-footer {
  display: none !important;
}
</style>
