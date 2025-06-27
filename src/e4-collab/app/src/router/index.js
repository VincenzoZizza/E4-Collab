import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import Dashboard from '../views/DashboardView.vue'
import Session from '../views/SessionView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login,
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: Dashboard,
    },
    {
      path: '/sessions',
      name: 'Sessions',
      component: Session,
    },
    {
      /*path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),*/
    },
  ],
})

router.beforeEach((to, from, next) => {
  document.title = to.name;
  next();
});

export default router
