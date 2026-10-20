<template>
  <div class="address-edit page">
    <van-nav-bar :title="isEdit ? '编辑地址' : '新增地址'" left-arrow @click-left="$router.back()" />

    <van-form @submit="onSave">
      <van-cell-group inset class="block">
        <van-field
          v-model="form.consignee"
          label="收货人"
          placeholder="请输入收货人姓名"
          :rules="[{ required: true, message: '请输入收货人' }]"
        />
        <van-field
          v-model="form.phone"
          label="手机号"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: /^1\d{10}$/, message: '手机号格式不正确' }
          ]"
        />
        <van-field label="性别">
          <template #input>
            <van-radio-group v-model="form.sex" direction="horizontal">
              <van-radio name="1">男</van-radio>
              <van-radio name="0">女</van-radio>
            </van-radio-group>
          </template>
        </van-field>
      </van-cell-group>

      <van-cell-group inset title="地址（用于计算配送距离）" class="block">
        <van-field
          v-model="form.provinceName"
          label="省"
          placeholder="如：北京市"
          :rules="[{ required: true, message: '请输入省份' }]"
        />
        <van-field
          v-model="form.cityName"
          label="市"
          placeholder="如：北京市"
          :rules="[{ required: true, message: '请输入城市' }]"
        />
        <van-field
          v-model="form.districtName"
          label="区"
          placeholder="如：海淀区"
          :rules="[{ required: true, message: '请输入区县' }]"
        />
        <van-field
          v-model="form.detail"
          label="详细地址"
          type="textarea"
          rows="2"
          maxlength="100"
          show-word-limit
          placeholder="街道、门牌号、楼层"
          :rules="[{ required: true, message: '请输入详细地址' }]"
        />
      </van-cell-group>

      <van-cell-group inset class="block">
        <van-field label="标签">
          <template #input>
            <van-radio-group v-model="form.label" direction="horizontal">
              <van-radio name="家">家</van-radio>
              <van-radio name="公司">公司</van-radio>
              <van-radio name="学校">学校</van-radio>
            </van-radio-group>
          </template>
        </van-field>
      </van-cell-group>

      <div class="submit">
        <van-button block round type="primary" native-type="submit" :loading="saving">
          保存
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { addAddress, getAddress, updateAddress } from '@/api'

defineOptions({ name: 'AddressEditPage' })

const route = useRoute()
const router = useRouter()

const id = computed(() => route.params.id || null)
const isEdit = computed(() => !!id.value)
const saving = ref(false)

const form = reactive({
  id: null,
  consignee: '',
  phone: '',
  sex: '1',
  // 默认填北京，方便演示「配送范围校验」直接通过
  provinceName: '北京市',
  cityName: '北京市',
  districtName: '海淀区',
  detail: '',
  label: '家'
})

async function load() {
  if (!id.value) return
  const detail = await getAddress(id.value)
  Object.assign(form, detail)
}

async function onSave() {
  saving.value = true
  try {
    if (isEdit.value) {
      await updateAddress({ ...form })
    } else {
      await addAddress({ ...form })
    }
    showSuccessToast(isEdit.value ? '修改成功' : '新增成功')
    router.back()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.block {
  margin-top: 12px;
}

.submit {
  padding: 24px 16px 34px;
}
</style>
