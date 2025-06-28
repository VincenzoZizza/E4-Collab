<template>
  <div class="d-table d-lg-grid">
  <div class="left-content">
    <Datepicker @changeDate="onDateChange" ref="datepickerRef"/>
    </div>
    <div id="right-content">
      <div id="right-header">
        <h2>
          <span id="sessions-text">{{ sessionText }}</span>
          <span id="sessions-count" class="badge fs-6 rounded-pill text-bg-primary">{{ sessions.length }}</span>
        </h2>
        <h5 id="selected-date" class="text-secondary">{{ selectedDateLabel }}</h5>
      </div>
      <DataTable :data="sessionsTable" id="sessionsTableRef" class="dataTable" :columns="columns" :options="sessionsTableOption">
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
import $ from 'jquery'

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
const sessionsTableOption = ref({
    lengthChange: false,
    searching: false,
    info: false,
    responsive: true,
    autoWidth: false,
    fixedHeader: {
      header: true,
      footer: true
    },
    language: {
      emptyTable: '<div class="text-center my-3">No sessions available</div>',
      zeroRecords: '<div class="text-center my-3">No sessions available</div>'
    }})

const columns= [
    { data: "time" ,title: "Time" },
    { data: "dur", title: "Duration" },
    { data: "dev", title: "Device" },
    { data: "id", title: "Session ID" },
    { data: null, title: "Actions", width: "12rem", sortable: false, render: (data, type, row) => {
        if(userStore.role == 2) {
          return `<button class="btn-show-dati btn btn-outline-primary shadow-sm" data-id="${row.id}">View Session</button>`;
        }else if(userStore.role == 0 || userStore.role == 1)
        {
          return `<button class="btn-show-dati btn btn-outline-primary shadow-sm" data-id="${row.id}"><i class="fas fa-eye text-primary"></i></button><button class="btn-down-dati btn btn-outline-primary shadow-sm" data-id="${row.id}"><i class="fas fa-download text-success"></i></button><button class="btn-delete-dati btn btn-outline-primary shadow-sm" data-id="${row.id}"><i class="fas fa-trash text-danger"></i></button>`
        }
    }}
]

      // Delego l'evento click al bottone "Elimina"

const showSessionGraph = (sessionId) => {
  selectedSessionId.value = sessionId;
  showGraphModal.value = true;
};

const deleteSession = (sessionId) => {
  selectedSessionId.value = sessionId;
  showDeleteModal.value = true;
};

const confirmDelete = async () => {
  await api.deleteSession(selectedSessionId.value).then(response => {
      showDeleteModal.value = false;
      datepickerRef.value.reload
      ()
      loadRange(currentRange.value);
  })
};

const downloadFile = async () => {
  await api.downloadSessionZip(selectedSessionId.value)
}

const currentRange = ref(null);

const loadRange = async (range) => {
  currentRange.value = range;
  await api.getUserSessions(userStore.username, range.from, range.to).then(response => {
        sessionsTable.value = []
        console.log(response)
        sessions.value = response.data
        sessions.value.forEach(session => {
            let row = {
            time: new Date(session.startTimestamp).toLocaleString("it-IT", { timeZone: "UTC" }),
            dur: formatDuration(session.duration),
            dev: session.deviceName,
            id: session.id,
            }
            sessionsTable.value.push(row);
        })
  })
  selectedDateLabel.value = range.label;
  /*sessionText.value = currentUser.value.username !== username ? `Sessions (${username})` : 'Sessions';*/
};

const onDateChange = async (selectedDate, view) => {
  console.log(selectedDate)
  console.log(view)
  const range = getDateRange(selectedDate.valueOf(), view, false);
  console.log(range)
  await loadRange(range);
};

onMounted(async () => {
  await loadRange({
    from: new Date(new Date() - 1).valueOf(),
    to: new Date().valueOf(),
    label: "Today"
    })

    $("#sessionsTableRef").on('click', '.btn-show-dati', function () {
        const id = $(this).data('id');
        console.log(id);
        selectedSessionId.value = id;
        showGraphModal.value = true;
      });

      if(userStore.role == 0 || userStore == 1)
      {
        $("#sessionsTableRef").on('click', '.btn-down-dati', function () {
          const id = $(this).data('id');
          console.log(id);
          selectedSessionId.value = id;
          downloadFile()
        });

        $("#sessionsTableRef").on('click', '.btn-delete-dati', function () {
          const id = $(this).data('id');
          console.log(id);
          showDeleteModal.value = true;
          selectedSessionId.value = id;
        });
      } 
});


</script>

<style scoped>
@import 'datatables.net-dt';

th {
  background-color: #143d59;
  color: white;
}
</style>
