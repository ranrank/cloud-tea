<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">分类管理</div>
        <div class="page-sub">饮品分类是菜单的一级分类，组合套餐分类单独归类</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增分类</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input
          v-model="query.name"
          placeholder="按分类名称搜索"
          clearable
          style="width: 220px"
          @keyup.enter="reload"
        />
        <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 160px">
          <el-option label="饮品分类" :value="1" />
          <el-option label="组合套餐分类" :value="2" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
        <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" effect="light" size="small">
              {{ row.type === 1 ? '饮品分类' : '组合套餐分类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后修改" width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ row.updateTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="primary" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑分类' : '新增分类'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：经典奶茶 / 鲜果茶" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">饮品分类</el-radio>
            <el-radio :value="2">组合套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="99" />
          <span class="muted" style="margin-left: 10px; font-size: 12.5px">数字越小越靠前</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import { addCategory, deleteCategory, pageCategory, setCategoryStatus, updateCategory } from '@/api'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)

const query = reactive({ page: 1, pageSize: 10, name: '', type: null })

const formVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', type: 1, sort: 0 })

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageCategory({ ...query })
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
  query.type = null
  reload()
}

function openForm(row) {
  Object.assign(form, row ? { id: row.id, name: row.name, type: row.type, sort: row.sort } : { id: null, name: '', type: 1, sort: 0 })
  formVisible.value = true
}

async function onSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (form.id) {
      await updateCategory({ ...form })
    } else {
      await addCategory({ ...form })
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
  await setCategoryStatus(next, row.id)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？若该分类下还有饮品或组合套餐，后端会拒绝删除。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return // 用户取消
  }

  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    // 删除失败的原因（如被关联）已由拦截器提示
  }
}

onMounted(load)
</script>
