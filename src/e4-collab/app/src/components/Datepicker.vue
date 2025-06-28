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
      <button class="btn btn-outline-primary btn-sm" @click="getAll()">
        <i class="fas fa-list"></i> All Sessions
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, defineProps , defineEmits, defineExpose} from 'vue';
import Datepicker from 'vanillajs-datepicker/Datepicker';
import * as api from '@/service/api.js';
import * as utils from '@/service/utils.js';

import { useUserStore } from '@/stores/user.js';

const emit = defineEmits(['changeDate'])

const userStore = useUserStore();

const props = defineProps(['username'])

//TODO da testare bene 

const mode = ref('day');
const datepickerContainer = ref(null);
let datepicker = ref(null);
let sessionsTimestamp = [];
let lastUsername = null;

function changeMode(newMode) {
  mode.value = newMode;
  updateView();
}

function updateView() {
  const viewId = mode.value === 'year' ? 2 : mode.value === 'month' ? 1 : 0;
  if (datepicker.value?.picker.currentView.id !== viewId) {
    datepicker.value.picker.changeView(viewId).render();
  }
}

function getAll()
{
  emit("changeDate", new Date().valueOf(), 4);

}

function goToToday() {
  mode.value = 'day';
  datepicker.value.setDate(new Date().valueOf(), { forceRefresh: true, clear: true });
}

async function loadDates() {
  if (props.username != undefined)
  {
    lastUsername = username;
  } 
  else
  {
    lastUsername = userStore.username
  }
  const viewDate = datepicker.value.picker.viewDate.valueOf();
  let range;
  if (mode.value === 'year') range = utils.getDateRange(viewDate, 'year', false);
  else if (mode.value === 'month') range = utils.getDateRange(viewDate, 'year', true);
  else range = utils.getDateRange(viewDate, 'month', true);
  const sessions = await api.getUserSessions(lastUsername);
  console.log(sessions)
  sessionsTimestamp = [...new Set(sessions.data.map(s => s.startTimestamp))];
}

onMounted(async () => {
  await nextTick();
  datepicker.value = new Datepicker(datepickerContainer.value, {
    buttonClass: 'btn',
    container: datepickerContainer.value,
    prevArrow: '<i class="bi bi-caret-left-fill"><</i>',
    nextArrow: '<i class="bi bi-caret-right-fill">></i>',
    todayHighlight: true,
    beforeShowDay : function (date) {
      if (sessionsTimestamp.some(ts => utils.compareDates(ts, date, 'day')))
        return { classes: 'event', tooltip: 'You have a session on this day' };
      if (!datepicker.value || !utils.compareDates(datepicker.value.getDate(), date, 'day'))
        return { classes: 'disabled' };
    },
    beforeShowMonth: function (date) {
      if (sessionsTimestamp.some(ts => utils.compareDates(ts, date, 'month')))
        return { classes: 'event', tooltip: 'You have a session on this month' };
      if (!datepicker.value || !utils.compareDates(datepicker.value.getDate(), date, 'month'))
        return { classes: 'disabled' };
    },
    beforeShowYear: function (date) {
      if (sessionsTimestamp.some(ts => utils.compareDates(ts, date, 'year')))
        return { classes: 'event', tooltip: 'You have a session on this year' };
      if (!datepicker.value || !utils.compareDates(datepicker.value.getDate(), date, 'year'))
        return { classes: 'disabled' };
    }
  });

  datepicker.value.setDate(new Date().valueOf());
  datepicker.value.refresh({ forceRefresh: true });

  datepickerContainer.value.addEventListener('changeView', async e => {
    const viewId = mode.value === 'year' ? 2 : mode.value === 'month' ? 1 : 0;
    if (e.detail.viewId < viewId) {
      datepicker.value.picker.changeView(viewId).render();
    } else if (e.detail.viewId === viewId) {
      if (datepicker.value.getDate().valueOf() !== datepicker.value.picker.viewDate.valueOf()) {
        datepicker.value.setDate(datepicker.value.picker.viewDate.valueOf());
      } else {
        await loadDates();
      }
    }
  });
  datepickerContainer.value.addEventListener('changeDate', async e => {
      console.log(e.detail.viewId);
      console.log(datepicker.value.getDate())

      emit("changeDate", datepicker.value.getDate(), e.detail.viewId);
      //emit con data e tipo vista(0: giorno, 1 mese, 2 anno)
  });
  loadDates().then(() => {
    datepicker.value.picker.changeView(0).render();
  })
});

function reload()
{
  loadDates().then(() => {
    datepicker.value.picker.changeView(0).render();
  })
}

defineExpose({
  reload
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/npm/vanillajs-datepicker@1.3.4/dist/css/datepicker.min.css');
</style>
