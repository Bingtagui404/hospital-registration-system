import request from './request'
import type { Result, Department, Doctor, Schedule, Registration, Patient, Admin, Statistics } from '../types'

// 科室 API
export const deptApi = {
  list: () => request.get<unknown, Result<Department[]>>('/dept/list'),
  getById: (id: number) => request.get<unknown, Result<Department>>(`/dept/${id}`),
  create: (data: Partial<Department>) => request.post<unknown, Result<Department>>('/dept', data),
  update: (data: Partial<Department>) => request.put<unknown, Result<Department>>('/dept', data),
  delete: (id: number) => request.delete<unknown, Result<void>>(`/dept/${id}`)
}

// 医生 API
export const doctorApi = {
  list: () => request.get<unknown, Result<Doctor[]>>('/doctor/list'),
  listByDept: (deptId: number) => request.get<unknown, Result<Doctor[]>>('/doctor/listByDept', { params: { deptId } }),
  getById: (id: number) => request.get<unknown, Result<Doctor>>(`/doctor/${id}`)
}

// 排班 API
export const scheduleApi = {
  list: (deptId?: number, workDate?: string) =>
    request.get<unknown, Result<Schedule[]>>('/schedule/list', { params: { deptId, workDate } }),
  available: (deptId: number, workDate: string) =>
    request.get<unknown, Result<Schedule[]>>('/schedule/available', { params: { deptId, workDate } }),
  getById: (id: number) => request.get<unknown, Result<Schedule>>(`/schedule/${id}`),
  create: (data: Partial<Schedule>) => request.post<unknown, Result<Schedule>>('/schedule', data),
  update: (data: Partial<Schedule>) => request.put<unknown, Result<Schedule>>('/schedule', data),
  delete: (id: number) => request.delete<unknown, Result<void>>(`/schedule/${id}`)
}

// 挂号 API
export const registrationApi = {
  create: (patientId: number, scheduleId: number) =>
    request.post<unknown, Result<Registration>>('/registration', { patientId, scheduleId }),
  cancel: (id: number) => request.put<unknown, Result<void>>(`/registration/cancel/${id}`),
  myList: (patientId: number) =>
    request.get<unknown, Result<Registration[]>>('/registration/my', { params: { patientId } }),
  list: (startDate?: string, endDate?: string, status?: string) =>
    request.get<unknown, Result<Registration[]>>('/registration/list', { params: { startDate, endDate, status } }),
  getById: (id: number) => request.get<unknown, Result<Registration>>(`/registration/${id}`),
  statistics: () => request.get<unknown, Result<Statistics>>('/registration/statistics')
}

// 患者 API
export const patientApi = {
  register: (data: Partial<Patient>) => request.post<unknown, Result<Patient>>('/patient/register', data),
  login: (phone: string, password: string) =>
    request.post<unknown, Result<Patient>>('/patient/login', { phone, password }),
  getInfo: (patientId: number) =>
    request.get<unknown, Result<Patient>>('/patient/info', { params: { patientId } }),
  updateInfo: (data: Patient) => request.put<unknown, Result<Patient>>('/patient/info', data)
}

// 管理员 API
export const adminApi = {
  login: (username: string, password: string) =>
    request.post<unknown, Result<Admin>>('/admin/login', { username, password })
}
