<template>
  <div id="datepicker-wrapper">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/vanillajs-datepicker@1.3.4/dist/css/datepicker.min.css" />
    <link rel="stylesheet" href="/css/components/datepicker.css" />

    <ul class="nav nav-tabs mb-2" role="tablist">
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          :class="{ active: mode === 'day' }"
          @click="changeMode('day')"
        >Day</button>
      </li>
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          :class="{ active: mode === 'month' }"
          @click="changeMode('month')"
        >Month</button>
      </li>
      <li class="nav-item" role="presentation">
        <button
          class="nav-link"
          :class="{ active: mode === 'year' }"
          @click="changeMode('year')"
        >Year</button>
      </li>
    </ul>

    <div ref="datepickerContainer" id="datepicker-container"></div>

    <div class="d-grid gap-2 mt-4">
      <button class="btn btn-outline-secondary btn-sm" @click="goToToday">
        <i class="fas fa-calendar-day"></i> Today
      </button>
      <button class="btn btn-outline-primary btn-sm" @click="$emit('all-sessions')">
        <i class="fas fa-list"></i> All Sessions
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import Datepicker from 'vanillajs-datepicker/Datepicker';
import * as api from '@/service/api.js';
import * as utils from '@/service/utils.js';

//TODO da testare bene 

const mode = ref('day');
const datepickerContainer = ref(null);
let datepicker = null;
let sessionsTimestamp = [];
let lastUsername = null;

function changeMode(newMode) {
  mode.value = newMode;
  updateView();
}

function updateView() {
  const viewId = mode.value === 'year' ? 2 : mode.value === 'month' ? 1 : 0;
  if (datepicker?.picker.currentView.id !== viewId) {
    datepicker.picker.changeView(viewId).render();
  }
}

function goToToday() {
  mode.value = 'day';
  datepicker.setDate(new Date().valueOf(), { forceRefresh: true, clear: true });
}

async function loadDates(username = null) {
  if (username) lastUsername = username;
  const viewDate = datepicker.picker.viewDate.valueOf();
  let range;
  if (mode.value === 'year') range = utils.getDateRange(viewDate, 'year', false);
  else if (mode.value === 'month') range = utils.getDateRange(viewDate, 'year', true);
  else range = utils.getDateRange(viewDate, 'month', true);
  const sessions = await api.getUserSessions(lastUsername);
  sessionsTimestamp = [...new Set(sessions.map(s => s.startTimestamp))];
}

onMounted(async () => {
  await nextTick();
  datepicker = new Datepicker(datepickerContainer.value, {
    buttonClass: 'btn',
    container: datepickerContainer.value,
    prevArrow: '<i class="bi bi-caret-left-fill"></i>',
    nextArrow: '<i class="bi bi-caret-right-fill"></i>',
    todayHighlight: true,
    beforeShowDay(date) {
      if (sessionsTimestamp.some(ts => utils.compareDates(ts, date, 'day')))
        return { classes: 'event', tooltip: 'You have a session on this day' };
      if (!datepicker || !utils.compareDates(datepicker.getDate(), date, 'day'))
        return { classes: 'disabled' };
    },
    beforeShowMonth(date) {
      if (sessionsTimestamp.some(ts => utils.compareDates(ts, date, 'month')))
        return { classes: 'event', tooltip: 'You have a session on this month' };
      if (!datepicker || !utils.compareDates(datepicker.getDate(), date, 'month'))
        return { classes: 'disabled' };
    },
    beforeShowYear(date) {
      if (sessionsTimestamp.some(ts => utils.compareDates(ts, date, 'year')))
        return { classes: 'event', tooltip: 'You have a session on this year' };
      if (!datepicker || !utils.compareDates(datepicker.getDate(), date, 'year'))
        return { classes: 'disabled' };
    }
  });

  datepicker.setDate(new Date().valueOf());
  datepicker.refresh({ forceRefresh: true });

  datepickerContainer.value.addEventListener('changeView', async e => {
    const viewId = mode.value === 'year' ? 2 : mode.value === 'month' ? 1 : 0;
    if (e.detail.viewId < viewId) {
      datepicker.picker.changeView(viewId).render();
    } else if (e.detail.viewId === viewId) {
      if (datepicker.getDate().valueOf() !== datepicker.picker.viewDate.valueOf()) {
        datepicker.setDate(datepicker.picker.viewDate.valueOf());
      } else {
        await loadDates();
      }
    }
  });
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/npm/vanillajs-datepicker@1.3.4/dist/css/datepicker.min.css');
</style>
