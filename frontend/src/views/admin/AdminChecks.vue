<template>
  <div class="admin-checks">
    <PageHeader title="支票管理" subtitle="开具和管理银行支票" />

    <!-- 顶部操作栏 -->
    <a-card :bordered="false" style="margin-bottom:16px; border-radius:12px; box-shadow:0 2px 8px rgba(0,0,0,.04);">
      <a-row :gutter="16" align="middle">
        <a-col :flex="1">
          <a-space>
            <a-statistic title="已开支票" :value="checks.length" style="margin-right:24px" />
            <a-statistic title="总金额" :value="'¥' + fmtMoney(totalAmount)" />
            <a-statistic title="已兑付" :value="cashedCount" />
            <a-statistic title="待兑付" :value="pendingCount" />
          </a-space>
        </a-col>
        <a-col>
          <a-space>
            <a-button @click="exportChecksPDF" :icon="h(FilePdfOutlined)">导出报表</a-button>
            <a-button type="primary" @click="openCreate" :icon="h(PlusOutlined)">开具新支票</a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 支票列表 -->
    <a-card :bordered="false" style="border-radius:12px; box-shadow:0 2px 8px rgba(0,0,0,.04);">
      <a-table :columns="columns" :data-source="checks" :loading="loading"
        :pagination="{ pageSize: 10 }" row-key="checkNo" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span class="amount-text">¥{{ fmtMoney(record.amount) }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.statusText }}</a-tag>
          </template>
          <template v-if="column.key === 'amountCn'">
            <span style="font-size:11px;color:#888">{{ record.amountCn }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewCheck(record)">详情</a-button>
              <a-button type="link" size="small" @click="exportSinglePDF(record)">打印</a-button>
              <a-popconfirm v-if="record.status === 0"
                title="确认兑付此支票？" ok-text="确认兑付" cancel-text="取消"
                @confirm="cashCheck(record)">
                <a-button type="link" size="small" style="color:#52C41A"><CheckCircleOutlined /></a-button>
              </a-popconfirm>
              <a-popconfirm v-if="record.status === 0"
                title="确认作废此支票？" ok-text="确认作废" cancel-text="取消"
                ok-button-props="{ danger: true }"
                @confirm="voidCheck(record)">
                <a-button type="link" size="small" danger>作废</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 开具支票弹窗 -->
    <a-modal v-model:open="createVisible" title="开具银行支票" width="640px"
      @ok="handleCreate" :confirmLoading="creating" ok-text="确认开具" cancel-text="取消">
      <a-form :model="form" layout="vertical" style="margin-top:16px">
        <!-- 支票预览卡片 -->
        <div class="check-preview">
          <div class="check-header">
            <BankOutlined />
            <span>中国工商银行 现金支票</span>
          </div>
          <div class="check-meta">
            <span>支票号码：{{ previewCheckNo }}</span>
            <span>日期：{{ form.issueDate || '____年__月__日' }}</span>
          </div>
          <div class="check-row">
            <span class="check-label">收款人：</span>
            <span class="check-val">{{ form.payee || '____________________' }}</span>
          </div>
          <div class="check-row">
            <span class="check-label">金额（小写）：</span>
            <span class="check-val amount-val">
              {{ form.amount ? '¥' + fmtMoney(form.amount) : '¥_____________' }}
            </span>
          </div>
          <div class="check-row">
            <span class="check-label">金额（大写）：</span>
            <span class="check-val amount-cn">{{ amountCn || '________________________________________' }}</span>
          </div>
          <div class="check-row">
            <span class="check-label">用&nbsp;&nbsp;&nbsp;&nbsp;途：</span>
            <span class="check-val">{{ form.purpose || '____________________' }}</span>
          </div>
          <div class="check-footer">
            <div>付款行：中国工商银行总行营业部</div>
            <div>出票人签章：<span class="stamp">ICBC 中国工商银行</span></div>
          </div>
        </div>
        <a-divider />
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="支票号码" required>
              <a-input v-model:value="form.checkNo" disabled :placeholder="previewCheckNo" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="出票日期" required>
              <a-date-picker v-model:value="form.issueDate" style="width:100%" placeholder="选择日期" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="收款人全称" required>
          <a-input v-model:value="form.payee" placeholder="请输入收款单位或个人全称" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="金额（元）" required>
              <a-input-number v-model:value="form.amount" :min="0.01" :max="999999999.99" :precision="2"
                style="width:100%" placeholder="请输入支票金额" size="large" @change="onAmountChange" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="金额大写">
              <a-input :value="amountCn" disabled placeholder="自动转换" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="用途">
          <a-input v-model:value="form.purpose" placeholder="如：货款、工程款、劳务费等" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" placeholder="可选备注信息" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 支票详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="支票详情" :footer="null" width="560px">
      <div class="check-detail" v-if="currentCheck">
        <div class="check-preview" style="margin-top:0">
          <div class="check-header"><BankOutlined /><span>中国工商银行 现金支票</span></div>
          <div class="check-meta">
            <span>支票号码：{{ currentCheck.checkNo }}</span>
            <span>日期：{{ currentCheck.issueDate }}</span>
          </div>
          <div class="check-row"><span class="check-label">收款人：</span><span class="check-val">{{ currentCheck.payee }}</span></div>
          <div class="check-row"><span class="check-label">金额（小写）：</span><span class="check-val amount-val">¥{{ fmtMoney(currentCheck.amount) }}</span></div>
          <div class="check-row"><span class="check-label">金额（大写）：</span><span class="check-val amount-cn">{{ currentCheck.amountCn }}</span></div>
          <div class="check-row"><span class="check-label">用&nbsp;&nbsp;&nbsp;&nbsp;途：</span><span class="check-val">{{ currentCheck.purpose || '-' }}</span></div>
          <div class="check-footer">
            <div>付款行：中国工商银行总行营业部</div>
            <div>出票人签章：<span class="stamp">ICBC 中国工商银行</span></div>
          </div>
          <div style="margin-top:12px; padding-top:8px; border-top:1px dashed #ddd; display:flex; justify-content:space-between; font-size:12px; color:#999">
            <span>状态：{{ currentCheck.statusText }}</span>
            <span>{{ currentCheck.remark || '' }}</span>
          </div>
        </div>
        <a-button block type="primary" style="margin-top:12px" @click="exportSinglePDF(currentCheck)">
          <FilePdfOutlined /> 打印支票单
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, h, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'
import { BankOutlined, PlusOutlined, FilePdfOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import { exportICBCReport, exportCheckPDF, fmt, genFilename } from '@/utils/pdfExport'

// ==================== 状态 ====================
const loading = ref(false)
const creating = ref(false)
const checks = ref([])
const createVisible = ref(false)
const detailVisible = ref(false)
const currentCheck = ref(null)

const form = ref({
  checkNo: '',
  issueDate: null,
  payee: '',
  amount: null,
  purpose: '',
  remark: ''
})

// 模拟的已开具支票数据
const mockChecks = [
  { checkNo: 'CHK20260601001', issueDate: '2026-06-01', payee: '华为技术有限公司', amount: 1580000.00, amountCn: '壹佰伍拾捌万元整', purpose: '设备采购款', status: 2, statusText: '已兑付', remark: '合同HT-2026-0588', cashedDate: '2026-06-03' },
  { checkNo: 'CHK20260603002', issueDate: '2026-06-03', payee: '中兴通讯股份有限公司', amount: 820000.00, amountCn: '捌拾贰万元整', purpose: '通信设备款', status: 2, statusText: '已兑付', remark: '采购订单PO-2026-1123', cashedDate: '2026-06-05' },
  { checkNo: 'CHK20260608003', issueDate: '2026-06-08', payee: '北京建工集团', amount: 3560000.00, amountCn: '叁佰伍拾陆万元整', purpose: '工程款', status: 0, statusText: '待兑付', remark: '基建项目二期' },
  { checkNo: 'CHK20260610004', issueDate: '2026-06-10', payee: '腾讯科技有限公司', amount: 285000.00, amountCn: '贰拾捌万伍仟元整', purpose: '云服务费', status: 0, statusText: '待兑付', remark: '2026年度云服务' },
  { checkNo: 'CHK20260612005', issueDate: '2026-06-12', payee: '阿里巴巴集团', amount: 520000.00, amountCn: '伍拾贰万元整', purpose: '技术服务费', status: 0, statusText: '待兑付', remark: '年度技术服务合同' },
  { checkNo: 'CHK20260615006', issueDate: '2026-06-15', payee: '中国石油天然气集团', amount: 4200000.00, amountCn: '肆佰贰拾万元整', purpose: '货款', status: 1, statusText: '已打印', remark: '原油采购2026-Q2' },
  { checkNo: 'CHK20260615007', issueDate: '2026-06-15', payee: '京东集团', amount: 168000.00, amountCn: '壹拾陆万捌仟元整', purpose: '办公设备采购', status: 0, statusText: '待兑付', remark: '' },
  { checkNo: 'CHK20260616008', issueDate: '2026-06-16', payee: '上海浦东发展银行', amount: 9800000.00, amountCn: '玖佰捌拾万元整', purpose: '同业拆借', status: 3, statusText: '已作废', remark: '合同终止' },
]

const columns = [
  { title: '支票号码', dataIndex: 'checkNo', width: 150 },
  { title: '出票日期', dataIndex: 'issueDate', width: 100 },
  { title: '收款人', dataIndex: 'payee', width: 160 },
  { title: '金额', key: 'amount', width: 130 },
  { title: '金额(大写)', key: 'amountCn', width: 170, ellipsis: true },
  { title: '用途', dataIndex: 'purpose', width: 120 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 220 }
]

// ==================== 计算属性 ====================
const previewCheckNo = computed(() => {
  const d = new Date()
  const n = checks.value.length + 1
  return `CHK${d.getFullYear()}${pad(d.getMonth()+1)}${pad(d.getDate())}${pad(n, 3)}`
})

const amountCn = computed(() => {
  if (!form.value.amount) return ''
  return numberToChinese(form.value.amount)
})

const totalAmount = computed(() => checks.value.reduce((s, c) => s + (c.amount || 0), 0))
const cashedCount = computed(() => checks.value.filter(c => c.status === 2).length)
const pendingCount = computed(() => checks.value.filter(c => c.status === 0).length)

// ==================== 生命周期 ====================
onMounted(() => {
  // 模拟加载数据
  loading.value = true
  setTimeout(() => {
    checks.value = [...mockChecks]
    loading.value = false
  }, 400)
})

// ==================== 操作函数 ====================
function openCreate() {
  form.value = {
    checkNo: '',
    issueDate: null,
    payee: '',
    amount: null,
    purpose: '',
    remark: ''
  }
  createVisible.value = true
}

async function handleCreate() {
  if (!form.value.payee || !form.value.amount) {
    message.warning('请填写收款人和金额')
    return
  }
  creating.value = true
  // 模拟保存
  await new Promise(r => setTimeout(r, 600))
  const d = form.value.issueDate ? new Date(form.value.issueDate) : new Date()
  const dateStr = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
  checks.value.unshift({
    checkNo: previewCheckNo.value,
    issueDate: dateStr,
    payee: form.value.payee,
    amount: form.value.amount,
    amountCn: amountCn.value,
    purpose: form.value.purpose || '',
    status: 0,
    statusText: '待兑付',
    remark: form.value.remark || ''
  })
  message.success(`支票 ${previewCheckNo.value} 开具成功！`)
  createVisible.value = false
  creating.value = false
}

function viewCheck(record) {
  currentCheck.value = record
  detailVisible.value = true
}

function cashCheck(record) {
  record.status = 2
  record.statusText = '已兑付'
  record.cashedDate = new Date().toISOString().split('T')[0]
  message.success('支票已兑付')
}

function voidCheck(record) {
  record.status = 3
  record.statusText = '已作废'
  message.success('支票已作废')
}

function onAmountChange() {
  // 触发大写转换计算
}

// ==================== PDF 导出 ====================
async function exportChecksPDF() {
  try {
    const colHeaders = ['支票号码', '出票日期', '收款人', '金额(元)', '金额(大写)', '用途', '状态', '备注']
    const rows = checks.value.map(c => [
      c.checkNo,
      c.issueDate,
      c.payee,
      fmt(c.amount),
      c.amountCn,
      c.purpose || '-',
      c.statusText,
      c.remark || '-'
    ])
    await exportICBCReport({
      title: '支票管理报表',
      orientation: 'landscape',
      columns: colHeaders,
      rows,
      summaryItems: [
        { label: '支票总数：', value: `${checks.value.length} 张` },
        { label: '总金额：', value: fmt(totalAmount.value) },
        { label: '已兑付：', value: `${cashedCount.value} 张` },
        { label: '待兑付：', value: `${pendingCount.value} 张` }
      ],
      filename: genFilename('支票管理报表')
    })
  } catch (e) {
    console.error('PDF导出失败:', e)
    message.error('PDF导出失败，请重试')
  }
}

async function exportSinglePDF(record) {
  try {
    await exportCheckPDF(record)
  } catch (e) {
    console.error('支票PDF导出失败:', e)
    message.error('支票打印失败，请重试')
  }
}

// ==================== 工具函数 ====================
function statusColor(s) {
  const m = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'red' }
  return m[s] || 'default'
}

function fmtMoney(val) {
  const num = parseFloat(val) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function pad(n, len = 2) {
  return String(n).padStart(len, '0')
}

/**
 * 数字转中文大写金额
 */
function numberToChinese(num) {
  if (!num || num <= 0) return ''
  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const radices = ['', '拾', '佰', '仟']
  const bigRadices = ['', '万', '亿', '万亿']
  const decimals = ['角', '分']

  let n = parseFloat(num)
  let result = ''

  // 整数部分
  let integerPart = Math.floor(n)
  if (integerPart === 0) {
    result = '零'
  } else {
    let zeroCount = 0
    let str = ''
    let unitIndex = 0
    while (integerPart > 0) {
      const section = integerPart % 10000
      let sectionStr = ''
      let sectionZero = false
      for (let i = 0; i < 4; i++) {
        const digit = section % 10
        if (digit === 0) {
          sectionZero = true
        } else {
          if (sectionZero && sectionStr) sectionStr = '零' + sectionStr
          sectionStr = digits[digit] + radices[i] + sectionStr
          sectionZero = false
        }
        section = Math.floor(section / 10)
        if (section === 0) break
      }
      if (sectionStr) {
        str = sectionStr + bigRadices[unitIndex] + (zeroCount && str ? '零' : '') + str
        zeroCount = 0
      } else {
        zeroCount = 1
      }
      integerPart = Math.floor(integerPart / 10000)
      unitIndex++
    }
    result = str
  }
  result += '元'

  // 小数部分
  const decPart = Math.round((n - Math.floor(n)) * 100)
  if (decPart === 0) {
    result += '整'
  } else {
    const jiao = Math.floor(decPart / 10)
    const fen = decPart % 10
    if (jiao > 0) result += digits[jiao] + '角'
    if (fen > 0) result += digits[fen] + '分'
  }

  return result
}
</script>

<style scoped>
.admin-checks { max-width: 1600px; }

.amount-text { font-weight: 700; color: #C8102E; font-size: 14px; }

/* 支票预览卡片 */
.check-preview {
  background: linear-gradient(135deg, #FFFBF0, #FFF8E1, #FFFBF0);
  border: 2px solid #C4A265;
  border-radius: 12px;
  padding: 20px 24px;
  margin-top: 8px;
  position: relative;
  overflow: hidden;
}
.check-preview::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 200'%3E%3Ctext x='100' y='120' text-anchor='middle' font-size='80' fill='rgba(196,162,101,0.06)' font-weight='bold'%3EICBC%3C/text%3E%3C/svg%3E") center/contain no-repeat;
  pointer-events: none;
}
.check-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #C8102E;
  margin-bottom: 12px;
  border-bottom: 2px solid #C8102E;
  padding-bottom: 10px;
}
.check-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #888;
  margin-bottom: 14px;
}
.check-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}
.check-label {
  color: #666;
  white-space: nowrap;
  width: 110px;
}
.check-val {
  color: #1A1A1A;
  font-weight: 500;
  border-bottom: 1px dashed #ddd;
  padding: 2px 8px;
  min-width: 200px;
}
.amount-val {
  color: #C8102E;
  font-weight: 700;
  font-size: 16px;
}
.amount-cn {
  font-size: 13px;
  color: #333;
  font-weight: 600;
}
.check-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 8px;
  border-top: 1px dashed #ddd;
  font-size: 12px;
  color: #666;
}
.stamp {
  color: #C8102E;
  font-weight: 700;
  font-size: 14px;
}
.check-detail .stamp {
  border: 2px solid #C8102E;
  padding: 2px 12px;
  border-radius: 4px;
}
</style>
