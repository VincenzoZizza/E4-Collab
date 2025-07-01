<template>
  <div id="session-graph">
    <!--<div
      v-for="n in 7"
      :key="n"
      :id="`areaChartTooltip${n}`"
      class="tooltip-box"
    >
      <span></span>
    </div>
    <svg id="areaChart"></svg>-->

    <Plotly :data="signals" :layout="layoutFull" :display-mode-bar="false"></Plotly>
  </div>
</template>

<script setup>
import { onMounted, ref , defineProps } from 'vue';
import * as d3 from 'd3'
import * as api from '@/service/api.js';
import * as chart from '@/service/chart.js';
import Plotly from '@aurium/vue-plotly'

const edaRef = ref({})
const bvpRef = ref({})
const accRef = ref({})
const ibiRef = ref({})
const tempRef = ref({}) 
const tagsRef = ref({})

const signals = ref([]);

const sessionGraphRef = ref(null);

const props = defineProps({ sessionId: Number });

function parseSensorData(startTimestamp, data, sampleRate) {
  const trimmedData = data?.trim();
  if (!trimmedData) return [];
  const values = trimmedData.split('\n').map(row => parseFloat(row.split(',')[0]));
  return values.map((v, i) => ({ x: new Date(startTimestamp + i * (1000 / sampleRate)), y: v }))
              .sort((a, b) => a.x - b.x);
}

function parseBVPData(startTimestamp, data, sampleRate) {
  return parseSensorData(startTimestamp, data, sampleRate).map(d => ({ ...d, y: -d.y }));
}

function parseACCData(startTimestamp, data, sampleRate) {
  const trimmedData = data?.trim();
  if (!trimmedData) return [];
  const rawValues = trimmedData.split('\n').map(row => {
    const [x, y, z] = row.split(',').map(parseFloat);
    return { x, y, z };
  });
  const convertToG = val => val * 2 / 128;
  let prev = { x: 0, y: 0, z: 0 }, sum = 0, avg = 0;

  const filtered = rawValues.map((sample, i) => {
    const xG = convertToG(sample.x);
    const yG = convertToG(sample.y);
    const zG = convertToG(sample.z);
    const magnitude = Math.sqrt(xG ** 2 + yG ** 2 + zG ** 2);
    if (i > 0) {
      sum += Math.max(Math.abs(sample.x - prev.x), Math.abs(sample.y - prev.y), Math.abs(sample.z - prev.z));
      if ((i + 1) % 32 === 0) {
        avg = 0.9 * avg + 0.1 * (sum / 32);
        sum = 0;
      }
    }
    prev = sample;
    return avg;
  });

  return filtered.map((v, i) => ({ x: new Date(startTimestamp + i * (1000 / sampleRate)), y: v }));
}

function parseIBISensorData(startTimestamp, data) {
  const trimmed = data?.trim();
  if (!trimmed) return [];
  return trimmed.split('\n').map(row => {
    const [time, ibi] = row.split(',');
    return { x: new Date(startTimestamp + parseFloat(time) * 1000), y: 60 / parseFloat(ibi) };
  });
}

function parseTAGSData(data) {
  const trimmed = data?.trim();
  if (!trimmed) return [];
  return trimmed.split('\n').map(row => parseInt(row * 1000));
}

onMounted(() => {
  loadGraph(props.sessionId);
})

async function loadGraph(sessionId) {
  const session = await api.getSession(sessionId);

  const eda = parseSensorData(session.startTimestamp, session.edaData, session.edaSampleRate);
  const bvp = parseBVPData(session.startTimestamp, session.bvpData, session.bvpSampleRate);
  const acc = parseACCData(session.startTimestamp, session.accData, session.accSampleRate);
  const ibi = parseIBISensorData(session.startTimestamp, session.ibiData);
  const temp = parseSensorData(session.startTimestamp, session.tempData, session.tempSampleRate);
  const tags = parseTAGSData(session.tagsData);

  const sensors = [eda, bvp, acc, ibi, temp];

  console.log(sensors)
  
  edaRef.value.x = eda.map(v => v.x)
  edaRef.value.y = eda.map(v => v.y)

  bvpRef.value.x = bvp.map(v => v.x)
  bvpRef.value.y = bvp.map(v => v.y)

  accRef.value.x = acc.map(v => v.x)
  accRef.value.y = acc.map(v => v.y)

  ibiRef.value.x = ibi.map(v => v.x)
  ibiRef.value.y = ibi.map(v => v.y)

  tempRef.value.x = temp.map(v => v.x)
  tempRef.value.y = temp.map(v => v.y)

  signals.value = [
  {
    y: edaRef.value.y,
    x: edaRef.value.x,
    type: 'scatter',
    mode: 'lines',
    name: 'EDA',
    yaxis: 'y',
  },
  {
    y: bvpRef.value.y,
    x: bvpRef.value.x,
    type: 'scatter',
    mode: 'lines',
    name: 'BVP',
    yaxis: 'y2',
  },
  {
    y: accRef.value.y,
    x: accRef.value.x,
    type: 'scatter',
    mode: 'lines',
    name: 'Accelerometer',
    yaxis: 'y3',
  },
  {
    y: ibiRef.value.y,
    x: ibiRef.value.x,
    type: 'scatter',
    mode: 'lines',
    name: 'HR from IBI (BPM)',
    yaxis: 'y4',
  },
  {
    y: tempRef.value.y,
    x: tempRef.value.x,
    type: 'scatter',
    mode: 'lines',
    name: 'Temperature',
    yaxis: 'y5',
  }
];

}

const layout = ref({
  grid: { rows: 5, columns: 1, pattern: 'independent' },
  height: 800,
  showlegend: false,
  margin: { t: 30 },
});

const layoutFull = ref({
  ...layout.value,
  yaxis:  { title: 'EDA (µS)', domain: [0.80, 1.0] },
  yaxis2: { title: 'BVP', domain: [0.60, 0.80] },
  yaxis3: { title: 'Accel (g)', domain: [0.40, 0.60] },
  yaxis4: { title: 'Accel (g)', domain: [0.20, 0.40] },
  yaxis5: { title: 'Temp (°C)', domain: [0.00, 0.20] },
  xaxis:  { title: 'Time (s)', anchor: 'y' },
  xaxis2: { title: 'Time (s)', anchor: 'y2' },
  xaxis3: { title: 'Time (s)', anchor: 'y3' },
  xaxis4: { title: 'Time (s)', anchor: 'y4' },
  xaxis5: { title: 'Time (s)', anchor: 'y5' }
});
</script>

<style scoped>
.tooltip-box {
  display: none;
  position: fixed;
  width: fit-content;
  height: fit-content;
  top: 0px;
  right: 0px;
  background-color: #f7fdffd9;
  padding: 0.2rem;
  border: 1px solid #777676eb;
  border-radius: 5px;
  transform: translate(1rem, -50%);
  z-index: 100;
}
</style>
