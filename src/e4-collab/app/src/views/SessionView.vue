<template>
  <div class="d-table d-lg-grid">
    <Datepicker @changeDate="onDateChange()" ref="datepickerRef" />

    <div id="right-content">
      <div id="right-header">
        <h2>
          <span id="sessions-text">{{ sessionText }}</span>
          <span id="sessions-count" class="badge fs-6 rounded-pill text-bg-primary">{{ sessions.length }}</span>
        </h2>
        <h5 id="selected-date" class="text-secondary">{{ selectedDateLabel }}</h5>
      </div>

      <DataTable :data="sessionsTable" class="dataTable">
        <thead>
            <tr>
                <th>Time</th>
                <th>Duration</th>
                <th>Device</th>
                <th>Session ID</th>
                <th>Actions</th>
            </tr>
        </thead>
      </DataTable>
    </div>

    <SessionGraphModal
      :session-id="selectedSessionId"
      v-if="showGraphModal"
      @close="showGraphModal = false"
    />

    <DeleteSessionModal
      :session-id="selectedSessionId"
      v-if="showDeleteModal"
      @confirm="confirmDelete"
      @cancel="showDeleteModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import * as api from '@/service/api.js';
import { getDateRange, formatDuration } from '@/service/utils.js';
import { useUserStore } from '@/stores/user'

import DataTable from 'datatables.net-vue3';
import DataTablesCore from 'datatables.net-dt';

import Datepicker from '@/components/Datepicker.vue';
import SessionsTable from '@/components/SessionTable.vue';
import SessionGraphModal from '@/components/SessionGraphModal.vue';
import DeleteSessionModal from '@/components/DeleteSessionModal.vue';

const route = useRoute();
const userStore = useUserStore()

DataTable.use(DataTablesCore);

const sessions = ref([]);
const selectedDateLabel = ref('');
const sessionText = ref('Sessions');
const selectedSessionId = ref(null);
const showGraphModal = ref(false);
const showDeleteModal = ref(false);

const datepickerRef = ref(null);
const sessionsTableRef = ref(null);

const sessionsTable = ref([]);

const showSessionGraph = (sessionId) => {
  selectedSessionId.value = sessionId;
  showGraphModal.value = true;
};

const deleteSession = (sessionId) => {
  selectedSessionId.value = sessionId;
  showDeleteModal.value = true;
};

const confirmDelete = async () => {
  await api.deleteSession(selectedSessionId.value);
  showDeleteModal.value = false;
  await loadRange(currentRange.value);
};

const currentRange = ref(null);

const loadRange = async (range) => {
  currentRange.value = range;
  await api.getUserSessions(userStore.username, range.from, range.to).then(response => {
        console.log(response)
        sessions.value = response.data
        sessions.value.forEach(session => {
            let row = [new Date(session.startTimestamp),
            formatDuration(session.duration),
            session.deviceName,
            session.username,
            ""]
            sessionsTable.value.push(row);
        })
  })
  selectedDateLabel.value = range.label;
  /*sessionText.value = currentUser.value.username !== username ? `Sessions (${username})` : 'Sessions';*/
};

const onDateChange = async (selectedDate) => {
  const mode = datepickerRef.value.mode;
  if (mode === 'none') return;
  const range = utils.getDateRange(selectedDate.valueOf(), mode, true);
  await loadRange(range);
};

onMounted(async () => {
  const initialMode = datepickerRef.value.mode;
  await loadRange({
    from: 0,
    to: new Date().valueOf(),
    label: "All Sessions"
    }/*utils.getDateRange(new Date(), initialMode, false)*/);
  await datepickerRef.value.loadDates(username);
  datepickerRef.value.refresh({ forceRefresh: true });
});
</script>

<style scoped>
/* Add your styles or use Tailwind/bootstrap */
</style>
