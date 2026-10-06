import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'

// ==================== ICBC 品牌色 ====================
const RED   = '#C8102E'
const DARK  = '#1A1A2E'
const GOLD  = '#C4A265'

// ==================== 通用 HTML→PDF 渲染 ====================

/**
 * 将 HTML 元素渲染为 Canvas，然后嵌入 jsPDF
 * @param {jsPDF} doc
 * @param {HTMLElement} element
 * @param {number} startY   PDF 中的起始 Y 坐标(mm)
 * @returns {number} 渲染后的结束 Y 坐标
 */
async function renderHTMLToPDF(doc, element, startY = 0) {
  const pageW = doc.internal.pageSize.getWidth()
  const margin = 15
  const contentW = pageW - margin * 2  // 可用内容宽度(mm)

  const canvas = await html2canvas(element, {
    scale: 2,
    useCORS: true,
    logging: false,
    backgroundColor: '#ffffff'
  })

  const imgW = contentW
  const imgH = (canvas.height / canvas.width) * imgW

  // 分页处理
  const pageH = doc.internal.pageSize.getHeight()
  const maxContentH = pageH - margin - 6  // 底部留红条空间
  let remainingH = imgH
  let srcY = 0
  let currentY = startY

  while (remainingH > 0) {
    const sliceH = Math.min(remainingH, maxContentH - (currentY > margin ? margin : currentY))
    const srcH = (sliceH / imgH) * canvas.height

    // 新建页面（除首次外）
    if (currentY > margin && srcY > 0) {
      doc.addPage()
      currentY = margin
    }

    // 截取 canvas 部分
    const sliceCanvas = document.createElement('canvas')
    sliceCanvas.width = canvas.width
    sliceCanvas.height = Math.ceil(srcH)
    const ctx = sliceCanvas.getContext('2d')
    ctx.drawImage(canvas, 0, srcY, canvas.width, srcH, 0, 0, canvas.width, srcH)

    doc.addImage(sliceCanvas.toDataURL('image/jpeg', 0.95), 'JPEG', margin, currentY, imgW, sliceH)

    srcY += srcH
    remainingH -= sliceH
    currentY = margin + sliceH
  }

  return currentY
}

/**
 * 为所有页面添加 ICBC 页脚红条和页码
 */
function addFooter(doc) {
  const pageW = doc.internal.pageSize.getWidth()
  const pageH = doc.internal.pageSize.getHeight()
  const totalPages = doc.getNumberOfPages()

  for (let i = 1; i <= totalPages; i++) {
    doc.setPage(i)
    // 底部红条
    doc.setFillColor(200, 16, 46)
    doc.rect(0, pageH - 6, pageW, 6, 'F')
    // 页码
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(7)
    doc.setTextColor(255, 255, 255)
    doc.text(`ICBC 中国工商银行 · 第 ${i}/${totalPages} 页`, pageW - 15, pageH - 1.5, { align: 'right' })
  }
}

// ==================== 生成文件名 ====================
function pad(n) { return n < 10 ? '0' + n : '' + n }

export function genFilename(prefix) {
  const d = new Date()
  return `${prefix}_${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}_${pad(d.getHours())}${pad(d.getMinutes())}${pad(d.getSeconds())}.pdf`
}

