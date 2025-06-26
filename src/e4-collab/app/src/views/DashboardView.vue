<template>
  <div id="main" class="d-table d-lg-grid">
    <div id="right-content">
      <div id="right-body">
        <div id="dashboard" class="container-fluid pt-3">
          <div class="row">
            <DashboardCard title="Sessions count" :value="userSummary.sessionsCount" icon="fa-layer-group" />
            <DashboardCard title="Shortest session" :value="formatDurationInside(userSummary.minSessionDuration)" icon="fa-hourglass-half" />
            <DashboardCard title="Longest session" :value="formatDurationInside(userSummary.maxSessionDuration)" icon="fa-hourglass-end" />
            <DashboardCard title="Average session duration" :value="formatDurationInside(userSummary.averageSessionDuration)" icon="fa-hourglass" />
          </div>

          <div class="row">
            <div class="col-xl-8 col-lg-7">
              <ChartCard title="Monthly sessions" icon="fa-calendar-days">
                <div class="chart-area" style="width: 100%; height: 25rem">
                  <TooltipBox ref="areaTooltip" />
                  <svg ref="areaChart" style="width: 100%; height: 100%"></svg>
                </div>
              </ChartCard>
            </div>
            <div class="col-xl-4 col-lg-5">
              <ChartCard title="Hourly sessions" icon="fa-moon-over-sun">
                <div class="chart-area" style="width: 100%; height: 25rem">
                  <TooltipBox ref="pieTooltip" />
                  <svg ref="pieChart" style="width: 100%; height: 100%"></svg>
                </div>
              </ChartCard>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { formatDuration } from '@/service/utils.js'
import * as chart from '@/service/api.js'
import { getUserSummary } from '@/service/api.js'
import DashboardCard from '@/components/DashboardCard.vue'
import ChartCard from '@/components/ChartCard.vue'
import TooltipBox from '@/components/TooltipBox.vue'

const userStore = useUserStore()
const userSummary = ref({
  sessionsCount: 0,
  minSessionDuration: 0,
  maxSessionDuration: 0,
  averageSessionDuration: 0,
  monthlySessionsCounts: {},
  hourlySessionsCounts: {}
})

const areaChart = ref(null)
const pieChart = ref(null)
const areaTooltip = ref(null)
const pieTooltip = ref(null)

const formatDurationInside = (seconds) => formatDuration(seconds, 3)

onMounted(async () => {
  const response = await getUserSummary(userStore.username)
  userSummary.value = response

  const today = new Date()
  const fromMonth = new Date(today.getFullYear() - 1, today.getMonth() + 1, 1)
  const toMonth = new Date(today.getFullYear(), today.getMonth(), 1)

  const monthlyData = Object.entries(response.monthlySessionsCounts)
    .map(([key, val]) => ({ x: new Date(key), y: val }))
    .sort((a, b) => a.x - b.x)

  let startIndex = monthlyData.findLastIndex(data => data.x <= fromMonth)
  let endIndex = monthlyData.findIndex(data => data.x >= toMonth)
  startIndex = startIndex > 0 ? startIndex - 1 : 0
  endIndex = endIndex > -1 ? endIndex + 1 : monthlyData.length - 1

  chart.createAreaChart(
    monthlyData.slice(startIndex, endIndex),
    areaChart.value,
    areaTooltip.value.$el,
    areaTooltip.value.$el.querySelector('span'),
    [fromMonth, toMonth]
  )

  const hourlyData = [
    { label: 'Morning', value: response.hourlySessionsCounts.MORNING ?? 0 },
    { label: 'Afternoon', value: response.hourlySessionsCounts.AFTERNOON ?? 0 },
    { label: 'Evening', value: response.hourlySessionsCounts.EVENING ?? 0 },
  ]

  chart.createPieChart(
    hourlyData,
    pieChart.value,
    pieTooltip.value.$el,
    pieTooltip.value.$el.querySelector('span'),
    [fromMonth, toMonth]
  )
})
</script>

<style scoped>

</style>
