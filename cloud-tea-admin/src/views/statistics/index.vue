<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">数据统计</div>
        <div class="page-sub">营业额、用户、订单与销量趋势</div>
      </div>
      <div class="toolbar" style="margin: 0">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :clearable="false"
          @change="loadAll"
        />
        <el-button :icon="Download" :loading="exporting" @click="onExport">导出报表</el-button>
      </div>
    </div>

    <!-- 区间汇总 -->
    <div class="summary" v-loading="loading">
      <div class="sum-item">
        <div class="sum-label">区间营业额</div>
        <div class="sum-value">{{ turnoverSum }} <i>元</i></div>
      </div>
      <div class="sum-item">
        <div class="sum-label">区间订单总数</div>
        <div class="sum-value">{{ orderReport.totalOrderCount ?? 0 }} <i>单</i></div>
      </div>
      <div class="sum-item">
        <div class="sum-label">区间有效订单</div>
        <div class="sum-value">{{ orderReport.validOrderCount ?? 0 }} <i>单</i></div>
      </div>
      <div class="sum-item">
        <div class="sum-label">订单完成率</div>
        <div class="sum-value">{{ ((orderReport.orderCompletionRate ?? 0) * 100).toFixed(1) }} <i>%</i></div>
      </div>
    </div>

    <div class="cards" v-loading="loading">
      <div class="card">
        <div class="card-title">营业额趋势</div>
        <BaseChart :option="turnoverOption" />
      </div>

      <div class="card">
        <div class="card-title">用户增长</div>
        <BaseChart :option="userOption" />
      </div>

      <div class="card">
        <div class="card-title">订单趋势</div>
        <BaseChart :option="orderOption" />
      </div>

      <div class="card">
        <div class="card-title">饮品销量 Top10</div>
        <BaseChart :option="top10Option" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import BaseChart from '@/components/BaseChart.vue'
import {
  exportReport,
  ordersStatistics,
  salesTop10,
  turnoverStatistics,
  userStatistics
} from '@/api'
import { lastNDays, splitCsv, splitCsvNumbers } from '@/utils/format'

const GREEN = '#2e7d6b'
const AMBER = '#c9a227'

// 默认看最近 30 天：演示数据是按 30 天铺的，7 天窗口会把增长趋势压成直线
const range = ref(lastNDays(30))
const loading = ref(false)
const exporting = ref(false)

const orderReport = ref({})
const turnoverOption = ref({})
const userOption = ref({})
const orderOption = ref({})
const top10Option = ref({})

// 区间营业额汇总。抽成 computed，避免在模板里对可能还没有 series 的 option 取下标
const turnoverValues = ref([])
const turnoverSum = computed(() =>
  turnoverValues.value.reduce((a, b) => a + (Number(b) || 0), 0).toFixed(2)
)

const axisBase = {
  grid: { left: 48, right: 22, top: 34, bottom: 34 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisLine: { lineStyle: { color: '#dde5e2' } },
    axisLabel: { color: '#6b7c78', fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#eef3f1' } },
    axisLabel: { color: '#6b7c78', fontSize: 11 }
  }
}

function lineOption(dates, series) {
  return {
    ...axisBase,
    legend: { right: 10, top: 0, icon: 'roundRect', itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11.5, color: '#6b7c78' } },
    xAxis: { ...axisBase.xAxis, data: dates },
    series
  }
}

