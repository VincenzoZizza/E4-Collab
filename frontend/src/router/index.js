import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import Dashboard from '../views/DashboardView.vue'
import Session from '../views/SessionView.vue'
import Users from '../views/UsersView.vue'
import { useUserStore } from '@/stores/user.js';

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
      path: '/sessions/:username?',
      name: 'Sessions',
      component: Session,
    },
    {
      path: '/users',
      name: 'Users',
      component: Users,
      meta: { requiresAuth: true }
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

  const userStore = useUserStore();

  document.title = to.name;
  if (to.meta.requiresAuth && userStore.role == 2) {
    next('/dashboard')
  } else {
    next()
  }
});

export default router
