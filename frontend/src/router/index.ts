import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/patient/login'
    },
    // 患者端路由
    {
      path: '/patient/login',
      name: 'PatientLogin',
      component: () => import('../views/patient/Login.vue')
    },
    {
      path: '/patient',
      component: () => import('../components/layout/PatientLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'appointment',
          name: 'Appointment',
          component: () => import('../views/patient/Appointment.vue')
        },
        {
          path: 'records',
          name: 'MyRecords',
          component: () => import('../views/patient/MyRecords.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/patient/Profile.vue')
        }
      ]
    },
    // 管理员端路由
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('../views/admin/Login.vue')
    },
    {
      path: '/admin',
      component: () => import('../components/layout/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        {
          path: 'department',
          name: 'DepartmentManage',
          component: () => import('../views/admin/Department.vue')
        },
        {
          path: 'schedule',
          name: 'ScheduleManage',
          component: () => import('../views/admin/Schedule.vue')
        },
        {
          path: 'statistics',
          name: 'Statistics',
          component: () => import('../views/admin/Statistics.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isPatientLoggedIn) {
    next('/patient/login')
  } else if (to.meta.requiresAdmin && !userStore.isAdminLoggedIn) {
    next('/admin/login')
  } else {
    next()
  }
})

export default router
