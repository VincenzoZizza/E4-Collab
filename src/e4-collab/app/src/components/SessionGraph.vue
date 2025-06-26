<template>
  <div id="session-graph">
    <div
      v-for="n in 7"
      :key="n"
      :id="`areaChartTooltip${n}`"
      class="tooltip-box"
    >
      <span></span>
    </div>
    <svg id="areaChart"></svg>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import * as api from '@/service/api.js';
import * as chart from '@/service/chart.js';

const sessionGraphRef = ref(null);

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

  areaChart.innerHTML = '';
  chart.createSessionChart(
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
  );
}

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
