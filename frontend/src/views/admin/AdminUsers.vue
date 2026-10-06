<template>
  <div class="admin-users">
    <PageHeader title="用户管理" subtitle="管理系统所有注册用户">
      <template #extra>
        <a-button @click="exportUsersPDF" :icon="h(FilePdfOutlined)">导出PDF</a-button>
      </template>
    </PageHeader>

    <a-card :bordered="false" class="card-main">
      <a-table :columns="columns" :data-source="users" :loading="loading"
        :pagination="{ pageSize: 10 }" row-key="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'role'">
            <a-tag :color="record.role === 'ADMIN' ? 'red' : 'blue'">
              {{ record.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="viewAccounts(record)">查看账户</a-button>
            <a-button type="link" size="small" @click="viewUserDetail(record)">详情</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 用户详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="用户详情" :footer="null" width="500px">
      <a-descriptions bordered size="small" :column="1" v-if="currentUser">
        <a-descriptions-item label="用户ID">{{ currentUser.id }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ currentUser.username }}</a-descriptions-item>
        <a-descriptions-item label="真实姓名">{{ currentUser.realName }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentUser.phone }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</a-descriptions-item>
        <a-descriptions-item label="角色">
          <a-tag :color="currentUser.role === 'ADMIN' ? 'red' : 'blue'">
            {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentUser.status === 1 ? 'green' : 'red'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间">{{ currentUser.createdAt }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 用户账户弹窗 -->
    <a-modal v-model:open="accountsVisible" :title="'用户账户 - ' + (accountsUser?.username || '')"
      :footer="null" width="900px">
      <a-table :columns="acctColumns" :data-source="userAccounts" :loading="acctLoading"
        :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="record.accountType === 1 ? 'blue' : 'gold'">
              {{ record.accountTypeName }}
            </a-tag>
          </template>
          <template v-if="column.key === 'balance'">
            <span class="amount-text">¥{{ formatMoney(record.balance) }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="primary" size="small" @click="openRecharge(record)">充值</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 充值弹窗 -->
    <a-modal v-model:open="rechargeVisible" title="账户充值" @ok="handleRecharge" :confirmLoading="rechargeLoading" width="400px">
      <a-form layout="vertical">
        <a-form-item label="账户号">
          <a-input :value="rechargeAccount?.accountNo" disabled />
        </a-form-item>
        <a-form-item label="当前余额">
          <a-input :value="'¥' + formatMoney(rechargeAccount?.balance)" disabled />
        </a-form-item>
        <a-form-item label="充值金额" required>
          <a-input-number v-model:value="rechargeAmount" :min="1" :max="99999999"
            style="width:100%" placeholder="请输入充值金额" size="large" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getAdminUsers, getAdminUserAccounts, adminRecharge } from '@/api/index'
import PageHeader from '@/components/PageHeader.vue'
import { FilePdfOutlined } from '@ant-design/icons-vue'
import { exportICBCReport, genFilename } from '@/utils/pdfExport'

const loading = ref(false)
const acctLoading = ref(false)
const rechargeLoading = ref(false)
const users = ref([])
const detailVisible = ref(false)
const currentUser = ref(null)
const accountsVisible = ref(false)
const accountsUser = ref(null)
const userAccounts = ref([])
const rechargeVisible = ref(false)
const rechargeAccount = ref(null)
const rechargeAmount = ref(null)

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username', width: 110 },
  { title: '真实姓名', dataIndex: 'realName', width: 100 },
  { title: '手机号', dataIndex: 'phone', width: 120 },
  { title: '邮箱', dataIndex: 'email', width: 150 },
  { title: '角色', key: 'role', width: 90 },
  { title: '状态', key: 'status', width: 70 },
  { title: '注册时间', dataIndex: 'createdAt', width: 110 },
  { title: '操作', key: 'action', width: 160 }
]

const acctColumns = [
  { title: '账户号', dataIndex: 'accountNo', width: 160 },
  { title: '类型', key: 'type', width: 80 },
  { title: '余额', key: 'balance', width: 130 },
  { title: '信用额度', dataIndex: 'creditLimit', width: 120, customRender: ({ text }) => '¥' + formatMoney(text) },
  { title: '状态', dataIndex: 'status', width: 60, customRender: ({ text }) => text === 1 ? '正常' : '冻结' },
  { title: '操作', key: 'action', width: 80 }
]

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await getAdminUsers()
    users.value = res.data || []
  } catch (e) { message.error('加载用户列表失败') }
  finally { loading.value = false }
}

function viewUserDetail(record) {
  currentUser.value = record
  detailVisible.value = true
}

async function viewAccounts(record) {
  accountsUser.value = record
  acctLoading.value = true
  try {
    const res = await getAdminUserAccounts(record.id)
    userAccounts.value = res.data || []
    accountsVisible.value = true
  } catch (e) { message.error('加载账户失败') }
  finally { acctLoading.value = false }
}

function openRecharge(record) {
  rechargeAccount.value = record
  rechargeAmount.value = null
  rechargeVisible.value = true
}

async function handleRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) { message.warning('请输入充值金额'); return }
  rechargeLoading.value = true
  try {
    await adminRecharge({ accountNo: rechargeAccount.value.accountNo, amount: rechargeAmount.value, remark: '管理员充值' })
    message.success('充值成功！')
    rechargeVisible.value = false
    await viewAccounts(accountsUser.value)
  } catch (e) { message.error(e?.response?.data?.message || '充值失败') }
  finally { rechargeLoading.value = false }
}

function formatMoney(val) {
  const num = parseFloat(val) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// ==================== PDF 导出 ====================
async function exportUsersPDF() {
  try {
    const cols = ['ID', '用户名', '真实姓名', '手机号', '邮箱', '角色', '状态', '注册时间']
    const rows = users.value.map(u => [
      String(u.id), u.username, u.realName, u.phone, u.email || '-',
      u.role === 'ADMIN' ? '管理员' : '普通用户',
      u.status === 1 ? '正常' : '禁用',
      u.createdAt || '-'
    ])
    await exportICBCReport({
      title: '用户管理报表',
      columns: cols,
      rows,
      summaryItems: [
        { label: '用户总数：', value: `${users.value.length} 人` },
        { label: '管理员：', value: `${users.value.filter(u => u.role === 'ADMIN').length} 人` },
        { label: '普通用户：', value: `${users.value.filter(u => u.role === 'USER').length} 人` },
        { label: '正常用户：', value: `${users.value.filter(u => u.status === 1).length} 人` },
        { label: '禁用用户：', value: `${users.value.filter(u => u.status !== 1).length} 人` }
      ],
      filename: genFilename('用户管理报表')
    })
  } catch (e) {
    console.error('PDF导出失败:', e)
    message.error('PDF导出失败，请重试')
  }
}
</script>

<style scoped>
.admin-users { max-width: 1600px; }
.card-main { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.card-main :deep(.ant-card-body) { padding: 24px; }
.amount-text { font-weight: 600; color: #1A1A1A; }
</style>
