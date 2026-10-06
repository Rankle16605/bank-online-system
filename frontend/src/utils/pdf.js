/**
 * PDF导出工具 - ICBC智能在线银行系统
 * 基于html2pdf.js，将HTML元素导出为A4 PDF文件
 * 遵循阿里巴巴开发规范
 */
import html2pdf from 'html2pdf.js'

const DEFAULT_OPTIONS = {
  margin: [10, 10, 10, 10],
  filename: 'ICBC-导出.pdf',
  image: { type: 'jpeg', quality: 0.98 },
  html2canvas: {
    scale: 2,
    useCORS: true,
    letterRendering: true,
    logging: false,
    backgroundColor: '#ffffff'
  },
  jsPDF: {
    unit: 'mm',
    format: 'a4',
    orientation: 'portrait'
  },
  pagebreak: { mode: ['avoid-all', 'css', 'legacy'] }
}

/**
 * 将指定DOM元素导出为PDF
 * @param {HTMLElement|string} element - DOM元素或CSS选择器
 * @param {Object} options - 自定义选项
 * @param {string} filename - 文件名
 */
export async function exportToPdf(element, options = {}, filename = '') {
  const target = typeof element === 'string'
    ? document.querySelector(element)
    : element

  if (!target) {
    console.warn('PDF导出失败：找不到目标元素')
    return
  }

  const opt = {
    ...DEFAULT_OPTIONS,
    ...options,
    filename: filename || `ICBC-${formatDate(new Date())}.pdf`
  }

  try {
    // 添加导出类名便于样式控制
    target.classList.add('pdf-exporting')
    await html2pdf().set(opt).from(target).save()
    target.classList.remove('pdf-exporting')
  } catch (error) {
    target.classList.remove('pdf-exporting')
    console.error('PDF导出失败:', error)
    throw error
  }
}

/**
 * 导出交易记录为PDF
 * @param {Object} params - { element, accountNo, dateRange, transactions }
 */
export async function exportTransactionPdf({ element, accountNo, dateRange, transactions }) {
  const header = buildTransactionHeader(accountNo, dateRange)
  const wrapper = document.createElement('div')
  wrapper.className = 'pdf-export-wrapper'
  wrapper.innerHTML = header + element.innerHTML
  document.body.appendChild(wrapper)

  try {
    await exportToPdf(wrapper, {
      filename: `ICBC-交易记录-${formatDate(new Date())}.pdf`,
      margin: [15, 12, 15, 12]
    })
  } finally {
    document.body.removeChild(wrapper)
  }
}

/**
 * 导出账单详情为PDF
 * @param {Object} params - { element, billInfo }
 */
export async function exportBillPdf({ element, billInfo = {} }) {
  const header = buildBillHeader(billInfo)
  const wrapper = document.createElement('div')
  wrapper.className = 'pdf-export-wrapper'
  wrapper.innerHTML = header + element.outerHTML
  document.body.appendChild(wrapper)

  try {
    await exportToPdf(wrapper, {
      filename: `ICBC-账单-${billInfo.billMonth || formatDate(new Date())}.pdf`,
      margin: [15, 12, 15, 12]
    })
  } finally {
    document.body.removeChild(wrapper)
  }
}

function buildTransactionHeader(accountNo, dateRange) {
  const dateStr = dateRange || `${formatDate(new Date())}`
  return `
    <div class="pdf-header">
      <div class="pdf-header-top">
        <div class="pdf-logo">ICBC 中国工商银行</div>
        <div class="pdf-title">交易记录明细</div>
      </div>
      <div class="pdf-header-info">
        <div class="pdf-info-row"><span>账户号码：</span><span>${accountNo}</span></div>
        <div class="pdf-info-row"><span>查询日期：</span><span>${dateStr}</span></div>
        <div class="pdf-info-row"><span>打印时间：</span><span>${formatDateTime(new Date())}</span></div>
      </div>
    </div>
  `
}

function buildBillHeader(billInfo) {
  return `
    <div class="pdf-header">
      <div class="pdf-header-top">
        <div class="pdf-logo">ICBC 中国工商银行</div>
        <div class="pdf-title">电子账单</div>
      </div>
      <div class="pdf-header-info">
        <div class="pdf-info-row"><span>账单编号：</span><span>${billInfo.billNo || '-'}</span></div>
        <div class="pdf-info-row"><span>账单月份：</span><span>${billInfo.billMonth || '-'}</span></div>
        <div class="pdf-info-row"><span>账户号码：</span><span>${billInfo.accountNo || '-'}</span></div>
        <div class="pdf-info-row"><span>打印时间：</span><span>${formatDateTime(new Date())}</span></div>
      </div>
    </div>
  `
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatDateTime(date) {
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${formatDate(date)} ${h}:${min}:${s}`
}
