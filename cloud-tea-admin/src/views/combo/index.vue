<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">组合套餐</div>
        <div class="page-sub">把多款饮品打包成套餐，价格通常低于单点之和</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增套餐</el-button>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input
          v-model="query.name"
          placeholder="按套餐名称搜索"
          clearable
          style="width: 200px"
          @keyup.enter="reload"
        />
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 180px">
          <el-option v-for="s in comboCategoryOptions" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
        <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column label="套餐" min-width="220">
          <template #default="{ row }">
            <div class="combo-cell">
              <el-image v-if="row.image" :src="row.image" class="thumb" fit="cover" :preview-src-list="[row.image]" preview-teleported />
              <div v-else class="thumb placeholder"><el-icon><ShoppingBag /></el-icon></div>
              <div>
                <div class="combo-name">{{ row.name }}</div>
                <div class="muted combo-desc">{{ row.description || '—' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="所属分类" width="130" />
        <el-table-column label="套餐价" width="100">
          <template #default="{ row }">
            <span class="price">{{ Number(row.price).toFixed(2) }}</span> 元
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v) => toggleStatus(row, v)" />
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
    <el-dialog v-model="formVisible" :title="form.id ? '编辑组合套餐' : '新增组合套餐'" width="760px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="套餐名称" prop="name">
              <el-input v-model="form.name" placeholder="如：双人下午茶套餐" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择组合套餐分类" style="width: 100%">
                <el-option v-for="s in comboCategoryOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="套餐价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图片">
              <el-upload :show-file-list="false" :http-request="doUpload" accept="image/*" class="uploader">
                <el-image v-if="form.image" :src="form.image" class="upload-preview" fit="cover" />
                <div v-else class="upload-empty">
                  <el-icon><Plus /></el-icon><span>上传图片</span>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="120" show-word-limit />
        </el-form-item>

        <!-- 套餐内饮品 -->
        <el-form-item label="套餐内容">
          <div class="picker">
            <el-select v-model="pick.categoryId" placeholder="先选饮品分类" style="width: 160px" @change="onPickCategory">
              <el-option v-for="s in drinkCategoryOptions" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-select v-model="pick.drinkId" placeholder="再选饮品" style="width: 190px" :disabled="!pick.categoryId">
              <el-option v-for="d in pickDrinks" :key="d.id" :label="`${d.name}（${Number(d.price).toFixed(2)}元）`" :value="d.id" />
            </el-select>
            <el-input-number v-model="pick.copies" :min="1" :max="20" style="width: 110px" />
            <el-button type="primary" plain :icon="Plus" @click="addComboDrink">加入套餐</el-button>
          </div>

          <el-table v-if="form.comboDrinks.length" :data="form.comboDrinks" size="small" class="picked-table">
            <el-table-column prop="name" label="饮品" min-width="150" />
            <el-table-column label="单价" width="100">
              <template #default="{ row }">{{ Number(row.price).toFixed(2) }} 元</template>
            </el-table-column>
            <el-table-column label="份数" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.copies" :min="1" :max="20" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="110">
              <template #default="{ row }">
                <span class="price">{{ (Number(row.price) * row.copies).toFixed(2) }}</span> 元
              </template>
            </el-table-column>
            <el-table-column width="70">
              <template #default="{ $index }">
                <el-button link type="danger" :icon="Delete" @click="form.comboDrinks.splice($index, 1)" />
              </template>
            </el-table-column>
          </el-table>
          <div v-else class="muted empty-tip">还没有添加饮品</div>

          <div v-if="form.comboDrinks.length" class="sum-tip">
            单点合计 <b>{{ originalTotal }}</b> 元
            <span v-if="Number(form.price) > 0">
              ，套餐价 <b>{{ Number(form.price).toFixed(2) }}</b> 元
              <em :class="Number(form.price) < Number(originalTotal) ? 'save' : 'warn'">
                {{ Number(form.price) < Number(originalTotal)
                  ? `省 ${(originalTotal - Number(form.price)).toFixed(2)} 元`
                  : '比单点还贵，确认一下' }}
              </em>
            </span>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  addCombo,
  deleteCombo,
  getCombo,
  listDrink,
  listCategory,
  pageCombo,
  setComboStatus,
  updateCombo,
  uploadFile
} from '@/api'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)

