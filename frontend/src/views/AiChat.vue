<template>
  <div class="ai-chat-page">
    <PageHeader title="AI智能客服 · 小工" subtitle="工于至诚 行以致远 — 7×24小时智能在线服务" />
    <!-- ==================== 整体布局 ==================== -->
    <div class="chat-layout">
      <!-- 左侧：会话列表侧边栏 -->
      <div class="session-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <div v-if="!sidebarCollapsed" class="sidebar-title">
            <MessageOutlined />
            <span>会话列表</span>
          </div>
          <a-tooltip :title="sidebarCollapsed ? '展开会话列表' : '收起会话列表'">
            <a-button
              type="text"
              :icon="sidebarCollapsed ? h(MenuUnfoldOutlined) : h(MenuFoldOutlined)"
              class="collapse-btn"
              @click="sidebarCollapsed = !sidebarCollapsed"
            />
          </a-tooltip>
        </div>

        <div class="new-session-btn" v-if="!sidebarCollapsed">
          <a-button type="dashed" block @click="createNewSession">
            <template #icon><PlusOutlined /></template>
            新建会话
          </a-button>
        </div>
        <a-button v-else type="text" class="new-session-icon" @click="createNewSession">
          <PlusOutlined />
        </a-button>

        <div class="session-list" v-if="!sidebarCollapsed">
          <div v-if="sessions.length === 0 && !loadingSessions" class="empty-sessions">
            <MessageOutlined style="font-size: 24px; color: #d9d9d9; margin-bottom: 8px" />
            <span>暂无会话</span>
          </div>
          <div
            v-for="s in sessions"
            :key="s.sessionId"
            :class="['session-item', { active: currentSessionId === s.sessionId }]"
            @click="switchSession(s.sessionId)"
          >
            <div class="session-info">
              <div class="session-title">{{ s.title || '新会话' }}</div>
              <div class="session-meta">
                <span>{{ s.messageCount }}条消息</span>
                <span>{{ formatTime(s.updatedAt) }}</span>
              </div>
            </div>
            <a-popconfirm
              title="确定删除此会话？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="(e) => { e?.stopPropagation(); deleteSession(s.sessionId) }"
              @click.stop
            >
              <a-button type="text" size="small" danger class="delete-session-btn">
                <DeleteOutlined />
              </a-button>
            </a-popconfirm>
          </div>
        </div>

        <div class="sidebar-footer" v-if="!sidebarCollapsed">
          <div class="xiao-gong-tag">
            <SafetyCertificateOutlined />
            <span>小工 · 智能客服</span>
          </div>
        </div>
      </div>

      <!-- 右侧：主聊天区 -->
      <div class="chat-main">
        <!-- 顶部栏 -->
        <div class="chat-header">
          <div class="header-left">
            <div class="xiao-gong-avatar">
              <CustomerServiceOutlined />
            </div>
            <div class="header-info">
              <div class="header-name">小工 <a-tag color="success" size="small">在线</a-tag></div>
              <div class="header-desc">ICBC智能客服助手 · 基于豆包大模型</div>
            </div>
          </div>
          <div class="header-right">
            <a-tooltip title="切换模式">
              <a-segmented
                v-model:value="chatMode"
                :options="chatModeOptions"
                size="small"
              />
            </a-tooltip>
          </div>
        </div>

        <!-- 消息区 -->
        <div class="chat-messages" ref="messagesRef" @scroll="onScroll">
          <!-- 欢迎界面 -->
          <div v-if="messages.length === 0" class="chat-welcome">
            <div class="welcome-icon">
              <CustomerServiceOutlined />
            </div>
            <h3>您好，我是小工</h3>
            <p class="welcome-desc">您的ICBC智能银行助手，24小时为您服务</p>
            <div class="quick-questions">
              <a-tag
                v-for="q in quickQuestions"
                :key="q"
                color="error"
                class="quick-tag"
                @click="sendQuickQuestion(q)"
              >
                {{ q }}
              </a-tag>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-row', msg.type === 'user' ? 'is-user' : 'is-ai']"
          >
            <div class="message-avatar">
              <a-avatar
                v-if="msg.type === 'user'"
                :size="36"
                :style="{ backgroundColor: '#C8102E' }"
              >
                {{ username.charAt(0)?.toUpperCase() }}
              </a-avatar>
              <div v-else class="ai-avatar">
                <CustomerServiceOutlined />
              </div>
            </div>
            <div class="message-body">
              <div class="message-name">
                {{ msg.type === 'user' ? '我' : '小工' }}
                <span v-if="msg.responseTime" class="message-latency">{{ msg.responseTime }}ms</span>
              </div>
              <!-- 文本消息 -->
              <div v-if="msg.type !== 'image'" class="message-bubble">
                <!-- 流式输出中的打字机效果 -->
                <template v-if="msg.streaming">
                  <span v-html="formatMessage(msg.content)"></span>
                  <span class="cursor-blink">|</span>
                </template>
                <template v-else>
                  <span v-html="formatMessage(msg.content)"></span>
                </template>
              </div>
              <!-- 图片消息 -->
              <div v-if="msg.type === 'image' || msg.imageUrl" class="message-bubble image-bubble">
                <div class="image-prompt">{{ msg.content }}</div>
                <a-image
                  v-if="msg.imageUrl && !msg.imageLoading"
                  :src="msg.imageUrl"
                  :preview="true"
                  style="max-width: 320px; border-radius: 8px; cursor: pointer"
                  @click.stop
                />
                <div v-if="msg.imageLoading" class="image-loading">
                  <a-spin />
                  <span>小工正在生成图片...</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 流式输入中指示 -->
          <div v-if="streaming" class="message-row is-ai">
            <div class="message-avatar">
              <div class="ai-avatar">
                <CustomerServiceOutlined />
              </div>
            </div>
            <div class="message-body">
              <div class="message-name">小工 · 输入中</div>
              <div class="message-bubble typing-bubble">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部输入区 -->
        <div class="chat-input-area">
          <!-- 生图模式提示 -->
          <div v-if="chatMode === 'image'" class="image-mode-bar">
            <PictureOutlined /> 图片生成模式 · 描述您想要的图片，小工将为您生成
          </div>

          <div class="chat-input-row">
            <a-textarea
              v-model:value="inputMessage"
              :rows="1"
              :auto-size="{ minRows: 1, maxRows: 4 }"
              :placeholder="chatMode === 'image' ? '描述您想要的图片...' : '输入您的问题，按 Enter 发送...'"
              @press-enter="handleSend"
              :disabled="sending"
              class="input-area"
            />
            <a-button
              v-if="chatMode === 'text'"
              type="primary"
              :loading="sending"
              @click="handleSend"
              class="send-btn"
            >
              <SendOutlined /> 发送
            </a-button>
            <a-button
              v-else
              type="primary"
              :loading="sending"
              @click="handleGenerateImage"
              class="send-btn image-send-btn"
            >
              <PictureOutlined /> 生成图片
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, h, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/user'
import { aiChat, aiChatStream, getAiSessions, deleteAiSession, getSessionHistory, generateImage } from '@/api/index'
import { message } from 'ant-design-vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  CustomerServiceOutlined, SendOutlined, PictureOutlined,
  PlusOutlined, DeleteOutlined, MessageOutlined,
  MenuFoldOutlined, MenuUnfoldOutlined, SafetyCertificateOutlined
} from '@ant-design/icons-vue'

