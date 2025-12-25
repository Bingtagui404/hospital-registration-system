import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Patient, Admin } from '../types'

export const useUserStore = defineStore('user', () => {
  const patient = ref<Patient | null>(null)
  const admin = ref<Admin | null>(null)

  const isPatientLoggedIn = computed(() => !!patient.value)
  const isAdminLoggedIn = computed(() => !!admin.value)

  function setPatient(p: Patient | null) {
    patient.value = p
    if (p) {
      localStorage.setItem('patient', JSON.stringify(p))
    } else {
      localStorage.removeItem('patient')
    }
  }

  function setAdmin(a: Admin | null) {
    admin.value = a
    if (a) {
      localStorage.setItem('admin', JSON.stringify(a))
    } else {
      localStorage.removeItem('admin')
    }
  }

  function init() {
    try {
      const savedPatient = localStorage.getItem('patient')
      const savedAdmin = localStorage.getItem('admin')
      if (savedPatient) {
        patient.value = JSON.parse(savedPatient)
      }
      if (savedAdmin) {
        admin.value = JSON.parse(savedAdmin)
      }
    } catch {
      // 数据损坏，清理并重置
      localStorage.removeItem('patient')
      localStorage.removeItem('admin')
      patient.value = null
      admin.value = null
    }
  }

  function logoutPatient() {
    setPatient(null)
  }

  function logoutAdmin() {
    setAdmin(null)
  }

  return {
    patient,
    admin,
    isPatientLoggedIn,
    isAdminLoggedIn,
    setPatient,
    setAdmin,
    init,
    logoutPatient,
    logoutAdmin
  }
})
