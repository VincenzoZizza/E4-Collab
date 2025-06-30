<template>
  <div class="table-wrapper">
    <table ref="usersTableRef" id="users-table" class="display"></table>
    <DataTable :data="usersTable" id="usersTableRef" class="dataTable" :columns="columns" :options="usersTableOption" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import DataTable from 'datatables.net-vue3';
import DataTablesCore from 'datatables.net-dt';
import * as api from '@/service/api.js';
import { useUserStore } from '@/stores/user'
import $ from 'jquery'

import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const usersTable = ref([])

DataTable.use(DataTablesCore);

const decodeRole = ["admin", "editor", "user"]

const usersTableOption = ref({
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
      emptyTable: '<div class="text-center my-3">No users available</div>',
      zeroRecords: '<div class="text-center my-3">No users available</div>'
    }})

const columns= [
    { data:"username", title: 'Username' },
    { data: "email", title: 'Email' },
    { title: 'Role', orderable: false, render: (data, type, row) => {
        if(userStore.role == 0)
        {
          return `<select name="role" data-id="${row.username}" class="btn btn-outline-secondary dropdown-toggle dropdown-users-select">
            <option class="dropdown-item" value="admin">Admin</option>
            <option class="dropdown-item" value="editor">Editor</option>
            <option class="dropdown-item" value="user">User</option>
        </select>`
        }
        else
        {
            return `<strong>${row.role}</strong>`
        }
    }
    },
    { title: 'Actions', orderable: false, width: '12rem', render: (data, type, row) => {
          return `<button class="btn-show-dati btn btn-outline-primary shadow-sm btn-view-session" data-id="${row.username}">View Session</button>`;
    }}
]

const changeUserRole = async (username, role) => {
    api.setUserRole(username, role).then(() => {
      loadUsers()
    }).catch (e => {
      console.error('Errore cambio ruolo:', e)
    }) 
}

// Caricamento e costruzione tabella
const loadUsers = async () => {
  api.getUsers().then(data => {
    console.log(data.data)
    usersTable.value=[];
    data.data.forEach( row => {
          usersTable.value.push({
            username: row.username,
            email: row.email,
            role: decodeRole[row.role]
    })
    })
  })
  }

onMounted(async () => {  

        await  loadUsers();

        $("#usersTableRef").on('click', '.btn-view-session', function () {
          const id = $(this).data('id');
          router.push("sessions/"+id)
        });



      if(userStore.role == 0)
      {
        $("#usersTableRef").on('change', '.dropdown-users-select', function () {
        const id = $(this).data('id');
        const role = $(this).find(":selected").val();
        changeUserRole(id, role == "user" ? 2 : role == "editor" ? 1 : 0);
      });
      } 
})
</script>