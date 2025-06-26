<template>
  <div class="table-wrapper">
    <table id="sessions-table"></table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import $ from 'jquery';
import * as api from '@/service/api.js';
import * as utils from '@/service/utils.js';

const tableRef = ref(null);
const currentUser = ref(null); // to be set via props or fetch logic if needed
const isEditorOrAdmin = ref(false);
let dataTableInstance = null;

function initializeTable() {
  dataTableInstance = new DataTable(tableRef.value, {
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
    },
    columns: [
      { title: 'Time' },
      { title: 'Duration' },
      { title: 'Device' },
      { title: 'Session ID' },
      { title: 'Actions', sortable: false, width: isEditorOrAdmin.value ? '10rem' : '12rem' }
    ]
  });
}

function createActionButtons(sessionId, onView, onDelete) {
  const container = document.createElement('div');
  container.classList.add('actions-cell');

  if (isEditorOrAdmin.value) {
    container.append(
      utils.createActionButton(['btn', 'btn-sm', 'shadow-sm'], ['fas', 'fa-eye', 'text-primary'], null, () => onView(sessionId), 'View session')
    );
    container.append(
      utils.createActionButton(['btn', 'btn-sm', 'shadow-sm'], ['fas', 'fa-download', 'text-success'], null, () => api.downloadSessionZip(sessionId), 'Download session')
    );
    container.append(
      utils.createActionButton(
        ['btn', 'btn-sm', 'shadow-sm', 'btn-delete'],
        ['fas', 'fa-trash', 'text-danger'],
        null,
        async () => {
          const deleted = await onDelete(sessionId);
          if (deleted) emitLoadTable();
        },
        'Delete session'
      )
    );
  } else {
    container.append(
      utils.createActionButton(['btn', 'btn-outline-primary', 'shadow-sm'], ['fas', 'fa-eye'], 'View Session', () => onView(sessionId))
    );
  }

  return container;
}

async function loadTable(userSessions, onViewButtonClick, onDeleteButtonClick) {
  if (!dataTableInstance) return;
  dataTableInstance.clear().draw();

  const rows = userSessions.map(session => {
    const date = new Date(session.startTimestamp);
    const formattedTime = utils.formatDateUTC(date).replace(' GMT', '\nGMT');
    const duration = utils.formatDuration(session.duration);
    const device = session.deviceName;
    const id = session.id;
    const actions = createActionButtons(id, onViewButtonClick, onDeleteButtonClick);
    return [formattedTime, duration, device, id, actions];
  });

  dataTableInstance.rows.add(rows).draw();
}

defineExpose({ loadTable, setUser(user) {
  currentUser.value = user;
  isEditorOrAdmin.value = user?.role === 'ROLE_EDITOR' || user?.role === 'ROLE_ADMIN';
}});

onMounted(() => {
  initializeTable();
});
</script>

<style scoped>
.table-wrapper {
  overflow-x: auto;
}
</style>
