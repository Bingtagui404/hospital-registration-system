import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '../types'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data as Result<unknown>
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return response.data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
