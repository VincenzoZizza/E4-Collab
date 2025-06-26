import './assets/main.css'
import './assets/tables.css'
import './assets/styles.css'
import './assets/navbar.css'
import './assets/datepicker.css'
import './assets/dashboard.css'
import './assets/chart.css'
import './assets/base.css'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)


app.mount('#app')
