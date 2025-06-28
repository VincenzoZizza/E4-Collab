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
  const areaChart = document.getElementById('areaChart');
  const session = await api.getSession(sessionId);

  const eda = parseSensorData(session.startTimestamp, session.edaData, session.edaSampleRate);
  const bvp = parseBVPData(session.startTimestamp, session.bvpData, session.bvpSampleRate);
  const acc = parseACCData(session.startTimestamp, session.accData, session.accSampleRate);
  const ibi = parseIBISensorData(session.startTimestamp, session.ibiData);
  const temp = parseSensorData(session.startTimestamp, session.tempData, session.tempSampleRate);
  const tags = parseTAGSData(session.tagsData);

  const sensors = [eda, bvp, acc, ibi, temp];

  console.log(sensors)

  const defaultDomainsY = [[], [0, 2], [-1000, 1000], [-2, 2], [25, 40]];
  const roundIntervals = [0.3, null, 1, 3, 5];
  const curves = [null, null, d3.curveScale, d3.curveCatmullRom.alpha(0.3), d3.curveCatmullRom.alpha(1)];
  const titles = ['EDA (μS)', 'BVP', 'Accelerometer (g)', 'HR from IBI (BPM)', 'Temperature (°C)'];
  const colors = ['#76b5c5', '#a0312e', '#755c91', '#da8038', '#87a34b'];
  const ticksColors = ['#f0ffff', '#fff0f0', '#e4dbf0', '#fff0e5', '#f5fff0'];
  const tagsColor = '#b50a3b';

  const domainsY = sensors.map((d, i) => d?.length ? null : defaultDomainsY[i]);
  const fromDate = new Date(session.startTimestamp);
  const maxDate = new Date(Math.max(...(sensors[0].map(p => p.x.getTime()))));
  const domainX = [fromDate, maxDate];

  const tooltips = sensors.map((_, i) => {
    return {
      container: document.querySelector(`#areaChartTooltip${i + 1}`),
      label: document.querySelector(`#areaChartTooltip${i + 1} span`)
    }
  });

  //areaChart.innerHTML = '';
  /*chart.createSessionChart(
    sensors,
    tags,
    areaChart,
    tooltips.map(t => t.container),
    tooltips.map(t => t.label),
    null,
    domainsY,
    roundIntervals,
    curves,
    titles,
    colors,
    ticksColors,
    tagsColor
  );*/
}

const layout = {
  grid: { rows: 5, columns: 1, pattern: 'independent' },
  height: 800,
  showlegend: false,
  margin: { t: 30 },
};

const time = Array.from({ length: 100 }, (_, i) => i);

const signals = [
  {
    y: time.map(t => Math.sin(t / 10) * 2),
    x: time,
    type: 'scatter',
    mode: 'lines',
    name: 'EDA',
    yaxis: 'y',
  },
  {
    y: time.map(t => Math.sin(t / 5) * 1000),
    x: time,
    type: 'scatter',
    mode: 'lines',
    name: 'BVP',
    yaxis: 'y2',
  },
  {
    y: time.map(t => Math.sin(t / 8) * 1.5),
    x: time,
    type: 'scatter',
    mode: 'lines',
    name: 'Accelerometer',
    yaxis: 'y3',
  },
  {
    y: time.map(t => 32 + Math.sin(t / 20) * 2),
    x: time,
    type: 'scatter',
    mode: 'lines',
    name: 'Temperature',
    yaxis: 'y4',
  },
  {
    y: time.map(t => 32 + Math.sin(t / 20) * 2),
    x: time,
    type: 'scatter',
    mode: 'lines',
    name: 'Temperature',
    yaxis: 'y5',
  }
];

const layoutFull = {
  ...layout,
  yaxis:  { title: 'EDA (µS)', domain: [0.80, 1.0] },
  yaxis2: { title: 'BVP', domain: [0.60, 0.80] },
  yaxis3: { title: 'Accel (g)', domain: [0.40, 0.60] },
  yaxis4: { title: 'Accel (g)', domain: [0.20, 0.40] },
  yaxis5: { title: 'Temp (°C)', domain: [0.00, 0.20] },
  xaxis:  { title: 'Time (s)', anchor: 'y' },
  xaxis2: { title: 'Time (s)', anchor: 'y2' },
  xaxis3: { title: 'Time (s)', anchor: 'y3' },
  xaxis4: { title: 'Time (s)', anchor: 'y4' }
};

const config = { responsive: true };

// Expose to parent if needed
defineExpose({ loadGraph });
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
