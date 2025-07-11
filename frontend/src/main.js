import './assets/main.css'
import './assets/tables.css'
import './assets/styles.css'
import './assets/navbar.css'
import './assets/datepicker.css'
import './assets/dashboard.css'
import './assets/datatables.css'
import './assets/chart.css'
import './assets/base.css'
import "bootstrap/dist/css/bootstrap.min.css"
import '@fortawesome/fontawesome-free/css/all.min.css'
import "bootstrap"
import "vue-data-ui/style.css" 
import { createApp } from 'vue'
import { createPinia } from 'pinia'


import App from './App.vue'
import router from './router'


const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)


app.mount('#app')