const userStore = useUserStore()
const username = computed(() => userStore.username || 'U')

// ==================== 状态 ====================
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const streaming = ref(false)          // 是否正在流式接收
const currentSessionId = ref('')
const sessions = ref([])
const loadingSessions = ref(false)
const sidebarCollapsed = ref(false)
const chatMode = ref('text')          // 'text' | 'image'
const messagesRef = ref(null)
const abortController = ref(null)     // 用于中断流式请求

const chatModeOptions = [
  { value: 'text', label: '💬 文本' },
  { value: 'image', label: '🖼️ 生图' }
]

const quickQuestions = [
  '如何转账？',
  '如何查询余额？',
  '账单怎么查看？',
  '如何修改密码？',
  '转账限额是多少？',
  '如何联系人工客服？'
]

// ==================== 生命周期 ====================
onMounted(async () => {
  await loadSessions()
  // 如果有历史会话，加载最近一个
  if (sessions.value.length > 0) {
    await switchSession(sessions.value[0].sessionId)
  }
})

onUnmounted(() => {
  // 组件卸载时中断流式请求
  if (abortController.value) {
    abortController.value.abort()
  }
})

// ==================== 会话管理 ====================
async function loadSessions() {
  loadingSessions.value = true
  try {
    const res = await getAiSessions()
    sessions.value = res.data || []
  } catch (e) {
    console.error('加载会话列表失败', e)
  } finally {
    loadingSessions.value = false
  }
}

