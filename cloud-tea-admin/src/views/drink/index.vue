<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">饮品管理</div>
        <div class="page-sub">每款饮品可配置杯型、温度、糖度、加料等多组规格</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增饮品</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input
          v-model="query.name"
          placeholder="按饮品名称搜索"
          clearable
          style="width: 200px"
          @keyup.enter="reload"
        />
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 170px">
          <el-option v-for="s in categoryOptions" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
        <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column label="饮品" min-width="200">
          <template #default="{ row }">
            <div class="drink-cell">
              <el-image v-if="row.image" :src="row.image" class="thumb" fit="cover" :preview-src-list="[row.image]" preview-teleported />
              <div v-else class="thumb placeholder"><el-icon><ColdDrink /></el-icon></div>
              <div>
                <div class="drink-name">{{ row.name }}</div>
                <div class="muted drink-desc">{{ row.description || '—' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="所属分类" width="130" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="price">{{ Number(row.price).toFixed(2) }}</span> 元
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(v) => toggleStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后修改" width="180">
          <template #default="{ row }">
            <span class="mono muted">{{ row.updateTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
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
    <el-dialog v-model="formVisible" :title="form.id ? '编辑饮品' : '新增饮品'" width="720px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="饮品名称" prop="name">
              <el-input v-model="form.name" placeholder="如：招牌珍珠奶茶" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
                <el-option v-for="s in categoryOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图片">
              <el-upload
                :show-file-list="false"
                :http-request="doUpload"
                accept="image/*"
                class="uploader"
              >
                <el-image v-if="form.image" :src="form.image" class="upload-preview" fit="cover" />
                <div v-else class="upload-empty">
                  <el-icon><Plus /></el-icon>
                  <span>上传图片</span>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="120" show-word-limit placeholder="一句话描述这款饮品" />
        </el-form-item>

        <!-- 规格组编辑器 -->
        <el-form-item label="规格">
          <div class="spec-editor">
            <div v-for="(spec, i) in form.specs" :key="i" class="spec-row">
              <el-input v-model="spec.specName" placeholder="规格组名，如 杯型" class="spec-name" />

              <div class="spec-values">
                <el-tag
                  v-for="(v, vi) in spec.values"
                  :key="vi"
                  closable
                  type="success"
                  effect="light"
                  @close="spec.values.splice(vi, 1)"
                >
                  {{ v }}
                </el-tag>
                <el-input
                  v-model="spec.draft"
                  size="small"
                  class="spec-input"
                  placeholder="+ 输入规格值后回车"
                  @keyup.enter="addValue(spec)"
                  @blur="addValue(spec)"
                />
              </div>

              <el-button link type="danger" :icon="Delete" @click="form.specs.splice(i, 1)" />
            </div>

            <el-button link type="primary" :icon="Plus" @click="addSpecRow">添加规格组</el-button>
            <div class="muted spec-tip">
              示例：杯型 → 中杯 / 大杯；温度 → 热 / 常温 / 少冰 / 去冰；糖度 → 无糖 / 三分糖 / 七分糖 / 全糖
            </div>
          </div>
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
import { Delete, Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  addDrink,
  deleteDrink,
  getDrink,
  listCategory,
  pageDrink,
  setDrinkStatus,
  updateDrink,
  uploadFile
} from '@/api'
import { parseSpecValues } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const categoryOptions = ref([])

const query = reactive({ page: 1, pageSize: 10, name: '', categoryId: null, status: null })

const formVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  image: '',
  description: '',
  specs: []
})

const rules = {
  name: [{ required: true, message: '请输入饮品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

/* -------- 规格编辑 -------- */
function addSpecRow() {
  form.specs.push({ specName: '', values: [], draft: '' })
}

function addValue(spec) {
  const v = (spec.draft || '').trim()
  if (!v) return
  if (!spec.values.includes(v)) {
    spec.values.push(v)
  }
  spec.draft = ''
}

/** 表单态 -> 接口态：values 数组序列化成后端存的 JSON 字符串 */
function specsToPayload() {
  return form.specs
    .filter((s) => s.specName.trim() && s.values.length)
    .map((s) => ({ specName: s.specName.trim(), specValues: JSON.stringify(s.values) }))
}

/* -------- 列表 -------- */
async function load() {
  loading.value = true
  try {
    const data = await pageDrink({ ...query })
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
  Object.assign(query, { page: 1, name: '', categoryId: null, status: null })
  load()
}

async function loadCategory() {
  categoryOptions.value = (await listCategory(1)) || []
}

/* -------- 表单 -------- */
async function openForm(row) {
  if (row) {
    const detail = await getDrink(row.id)
    Object.assign(form, {
      id: detail.id,
      name: detail.name,
      categoryId: detail.categoryId,
      price: Number(detail.price),
      image: detail.image || '',
      description: detail.description || '',
      specs: (detail.specs || []).map((s) => ({
        specName: s.specName,
        values: parseSpecValues(s.specValues),
        draft: ''
      }))
    })
  } else {
    Object.assign(form, { id: null, name: '', categoryId: null, price: 0, image: '', description: '', specs: [] })
    addSpecRow()
  }
  formVisible.value = true
}

async function doUpload({ file }) {
  try {
    const url = await uploadFile(file)
    form.image = url
    ElMessage.success('图片上传成功')
  } catch {
    // 上传失败原因已由拦截器提示
  }
}

async function onSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const specs = specsToPayload()
  if (!specs.length) {
    ElMessage.warning('请至少配置一组规格')
    return
  }

  saving.value = true
  try {
    // specsToPayload() 已经只保留 { specName, specValues }，草稿字段不会带出去
    const payload = { ...form, specs }
    if (form.id) {
      await updateDrink(payload)
    } else {
      await addDrink(payload)
    }
    ElMessage.success(form.id ? '修改成功' : '新增成功')
    formVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row, value) {
  const next = value ? 1 : 0
  await setDrinkStatus(next, row.id)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除饮品「${row.name}」吗？上架中的饮品或被组合套餐引用的饮品不能删除。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await deleteDrink(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    // 失败原因已由拦截器提示
  }
}

onMounted(() => {
  loadCategory()
  load()
})
</script>

<style scoped>
.drink-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thumb {
  width: 46px;
  height: 46px;
  border-radius: 9px;
  flex-shrink: 0;
}

.thumb.placeholder {
  display: grid;
  place-items: center;
  background: var(--ct-green-soft);
  color: var(--ct-green);
  font-size: 20px;
}

.drink-name {
  font-weight: 600;
  font-size: 14px;
}

.drink-desc {
  font-size: 12px;
  margin-top: 3px;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-weight: 600;
  color: var(--ct-green-deep);
}

.uploader :deep(.el-upload) {
  border: 1px dashed var(--ct-line);
  border-radius: 10px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.uploader :deep(.el-upload:hover) {
  border-color: var(--ct-green);
}

.upload-empty {
  width: 104px;
  height: 78px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: var(--ct-ink-soft);
}

.upload-preview {
  width: 104px;
  height: 78px;
  display: block;
}

.spec-editor {
  width: 100%;
}

.spec-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  margin-bottom: 10px;
  border: 1px solid var(--ct-line);
  border-radius: 10px;
  background: #fafcfb;
}

.spec-name {
  width: 150px;
  flex-shrink: 0;
}

.spec-values {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.spec-input {
  width: 168px;
}

.spec-tip {
  font-size: 12px;
  margin-top: 6px;
  line-height: 1.7;
}
</style>