const drinkCategoryOptions = ref([]) // type=1 饮品分类
const comboCategoryOptions = ref([]) // type=2 组合套餐分类
const pickDrinks = ref([])

const query = reactive({ page: 1, pageSize: 10, name: '', categoryId: null, status: null })
const pick = reactive({ categoryId: null, drinkId: null, copies: 1 })

const formVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  image: '',
  description: '',
  comboDrinks: []
})

const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入套餐价', trigger: 'blur' }]
}

const originalTotal = computed(() =>
  form.comboDrinks
    .reduce((sum, d) => sum + Number(d.price) * d.copies, 0)
    .toFixed(2)
)

async function load() {
  loading.value = true
  try {
    const data = await pageCombo({ ...query })
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

async function loadCategoryOptions() {
  const [drinks, combos] = await Promise.all([listCategory(1), listCategory(2)])
  drinkCategoryOptions.value = drinks || []
  comboCategoryOptions.value = combos || []
}

async function onPickCategory(categoryId) {
  pick.drinkId = null
  pickDrinks.value = categoryId ? (await listDrink(categoryId)) || [] : []
}

function addComboDrink() {
  if (!pick.drinkId) {
    ElMessage.warning('请先选择要加入的饮品')
    return
  }
  const drink = pickDrinks.value.find((d) => d.id === pick.drinkId)
  if (!drink) return

  const existing = form.comboDrinks.find((d) => d.drinkId === drink.id)
  if (existing) {
    existing.copies += pick.copies
  } else {
    form.comboDrinks.push({
      drinkId: drink.id,
      name: drink.name,
      price: Number(drink.price),
      copies: pick.copies
    })
  }
  pick.drinkId = null
  pick.copies = 1
}

async function openForm(row) {
  if (row) {
    const detail = await getCombo(row.id)
    Object.assign(form, {
      id: detail.id,
      name: detail.name,
      categoryId: detail.categoryId,
      price: Number(detail.price),
      image: detail.image || '',
      description: detail.description || '',
      comboDrinks: (detail.comboDrinks || []).map((d) => ({
        drinkId: d.drinkId,
        name: d.name,
        price: Number(d.price),
        copies: d.copies
      }))
    })
  } else {
    Object.assign(form, { id: null, name: '', categoryId: null, price: 0, image: '', description: '', comboDrinks: [] })
  }
  Object.assign(pick, { categoryId: null, drinkId: null, copies: 1 })
  pickDrinks.value = []
  formVisible.value = true
}

async function doUpload({ file }) {
  try {
    form.image = await uploadFile(file)
    ElMessage.success('图片上传成功')
  } catch {
    // 拦截器已提示
  }
}

async function onSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.comboDrinks.length) {
    ElMessage.warning('套餐内至少要有一款饮品')
    return
  }

  saving.value = true
  try {
    const payload = { ...form }
    if (form.id) {
      await updateCombo(payload)
    } else {
      await addCombo(payload)
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
  await setComboStatus(next, row.id)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除套餐「${row.name}」吗？上架中的套餐不能删除。`, '删除确认', {
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await deleteCombo(row.id)
    ElMessage.success('删除成功')
    load()
  } catch {
    // 拦截器已提示
  }
}

onMounted(() => {
  loadCategoryOptions()
  load()
})
</script>

<style scoped>
.combo-cell {
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

.combo-name {
  font-weight: 600;
  font-size: 14px;
}

.combo-desc {
  font-size: 12px;
  margin-top: 3px;
  max-width: 280px;
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

.picker {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.picked-table {
  margin-bottom: 8px;
}

.empty-tip {
  font-size: 12.5px;
  padding: 14px;
  text-align: center;
  border: 1px dashed var(--ct-line);
  border-radius: 10px;
}

.sum-tip {
  font-size: 12.5px;
  color: var(--ct-ink-soft);
}

.sum-tip b {
  color: var(--ct-green-deep);
}

.sum-tip em {
  font-style: normal;
  margin-left: 6px;
}

.sum-tip .save {
  color: #3f9d63;
}

.sum-tip .warn {
  color: #d98820;
}
</style>
