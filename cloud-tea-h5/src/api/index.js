import request from './request'

/* ---------------- 登录 ---------------- */
export const login = (code) => request.post('/user/user/login', { code })

/* ---------------- 门店 ---------------- */
export const getStoreStatus = () => request.get('/user/store/status')

/* ---------------- 菜单 ---------------- */
export const listCategory = (type) => request.get('/user/category/list', { params: { type } })
export const listDrink = (categoryId) => request.get('/user/drink/list', { params: { categoryId } })
export const listCombo = (categoryId) => request.get('/user/combo/list', { params: { categoryId } })
export const comboDrinks = (id) => request.get(`/user/combo/drink/${id}`)

/* ---------------- 购物车 ---------------- */
export const addToCart = (data) => request.post('/user/shoppingCart/add', data)
export const subFromCart = (data) => request.post('/user/shoppingCart/sub', data)
export const listCart = () => request.get('/user/shoppingCart/list')
export const cleanCart = () => request.delete('/user/shoppingCart/clean')

/* ---------------- 订单 ---------------- */
export const submitOrder = (data) => request.post('/user/order/submit', data)
export const payOrder = (data) => request.put('/user/order/payment', data)
export const historyOrders = (params) => request.get('/user/order/historyOrders', { params })
export const orderDetail = (id) => request.get(`/user/order/orderDetail/${id}`)
export const cancelOrder = (id) => request.put(`/user/order/cancel/${id}`)
export const repeatOrder = (id) => request.post(`/user/order/repetition/${id}`)
export const remindOrder = (id) => request.get(`/user/order/reminder/${id}`)

/* ---------------- 收货地址 ---------------- */
export const listAddress = () => request.get('/user/addressBook/list')
export const getAddress = (id) => request.get(`/user/addressBook/${id}`)
export const addAddress = (data) => request.post('/user/addressBook', data)
export const updateAddress = (data) => request.put('/user/addressBook', data)
export const setDefaultAddress = (data) => request.put('/user/addressBook/default', data)
export const deleteAddress = (id) => request.delete('/user/addressBook', { params: { id } })
export const getDefaultAddress = () => request.get('/user/addressBook/default')
