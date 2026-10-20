import request from './request'

/* ---------------- 登录 / 员工 ---------------- */
export const login = (data) => request.post('/admin/employee/login', data)
export const logout = () => request.post('/admin/employee/logout')

export const pageEmployee = (params) => request.get('/admin/employee/page', { params })
export const getEmployee = (id) => request.get(`/admin/employee/${id}`)
export const addEmployee = (data) => request.post('/admin/employee', data)
export const updateEmployee = (data) => request.put('/admin/employee', data)
export const setEmployeeStatus = (status, id) =>
  request.post(`/admin/employee/status/${status}`, null, { params: { id } })

/* ---------------- 饮品分类 ---------------- */
export const pageCategory = (params) => request.get('/admin/category/page', { params })
export const listCategory = (type) => request.get('/admin/category/list', { params: { type } })
export const addCategory = (data) => request.post('/admin/category', data)
export const updateCategory = (data) => request.put('/admin/category', data)
export const deleteCategory = (ids) => request.delete('/admin/category', { params: { ids } })
export const setCategoryStatus = (status, id) =>
  request.post(`/admin/category/status/${status}`, null, { params: { id } })

/* ---------------- 饮品 ---------------- */
export const pageDrink = (params) => request.get('/admin/drink/page', { params })
export const getDrink = (id) => request.get(`/admin/drink/${id}`)
export const listDrink = (categoryId) => request.get('/admin/drink/list', { params: { categoryId } })
export const addDrink = (data) => request.post('/admin/drink', data)
export const updateDrink = (data) => request.put('/admin/drink', data)
export const deleteDrink = (ids) => request.delete('/admin/drink', { params: { ids } })
export const setDrinkStatus = (status, id) =>
  request.post(`/admin/drink/status/${status}`, null, { params: { id } })

/* ---------------- 组合套餐 ---------------- */
export const pageCombo = (params) => request.get('/admin/combo/page', { params })
export const getCombo = (id) => request.get(`/admin/combo/${id}`)
export const addCombo = (data) => request.post('/admin/combo', data)
export const updateCombo = (data) => request.put('/admin/combo', data)
export const deleteCombo = (ids) => request.delete('/admin/combo', { params: { ids } })
// 注意：后端该接口写成 @PostMapping("status/{status}")（少个前导斜杠），
// Spring 解析出来的实际路径就是 /admin/combo/status/{status}
export const setComboStatus = (status, id) =>
  request.post(`/admin/combo/status/${status}`, null, { params: { id } })

/* ---------------- 订单 ---------------- */
export const searchOrders = (params) => request.get('/admin/order/conditionSearch', { params })
export const orderStatistics = () => request.get('/admin/order/statistics')
export const orderDetails = (id) => request.get(`/admin/order/details/${id}`)
export const confirmOrder = (data) => request.put('/admin/order/confirm', data)
export const rejectOrder = (data) => request.put('/admin/order/rejection', data)
export const cancelOrder = (data) => request.put('/admin/order/cancel', data)
export const deliveryOrder = (id) => request.put(`/admin/order/delivery/${id}`)
export const completeOrder = (id) => request.put(`/admin/order/complete/${id}`)

/* ---------------- 统计报表 ---------------- */
export const turnoverStatistics = (params) =>
  request.get('/admin/report/turnoverStatistics', { params })
export const userStatistics = (params) => request.get('/admin/report/userStatistics', { params })
export const ordersStatistics = (params) => request.get('/admin/report/ordersStatistics', { params })
export const salesTop10 = (params) => request.get('/admin/report/top10', { params })
export const exportReport = () =>
  request.get('/admin/report/export', { responseType: 'blob' })

/* ---------------- 工作台 ---------------- */
export const businessData = () => request.get('/admin/workspace/businessData')
export const overviewOrders = () => request.get('/admin/workspace/overviewOrders')
export const overviewDrinks = () => request.get('/admin/workspace/overviewDrinks')
export const overviewCombos = () => request.get('/admin/workspace/overviewCombos')

/* ---------------- 门店状态 ---------------- */
export const getStoreStatus = () => request.get('/admin/store/status')
export const setStoreStatus = (status) => request.put(`/admin/store/${status}`)

/* ---------------- 文件上传 ---------------- */
export const uploadFile = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/admin/common/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