/** 格式化金额 */
export function fmt(v) {
  const n = parseFloat(v) || 0
  return '¥' + n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// ==================== 构建 ICBC 报表外壳 HTML ====================
/**
 * 构建带 ICBC 抬头的报表容器
 * @param {string} title   报表名称
 * @param {string} innerHTML  表格等核心内容
 * @param {Array}  summaryItems   [{ label, value }] 汇总项（可选）
 */
function buildReportHTML(title, innerHTML, summaryItems = null, signFields = true) {
  const now = new Date()
  const ts = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`

  let summaryHTML = ''
  if (summaryItems && summaryItems.length > 0) {
    summaryHTML = `
      <div style="margin-top:16px;padding:12px 16px;background:#FAFAFC;border-left:3px solid ${RED};border-radius:4px;display:flex;flex-wrap:wrap;gap:8px 32px;">
        ${summaryItems.map(item => `
          <div style="display:flex;align-items:center;gap:4px;">
            <span style="color:${DARK};font-weight:600;font-size:12px;">${item.label}</span>
            <span style="color:#505050;font-size:12px;">${item.value}</span>
          </div>
        `).join('')}
      </div>
    `
  }

  let signHTML = ''
  if (signFields) {
    signHTML = `
      <div style="margin-top:20px;display:flex;justify-content:space-between;color:#888;font-size:12px;">
        <span>制表人：_______________</span>
        <span>复核人：_______________</span>
        <span>审批人：_______________</span>
      </div>
      <div style="margin-top:8px;text-align:center;color:#aaa;font-size:10px;">
        ICBC 中国工商银行  |  本报表由系统自动生成，仅供内部管理使用
      </div>
    `
  }

  return `
    <div style="font-family:'Microsoft YaHei','PingFang SC','Hiragino Sans GB','WenQuanYi Micro Hei',sans-serif;background:#fff;padding:0;">
      <!-- 顶部红条 -->
      <div style="height:6px;background:${RED};"></div>

      <!-- ICBC Logo行 -->
      <div style="display:flex;justify-content:space-between;align-items:flex-start;padding:10px 0 0 0;">
        <div>
          <div style="font-size:18px;font-weight:700;color:${RED};">ICBC</div>
          <div style="font-size:9px;color:#666;">中国工商银行</div>
          <div style="font-size:7px;color:#999;">INDUSTRIAL AND COMMERCIAL BANK OF CHINA</div>
        </div>
        <div style="text-align:right;font-size:8px;color:#aaa;">
          ${ts}
        </div>
      </div>

      <!-- 分隔线 -->
      <div style="height:1px;background:${RED};margin:8px 0 0 0;"></div>

      <!-- 标题 -->
      <div style="font-size:17px;font-weight:600;color:${DARK};padding:10px 0 4px 0;">${title}</div>

      <!-- 表体 -->
      ${innerHTML}

      <!-- 汇总 -->
      ${summaryHTML}

      <!-- 签名 -->
      ${signHTML}
    </div>
  `
}

// ==================== 构建 HTML 表格 ====================
/**
 * @param {string[]} headers
 * @param {string[][]} rows
 */
function buildTableHTML(headers, rows) {
  const headerHTML = headers.map(h =>
    `<th style="padding:8px 6px;background:${RED};color:#fff;font-size:10px;font-weight:600;text-align:center;white-space:nowrap;border:1px solid ${RED};">${h}</th>`
  ).join('')

  const bodyHTML = rows.map((row, ri) => {
    const bg = ri % 2 === 0 ? '#fff' : '#FAFAFC'
    return `<tr style="background:${bg};">` +
      row.map((cell, ci) => {
        const align = /^[¥\d]/.test(cell) ? 'right' : 'left'
        return `<td style="padding:6px;font-size:9px;color:#333;text-align:${align};border-bottom:1px solid #eee;white-space:nowrap;">${cell}</td>`
      }).join('') +
      '</tr>'
  }).join('')

  return `
    <table style="width:100%;border-collapse:collapse;margin-top:6px;">
      <thead><tr>${headerHTML}</tr></thead>
      <tbody>${bodyHTML}</tbody>
    </table>
  `
}

// ==================== 导出一个完整的 ICBC 报表 ====================

/**
 * 将表格数据导出为 ICBC 样式的 PDF
 * @param {Object} options
 * @param {string} options.title            报表名称
 * @param {string} options.orientation      'portrait'|'landscape'
 * @param {string[]} options.columns         表头（与customHTML二选一）
 * @param {string[][]} options.rows         数据行（与customHTML二选一）
 * @param {string} options.customHTML       自定义HTML内容（与columns+rows二选一）
 * @param {Array} options.summaryItems      [{label, value}] 汇总
 * @param {string} options.filename         保存的文件名
 */
export async function exportICBCReport({ title, orientation = 'portrait', columns, rows, customHTML, summaryItems, filename }) {
  const contentHTML = customHTML || buildTableHTML(columns || [], rows || [])
  const reportHTML = buildReportHTML(title, contentHTML, summaryItems, true)

  // 创建临时 DOM
  const wrapper = document.createElement('div')
  wrapper.style.cssText = 'position:fixed;left:-9999px;top:0;width:720px;'
  wrapper.innerHTML = reportHTML
  document.body.appendChild(wrapper)

  try {
    const doc = new jsPDF({ orientation, unit: 'mm', format: 'a4' })
    await renderHTMLToPDF(doc, wrapper, 0)
    addFooter(doc)
    doc.save(filename || genFilename(title))
  } finally {
    document.body.removeChild(wrapper)
  }
}

// ==================== 支票单张打印 ====================

/**
 * 导出一张 ICBC 支票
 * @param {Object} check  支票数据
 */
export async function exportCheckPDF(check) {
  const statusColor = check.status === 2 ? '#52C41A' : check.status === 3 ? '#FF4D4F' : '#FA8C16'
  const statusText = check.status === 2 ? '已兑付' : check.status === 3 ? '已作废' : '待兑付'

  const checkHTML = `
    <div style="font-family:'Microsoft YaHei','PingFang SC','Hiragino Sans GB','WenQuanYi Micro Hei',sans-serif;background:#fff;padding:0;">
      <!-- 顶部红条 -->
      <div style="height:6px;background:${RED};"></div>

      <!-- ICBC Logo & 标题 -->
      <div style="display:flex;justify-content:space-between;align-items:flex-start;padding:10px 0 6px 0;">
        <div>
          <div style="font-size:18px;font-weight:700;color:${RED};">ICBC</div>
          <div style="font-size:9px;color:#666;">中国工商银行</div>
        </div>
        <div style="text-align:right;">
          <div style="font-size:12px;font-weight:600;color:${DARK};">银行支票</div>
          <div style="font-size:8px;color:#999;">INDUSTRIAL AND COMMERCIAL BANK OF CHINA</div>
        </div>
      </div>
      <div style="height:1px;background:${RED};margin:4px 0 8px 0;"></div>

      <!-- 支票主体 -->
      <div style="border:2px solid ${RED};border-radius:4px;padding:16px 20px;position:relative;">
        <!-- 水印（已兑付） -->
        ${check.status === 2 ? '<div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%) rotate(-30deg);font-size:48px;color:rgba(82,196,26,.08);font-weight:900;pointer-events:none;">已兑付</div>' : ''}
        ${check.status === 3 ? '<div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%) rotate(-30deg);font-size:48px;color:rgba(255,77,79,.08);font-weight:900;pointer-events:none;">已作废</div>' : ''}

        <!-- 支票标题 -->
        <div style="text-align:center;font-size:18px;font-weight:700;color:${RED};margin-bottom:12px;">
          中国工商银行 现金支票
        </div>

        <!-- 支票号码（右上） -->
        <div style="text-align:right;font-size:10px;color:#666;margin-bottom:8px;">
          支票号码：${check.checkNo}
        </div>

        <!-- 分隔线 -->
        <div style="border-top:1px dashed #ddd;margin:4px 0 10px 0;"></div>

        <!-- 支票信息行 -->
        <div style="display:flex;flex-wrap:wrap;gap:0;">
          <div style="width:50%;padding:4px 0;font-size:12px;">
            <span style="color:#888;">出票日期：</span><span style="color:#333;">${check.issueDate}</span>
          </div>
          <div style="width:50%;padding:4px 0;font-size:12px;">
            <span style="color:#888;">收款人：</span><span style="color:${DARK};font-weight:600;">${check.payee}</span>
          </div>
        </div>

        <!-- 金额 -->
        <div style="padding:8px 0;font-size:14px;color:${RED};font-weight:700;">
          金&nbsp;&nbsp;额：${fmt(check.amount)}
        </div>
        <div style="padding:2px 0 4px 0;font-size:12px;color:${DARK};font-weight:600;">
          大写金额：${check.amountCn}
        </div>

        <!-- 用途 -->
        <div style="padding:4px 0;font-size:12px;">
          <span style="color:#888;">用&nbsp;&nbsp;途：</span><span style="color:#333;">${check.purpose || '-'}</span>
        </div>

        <!-- 分隔线 -->
        <div style="border-top:1px solid #ddd;margin:10px 0;"></div>

        <!-- 付款行和签章 -->
        <div style="display:flex;justify-content:space-between;align-items:center;font-size:10px;">
          <span style="color:#666;">付款行：中国工商银行总行营业部</span>
          <span style="color:${RED};font-weight:600;">出票人签章（ICBC 中国工商银行）</span>
        </div>

        <!-- 兑付日期 -->
        ${check.cashedDate ? `<div style="margin-top:6px;font-size:10px;color:#52C41A;">兑付日期：${check.cashedDate}</div>` : ''}

        <!-- 状态标签 -->
        <div style="text-align:right;margin-top:10px;font-size:14px;font-weight:700;color:${statusColor};">
          【${statusText}】
        </div>
      </div>

      <!-- 底部 -->
      <div style="margin-top:12px;text-align:center;color:#aaa;font-size:10px;">
        ICBC 中国工商银行  |  本票据由系统自动生成，仅供内部管理使用
      </div>
    </div>
  `

  const wrapper = document.createElement('div')
  wrapper.style.cssText = 'position:fixed;left:-9999px;top:0;width:580px;'
  wrapper.innerHTML = checkHTML
  document.body.appendChild(wrapper)

  try {
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' })
    await renderHTMLToPDF(doc, wrapper, 0)
    addFooter(doc)
    doc.save(genFilename(`支票_${check.checkNo}`))
  } finally {
    document.body.removeChild(wrapper)
  }
}
