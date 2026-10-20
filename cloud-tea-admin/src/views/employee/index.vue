<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">员工管理</div>
        <div class="page-sub">新增员工默认密码为 123456，员工可自行修改</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增员工</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input
          v-model="query.name"
          placeholder="按姓名搜索"
          clearable
          style="width: 220px"
          @keyup.enter="reload"
        />
        <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
        <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ row.sex === '1' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="idNumber" label="身份证号" min-width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ row.idNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后修改" width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ row.updateTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="primary" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="reload"
        />
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑员工' : '新增员工'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="登录用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio value="1">男</el-radio>
            <el-radio value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="18 位身份证号" maxlength="18" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import { addEmployee, getEmployee, pageEmployee, setEmployeeStatus, updateEmployee } from '@/api'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)

const query = reactive({ page: 1, pageSize: 10, name: '' })

const formVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', name: '', phone: '', sex: '1', idNumber: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

async function load() {
  loading.value = true
  try {
    const data = await pageEmployee({ ...query })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.page = 1
  load()
}

function reset() {
  query.name = ''
  reload()
}

async function openForm(row) {
  if (row) {
    // 列表里的字段不含身份证等完整信息，编辑时拉一次详情
    const detail = await getEmployee(row.id)
    Object.assign(form, detail)
  } else {
    Object.assign(form, { id: null, username: '', name: '', phone: '', sex: '1', idNumber: '' })
  }
  formVisible.value = true
}

async function onSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (form.id) {
      await updateEmployee({ ...form })
    } else {
      await addEmployee({ ...form })
    }
    ElMessage.success(form.id ? '修改成功' : '新增成功')
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  await setEmployeeStatus(next, row.id)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  load()
}

onMounted(load)
</script>
