import request from '@/utils/request'

export function login(data) {
  return request.post('/users/login', data)
}

export function register(data) {
  return request.post('/users/register', data)
}

export function getUserInfo() {
  return request.get('/users/info')
}

export function getAccounts() {
  return request.get('/accounts')
}

export function getAccountBalance(accountNo) {
  return request.get('/accounts/balance', { params: { accountNo } })
}

export function createAccount(accountType) {
  return request.post('/accounts', null, { params: { accountType } })
}

export function transfer(data) {
  return request.post('/transactions', data)
}

export function getTransactionHistory(accountNo, page = 0, size = 20) {
  return request.get('/transactions/history', { params: { accountNo, page, size } })
}

export function getBills() {
  return request.get('/bills')
}

export function getBillDetail(id) {
  return request.get(`/bills/${id}`)
}

export function aiChat(data) {
  return request.post('/ai/chat', data)
}

/** 流式聊天 - 直接返回fetch Response用于SSE读取 */
export function aiChatStream(data) {
  const token = localStorage.getItem('token')
  return fetch('/api/v1/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
  })
}

/** 获取AI会话列表 */
export function getAiSessions() {
  return request.get('/ai/sessions')
}

/** 删除AI会话 */
export function deleteAiSession(sessionId) {
  return request.delete(`/ai/sessions/${sessionId}`)
}

/** 获取会话历史消息 */
export function getSessionHistory(sessionId) {
  return request.get(`/ai/sessions/${sessionId}/history`)
}

/** 生成图片 */
export function generateImage(data) {
  return request.post('/ai/image/generate', data)
}

/** 创建新会话 */
export function createAiSession() {
  return request.post('/ai/session')
}

export function getSystemInfo() {
  return request.get('/info')
}

// ===== 信用卡借款 API =====
export function getMyLoans() {
  return request.get('/loans/my')
}

export function applyLoan(data) {
  return request.post('/loans/apply', data)
}

export function getLoanDetail(loanNo) {
  return request.get(`/loans/${loanNo}`)
}

export function getRepayments(loanNo) {
  return request.get(`/loans/${loanNo}/repayments`)
}

export function repayLoan(repaymentId) {
  return request.post(`/loans/repay/${repaymentId}`)
}

// ===== 管理员 API =====
export function getAdminDashboard() {
  return request.get('/admin/dashboard')
}

export function getAdminUsers() {
  return request.get('/admin/users')
}

export function getAdminUserAccounts(userId) {
  return request.get(`/admin/users/${userId}/accounts`)
}

export function getAdminAccounts() {
  return request.get('/admin/accounts')
}

export function adminRecharge(data) {
  return request.post('/admin/recharge', data)
}

export function getAdminLoans() {
  return request.get('/admin/loans')
}

export function adminApproveLoan(loanNo) {
  return request.post(`/admin/loans/${loanNo}/approve`)
}

export function adminRejectLoan(loanNo, reason) {
  return request.post(`/admin/loans/${loanNo}/reject`, null, { params: { reason } })
}

// ===== 支票管理 API =====
export function getAdminChecks() {
  return request.get('/admin/checks')
}

export function createCheck(data) {
  return request.post('/admin/checks', data)
}

export function cashCheck(checkNo) {
  return request.post(`/admin/checks/${checkNo}/cash`)
}

export function voidCheck(checkNo) {
  return request.post(`/admin/checks/${checkNo}/void`)
}