async function createNewSession() {
  currentSessionId.value = ''
  messages.value = []
  chatMode.value = 'text'
  inputMessage.value = ''
  // 等待用户发送第一条消息时自动创建会话
  await nextTick()
  inputFocus()
}

async function switchSession(sessionId) {
  if (currentSessionId.value === sessionId) return
  currentSessionId.value = sessionId
  messages.value = []
  chatMode.value = 'text'

  try {
    const res = await getSessionHistory(sessionId)
    const history = res.data || []
    messages.value = history.map(h => ({
      type: h.messageType === 'USER' ? 'user' :
            h.messageType === 'IMAGE' ? 'image' : 'ai',
      content: h.message,
      responseTime: h.responseTime,
      imageUrl: h.imageUrl,
      streaming: false,
      imageLoading: false
    }))
    await scrollToBottom()
  } catch (e) {
    console.error('加载会话历史失败', e)
  }
}

async function deleteSession(sessionId) {
  try {
    await deleteAiSession(sessionId)
    message.success('会话已删除')
    sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = ''
      messages.value = []
    }
  } catch (e) {
    message.error('删除失败')
  }
}

// ==================== 文本对话 ====================
async function handleSend(e) {
  // 处理 Shift+Enter 换行
  if (e?.shiftKey) return
  e?.preventDefault?.()

  const msg = inputMessage.value.trim()
  if (!msg || sending.value) return

  // 添加用户消息
  messages.value.push({ type: 'user', content: msg, streaming: false })
  inputMessage.value = ''
  sending.value = true

  await scrollToBottom()

  try {
    await handleStreamChat(msg)
  } catch (e) {
    if (e.name !== 'AbortError') {
      messages.value.push({
        type: 'ai',
        content: '抱歉，服务暂时不可用，请稍后重试或拨打95588联系人工客服。',
        streaming: false
      })
    }
  } finally {
    sending.value = false
    streaming.value = false
    abortController.value = null
    await scrollToBottom()
    await loadSessions() // 刷新会话列表
  }
}

// ==================== 流式对话 (SSE + 打字机效果) ====================
async function handleStreamChat(messageText) {
  streaming.value = true

  // 创建一个占位的 AI 消息用于打字机输出
  const aiMsgIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    streaming: true,
    responseTime: null
  })

  const startTime = Date.now()

  // 先尝试流式SSE，失败则自动降级为非流式
  let useFallback = false

  try {
    abortController.value = new AbortController()
    const response = await aiChatStream({
      message: messageText,
      sessionId: currentSessionId.value || undefined
    })

    if (!response.ok) {
      // 流式失败，抛出错误触发降级
      throw new Error('SSE_STREAM_FAILED')
    }

    // 从响应头获取 sessionId
    const newSessionId = response.headers.get('X-Session-Id')
    if (newSessionId && !currentSessionId.value) {
      currentSessionId.value = newSessionId
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullContent = ''
    let lastRenderTime = 0

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 解析 SSE 格式 (data:xxx\n\n)
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data && data !== '[DONE]') {
            fullContent += data
            await typewriterUpdate(aiMsgIndex, fullContent, lastRenderTime)
            lastRenderTime = Date.now()
          }
        }
      }

      if (Date.now() - lastRenderTime > 100) {
        await scrollToBottom()
      }
    }

    // 流结束
    const responseTime = Date.now() - startTime
    messages.value[aiMsgIndex].streaming = false
    messages.value[aiMsgIndex].responseTime = responseTime
    messages.value[aiMsgIndex].content = fullContent

    if (!currentSessionId.value) {
      await loadSessions()
    }

  } catch (e) {
    // SSE流式失败 → 自动降级为非流式对话 + 客户端模拟打字机
    console.warn('[小工] 流式SSE不可用，降级为非流式对话:', e.message)
    useFallback = true

    try {
      // 调用非流式API
      const res = await aiChat({
        message: messageText,
        sessionId: currentSessionId.value || undefined
      })
      const aiData = res.data || res
      const aiText = aiData.message || aiData.content || '抱歉，服务暂时不可用。'
      currentSessionId.value = aiData.sessionId || currentSessionId.value

      // 客户端模拟打字机效果
      const chars = [...aiText]
      for (let i = 0; i < chars.length; i++) {
        messages.value[aiMsgIndex].content = aiText.substring(0, i + 1)
        await new Promise(r => setTimeout(r, 35 + Math.random() * 15))
        await nextTick()
        if (i % 5 === 0) await scrollToBottom()
      }

      const responseTime = Date.now() - startTime
      messages.value[aiMsgIndex].streaming = false
      messages.value[aiMsgIndex].responseTime = responseTime
      messages.value[aiMsgIndex].content = aiText

      await loadSessions()
    } catch (fallbackErr) {
      console.error('[小工] 非流式降级也失败:', fallbackErr)
      messages.value[aiMsgIndex].streaming = false
      if (!messages.value[aiMsgIndex].content) {
        messages.value[aiMsgIndex].content = '抱歉，小工暂时无法响应，请稍后重试或拨打95588联系人工客服。'
      }
    }
  } finally {
    streaming.value = false
    abortController.value = null
    if (aiMsgIndex < messages.value.length && messages.value[aiMsgIndex].streaming) {
      messages.value[aiMsgIndex].streaming = false
    }
    await scrollToBottom()
  }
}

