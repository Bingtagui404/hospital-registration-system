// 统一响应格式
export interface Result<T> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// 科室
export interface Department {
  deptId: number
  deptName: string
  description: string
  status: number
  createTime?: string
  updateTime?: string
}

// 医生
export interface Doctor {
  doctorId: number
  deptId: number
  doctorName: string
  gender: string
  title: string
  specialty: string
  phone: string
  status: number
  deptName?: string
}

// 患者
export interface Patient {
  patientId: number
  patientName: string
  idCard: string
  phone: string
  gender: string
  age: number
  address: string
  medicalHistory: string
  password?: string
}

// 排班/号源
export interface Schedule {
  scheduleId: number
  doctorId: number
  workDate: string
  timeSlot: 'AM' | 'PM'
  totalQuota: number
  remainingQuota: number
  fee: number
  status: number
  doctorName?: string
  deptName?: string
  deptId?: number
  title?: string
}

// 挂号记录
export interface Registration {
  regId: number
  regNo: string
  patientId: number
  scheduleId: number
  doctorId: number
  deptId: number
  workDate: string
  timeSlot: 'AM' | 'PM'
  queueNo: number
  fee: number
  status: 'BOOKED' | 'CANCELLED' | 'FINISHED'
  regTime: string
  patientName?: string
  doctorName?: string
  deptName?: string
  title?: string
}

// 管理员
export interface Admin {
  adminId: number
  username: string
  realName: string
  status: number
}

// 统计数据
export interface Statistics {
  bookedCount: number
  cancelledCount: number
  finishedCount: number
  totalFee: number
}
