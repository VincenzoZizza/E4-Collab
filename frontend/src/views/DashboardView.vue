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
                  <LineChart v-if="userSummary.sessionsCount != 0"/>
                  <div v-if="userSummary.sessionsCount == 0"><strong>You have no sessions recorded</strong></div>
              </ChartCard>
            </div>
            <div class="col-xl-4 col-lg-5">
              <ChartCard title="Hourly sessions" icon="fa-moon-over-sun">
                  <PieChart v-if="userSummary.sessionsCount != 0"/>
                  <div v-if="userSummary.sessionsCount == 0"><strong>You have no sessions recorded</strong></div>
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
import * as chart from '@/service/chart.js'
import { getUserSummary } from '@/service/api.js'
import DashboardCard from '@/components/DashboardCard.vue'
import ChartCard from '@/components/ChartCard.vue'
import TooltipBox from '@/components/TooltipBox.vue'
import Navbar from '@/components/Navbar.vue'
import LineChart from '@/components/LineChart.vue'
import PieChart from '@/components/PieChart.vue'
import {formatDuration} from '@/service/utils.js'

const userStore = useUserStore()
const userSummary = ref({
  sessionsCount: 0,
  minSessionDuration: 0,
  maxSessionDuration: 0,
  averageSessionDuration: 0,
  monthlySessionsCounts: {},
  hourlySessionsCounts: {}
})

const pieChart = ref(null)
const areaTooltip = ref(null)
const pieTooltip = ref(null)

const x = ref([]);
const y = ref([]);

const assi = ref({});

const formatDurationInside = (seconds) => formatDuration(seconds, 3)

onMounted(async () => {
  const response = await getUserSummary(userStore.username).then(data => {
    userSummary.value = data.data
  })
})
</script>

<style scoped>

</style>