/**
 * 打字机效果逐字更新
 */
async function typewriterUpdate(index, fullText, lastTime) {
  const msg = messages.value[index]
  if (!msg) return

  const currentLen = msg.content.length
  const targetLen = fullText.length

  if (currentLen >= targetLen) return

  // 动态速度：根据时间间隔自适应
  const now = Date.now()
  const charsToAdd = Math.max(1, Math.floor((now - lastTime) / 30))

  msg.content = fullText.substring(0, currentLen + charsToAdd)

  // 触发响应式更新
  await nextTick()
  await scrollToBottom()
}

// ==================== 图片生成 ====================
async function handleGenerateImage() {
  const prompt = inputMessage.value.trim()
  if (!prompt || sending.value) return

  messages.value.push({ type: 'user', content: '[生成图片] ' + prompt, streaming: false })
  inputMessage.value = ''
  sending.value = true

  // 占位图片消息
  const imgIndex = messages.value.length
  messages.value.push({
    type: 'image',
    content: prompt,
    imageUrl: null,
    imageLoading: true,
    streaming: false
  })

  await scrollToBottom()

  try {
    const res = await generateImage({
      prompt,
      sessionId: currentSessionId.value || undefined
    })

    const data = res.data
    currentSessionId.value = data.sessionId
    messages.value[imgIndex].imageUrl = data.imageUrl
    messages.value[imgIndex].imageLoading = false
    messages.value[imgIndex].responseTime = data.responseTime

    if (!data.imageUrl) {
      messages.value.push({
        type: 'ai',
        content: '抱歉，图片生成失败，请重试或换个描述试试~',
        streaming: false
      })
    }
  } catch (e) {
    messages.value[imgIndex].imageLoading = false
    messages.value.push({
      type: 'ai',
      content: '抱歉，图片生成服务暂时不可用，请稍后重试。',
      streaming: false
    })
  } finally {
    sending.value = false
    await scrollToBottom()
    await loadSessions()
  }
}

// ==================== 快捷问题 ====================
function sendQuickQuestion(q) {
  inputMessage.value = q
  chatMode.value = 'text'
  handleSend()
}

// ==================== 工具函数 ====================
function formatMessage(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/【(.+?)】/g, '<strong style="color:#C8102E">【$1】</strong>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.+?)`/g, '<code style="background:#FFF1F0;padding:2px 6px;border-radius:4px;font-size:13px">$1</code>')
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 172800000) return '昨天'
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