async function loadAll() {
  if (!range.value?.length) return
  const [begin, end] = range.value
  loading.value = true

  const [t, u, o, top] = await Promise.allSettled([
    turnoverStatistics({ begin, end }),
    userStatistics({ begin, end }),
    ordersStatistics({ begin, end }),
    salesTop10({ begin, end })
  ])

  if (t.status === 'fulfilled') {
    const dates = splitCsv(t.value.dateList)
    const values = splitCsvNumbers(t.value.turnoverList)
    turnoverValues.value = values
    turnoverOption.value = lineOption(dates, [
      {
        name: '营业额（元）',
        type: 'line',
        smooth: true,
        symbolSize: 5,
        data: values,
        itemStyle: { color: GREEN },
        lineStyle: { width: 2.4, color: GREEN },
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(46,125,107,.26)' },
              { offset: 1, color: 'rgba(46,125,107,.02)' }
            ]
          }
        }
      }
    ])
  }

  if (u.status === 'fulfilled') {
    const dates = splitCsv(u.value.dateList)
    userOption.value = lineOption(dates, [
      {
        name: '用户总量',
        type: 'line',
        smooth: true,
        symbolSize: 5,
        data: splitCsvNumbers(u.value.totalUserList),
        itemStyle: { color: GREEN },
        lineStyle: { width: 2.4, color: GREEN }
      },
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbolSize: 5,
        data: splitCsvNumbers(u.value.newUserList),
        itemStyle: { color: AMBER },
        lineStyle: { width: 2.4, color: AMBER }
      }
    ])
  }

  if (o.status === 'fulfilled') {
    orderReport.value = o.value
    const dates = splitCsv(o.value.dateList)
    orderOption.value = lineOption(dates, [
      {
        name: '订单总数',
        type: 'line',
        smooth: true,
        symbolSize: 5,
        data: splitCsvNumbers(o.value.orderCountList),
        itemStyle: { color: '#7b8f8a' },
        lineStyle: { width: 2.2, color: '#7b8f8a' }
      },
      {
        name: '有效订单',
        type: 'line',
        smooth: true,
        symbolSize: 5,
        data: splitCsvNumbers(o.value.validOrderCountList),
        itemStyle: { color: GREEN },
        lineStyle: { width: 2.4, color: GREEN }
      }
    ])
  }

  if (top.status === 'fulfilled') {
    const names = splitCsv(top.value.nameList)
    const numbers = splitCsvNumbers(top.value.numberList)
    top10Option.value = {
      grid: { left: 108, right: 40, top: 18, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#eef3f1' } },
        axisLabel: { color: '#6b7c78', fontSize: 11 }
      },
      yAxis: {
        type: 'category',
        // ECharts 类目轴自下而上，反转后销量最高的排在最上面
        data: names.slice().reverse(),
        axisLine: { lineStyle: { color: '#dde5e2' } },
        axisLabel: { color: '#4a5c58', fontSize: 11.5 }
      },
      series: [
        {
          name: '销量',
          type: 'bar',
          barWidth: 13,
          data: numbers.slice().reverse(),
          itemStyle: {
            borderRadius: [0, 6, 6, 0],
            color: {
              type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
              colorStops: [
                { offset: 0, color: '#4a9c88' },
                { offset: 1, color: AMBER }
              ]
            }
          },
          label: { show: true, position: 'right', fontSize: 11, color: '#6b7c78' }
        }
      ]
    }
  }

  loading.value = false
}

async function onExport() {
  exporting.value = true
  try {
    const res = await exportReport()
    const blob = new Blob([res.data], {
      type: res.headers['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `云栖茶饮运营数据报表_${range.value[0]}_${range.value[1]}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('报表已开始下载')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 14px;
}

.sum-item {
  background: #fff;
  border: 1px solid var(--ct-line);
  border-radius: 12px;
  padding: 16px 18px;
}

.sum-label {
  font-size: 12.5px;
  color: var(--ct-ink-soft);
}

.sum-value {
  margin-top: 8px;
  font-size: 23px;
  font-weight: 600;
  color: var(--ct-green-deep);
}

.sum-value i {
  font-size: 12.5px;
  font-style: normal;
  font-weight: 400;
  color: var(--ct-ink-soft);
  margin-left: 3px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.card-title {
  font-size: 14.5px;
  font-weight: 600;
  margin-bottom: 6px;
}

@media (max-width: 1200px) {
  .summary,
  .cards {
    grid-template-columns: 1fr;
  }
}
</style>