async function scrollToBottom() {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function onScroll() {
  // 可用于检测用户是否在查看历史消息（暂停自动滚动）
}

function inputFocus() {
  nextTick(() => {
    const textarea = document.querySelector('.ai-chat-page .input-area textarea')
    textarea?.focus()
  })
}
</script>

<style scoped>
/* ==================== 页面布局 ==================== */
.ai-chat-page {
  height: calc(100vh - 112px);
  min-height: 600px;
}

.chat-layout {
  display: flex;
  height: 100%;
  gap: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* ==================== 左侧会话侧边栏 ==================== */
.session-sidebar {
  width: 260px;
  min-width: 260px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  background: #fafbfc;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.session-sidebar.collapsed {
  width: 56px;
  min-width: 56px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 12px;
  border-bottom: 1px solid #f0f0f0;
  min-height: 56px;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
}

.sidebar-title :deep(.anticon) {
  color: #C8102E;
  font-size: 16px;
}

.collapse-btn {
  color: #8c8c8c;
}

.new-session-btn {
  padding: 12px;
}

.new-session-icon {
  display: flex;
  justify-content: center;
  padding: 12px 0;
  color: #C8102E;
}

/* 会话列表 */
.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 16px;
  color: #bfbfbf;
  font-size: 13px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 2px;
  position: relative;
  group: session-item;
}

.session-item:hover {
  background: #f0f0f0;
}

.session-item.active {
  background: #FFF1F0;
}

.session-item.active .session-title {
  color: #C8102E;
  font-weight: 600;
}

.session-item .delete-session-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .delete-session-btn {
  opacity: 1;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 13px;
  color: #1f1f1f;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.session-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #bfbfbf;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
}

.xiao-gong-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #C8102E;
  padding: 8px 12px;
  background: #FFF1F0;
  border-radius: 8px;
}

/* ==================== 右侧主聊天区 ==================== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 顶部栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  min-height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.xiao-gong-avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #C8102E;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.header-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-desc {
  font-size: 12px;
  color: #8c8c8c;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ==================== 消息区 ==================== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #FAFBFC;
}

/* 欢迎界面 */
.chat-welcome {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: #FFF1F0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
  color: #C8102E;
  box-shadow: 0 2px 6px rgba(0,0,0,.06);
}

.chat-welcome h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1f1f1f;
  margin-bottom: 8px;
}

.welcome-desc {
  color: #8c8c8c;
  font-size: 14px;
  margin-bottom: 28px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  max-width: 480px;
  margin: 0 auto;
}

.quick-tag {
  cursor: pointer;
  font-size: 13px;
  padding: 4px 14px;
  border-radius: 16px;
  transition: all 0.2s;
}

.quick-tag:hover {
  opacity: 0.85;
}

/* ===== 消息行 ===== */
.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 22px;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-row.is-user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #FFF1F0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C8102E;
  font-size: 18px;
}

.message-body {
  max-width: 72%;
}

.is-user .message-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-name {
  font-size: 11px;
  color: #8c8c8c;
  margin-bottom: 4px;
  padding: 0 3px;
}

.message-latency {
  color: #bfbfbf;
  margin-left: 6px;
  font-size: 10px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.is-user .message-bubble {
  background: #C8102E;
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
}

.is-ai .message-bubble {
  background: #ffffff;
  color: #1f1f1f;
  border: 1px solid #f0f0f0;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

/* 图片消息气泡 */
.image-bubble {
  background: #fff !important;
  border: 2px dashed #f0d0d0 !important;
  padding: 12px !important;
}

.image-prompt {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 10px;
  font-style: italic;
}

.image-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px;
  color: #8c8c8c;
  font-size: 13px;
}

/* ===== 打字机光标 ===== */
.cursor-blink {
  display: inline;
  color: #C8102E;
  font-weight: 700;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* ===== 打字动画(等待态) ===== */
.typing-bubble {
  display: flex;
  gap: 5px;
  padding: 14px 18px;
}

.dot {
  width: 7px;
  height: 7px;
  background: #bfbfbf;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.3; }
  30% { transform: translateY(-5px); opacity: 1; }
}

/* ==================== 输入区 ==================== */
.chat-input-area {
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.image-mode-bar {
  padding: 8px 20px;
  background: #FFF1F0;
  font-size: 12px;
  color: #C8102E;
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-input-row {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  align-items: flex-end;
}

.input-area {
  flex: 1;
}

.send-btn {
  flex-shrink: 0;
  height: 40px;
  border-radius: 8px;
}

.image-send-btn {
  background: #C8102E;
  border: none;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .session-sidebar {
    width: 56px;
    min-width: 56px;
  }

  .message-body {
    max-width: 85%;
  }

  .chat-messages {
    padding: 12px;
  }
}
</style>
