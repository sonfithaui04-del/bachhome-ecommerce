import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/apiClient'
import { Package, Truck, CheckCircle, Clock, MapPin, Phone, User as UserIcon, RefreshCcw, Search, MessageSquare } from 'lucide-react'
import toast from 'react-hot-toast'
import ChatBox from '../components/ChatBox'
import { OrderSkeleton, CardSkeleton } from '../components/Skeleton'

export default function ShipperDashboard() {
  const { user } = useAuth()
  const [readyOrders, setReadyOrders] = useState([])
  const [currentOrders, setCurrentOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('available') // 'available' or 'current'
  const [activeChatOrderId, setActiveChatOrderId] = useState(null)

  const fetchData = async () => {
    setLoading(true)
    try {
      // Fetch orders READY for pickup
      const readyRes = await api.get('/api/orders/shipper/ready')
      setReadyOrders(readyRes.data)

      // Fetch current orders for this shipper
      if (user?.userId) {
        const currentRes = await api.get(`/api/orders/shipper/current/${user.userId}`)
        setCurrentOrders(currentRes.data)
      }
    } catch (error) {
      console.error('Failed to fetch shipper data', error)
      toast.error('Không thể tải dữ liệu đơn hàng')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
    // Polling for new orders every 30 seconds
    const interval = setInterval(fetchData, 30000)
    return () => clearInterval(interval)
  }, [user])

  const handlePickUp = async (orderId) => {
    try {
      await api.put(`/api/orders/${orderId}/pickup?shipperId=${user.userId}`)
      toast.success('Đã nhận đơn hàng! Chúc bạn giao hàng an toàn 🛵')
      fetchData()
      setActiveTab('current')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Lỗi khi nhận đơn')
    }
  }

  const handleDeliver = async (orderId) => {
    try {
      await api.put(`/api/orders/${orderId}/deliver`)
      toast.success('Tuyệt vời! Đơn hàng đã được giao thành công 🎉')
      fetchData()
    } catch (error) {
      toast.error('Lỗi khi cập nhật trạng thái giao hàng')
    }
  }

  if (user?.role !== 'SHIPPER') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 pt-20">
        <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md">
          <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center text-red-500 mx-auto mb-6">
            <UserIcon size={40} />
          </div>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Truy cập bị từ chối</h2>
          <p className="text-gray-500 mb-6">Chỉ những tài khoản có quyền Shipper mới có thể truy cập trang này.</p>
          <button onClick={() => window.location.href = '/'} className="px-6 py-3 bg-gray-800 text-white rounded-xl font-bold">Quay lại trang chủ</button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 pt-28 pb-12">
      <div className="container mx-auto px-4 max-w-6xl">
        {/* Header Section */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-1">Bảng điều khiển Shipper</h1>
            <p className="text-gray-500">Chào mừng trở lại, {user.fullName || user.email}!</p>
          </div>
          <button 
            onClick={fetchData}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-xl text-gray-600 hover:bg-gray-50 transition-colors shadow-sm"
          >
            <RefreshCcw size={18} className={loading ? 'animate-spin' : ''} />
            Làm mới
          </button>
        </div>

        {/* Info Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {loading ? (
            <>
              <CardSkeleton />
              <CardSkeleton />
              <CardSkeleton />
            </>
          ) : (
            <>
              <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex items-center gap-4">
                <div className="w-14 h-14 bg-emerald-100 rounded-2xl flex items-center justify-center text-emerald-500">
                  <Package size={28} />
                </div>
                <div>
                  <p className="text-sm text-gray-500 font-medium">Đơn sẵn sàng</p>
                  <p className="text-2xl font-bold text-gray-900">{readyOrders.length}</p>
                </div>
              </div>
              <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex items-center gap-4">
                <div className="w-14 h-14 bg-blue-100 rounded-2xl flex items-center justify-center text-blue-500">
                  <Truck size={28} />
                </div>
                <div>
                  <p className="text-sm text-gray-500 font-medium">Đang giao</p>
                  <p className="text-2xl font-bold text-gray-900">{currentOrders.length}</p>
                </div>
              </div>
              <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex items-center gap-4">
                <div className="w-14 h-14 bg-green-100 rounded-2xl flex items-center justify-center text-green-500">
                  <CheckCircle size={28} />
                </div>
                <div>
                  <p className="text-sm text-gray-500 font-medium">Hoàn thành hôm nay</p>
                  <p className="text-2xl font-bold text-gray-900">0</p>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Tabs */}
        <div className="flex gap-4 p-1 bg-gray-200/50 rounded-2xl mb-6 w-fit">
          <button 
            onClick={() => setActiveTab('available')}
            className={`px-6 py-2.5 rounded-xl font-bold transition-all ${activeTab === 'available' ? 'bg-white text-emerald-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
          >
            Đơn hàng mới ({readyOrders.length})
          </button>
          <button 
            onClick={() => setActiveTab('current')}
            className={`px-6 py-2.5 rounded-xl font-bold transition-all ${activeTab === 'current' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}
          >
            Đang giao của tôi ({currentOrders.length})
          </button>
        </div>

        {/* Orders Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {loading ? (
            <>
              <OrderSkeleton />
              <OrderSkeleton />
              <OrderSkeleton />
              <OrderSkeleton />
            </>
          ) : activeTab === 'available' ? (
            readyOrders.length > 0 ? (
              readyOrders.map(order => (
                <OrderCard key={order.id} order={order} type="ready" onAction={handlePickUp} />
              ))
            ) : (
                <EmptyState icon={<RefreshCcw size={48} />} title="Không có đơn hàng nào mới" subtitle="Hãy thử tải lại trang hoặc chờ ít phút nhé." />
            )
          ) : (
            currentOrders.length > 0 ? (
              currentOrders.map(order => (
                <OrderCard key={order.id} order={order} type="current" onAction={handleDeliver} onChat={() => setActiveChatOrderId(order.id)} />
              ))
            ) : (
                <EmptyState icon={<Truck size={48} />} title="Bạn chưa nhận đơn nào" subtitle="Chuyển sang tab Đơn hàng mới để nhận chuyến giao đầu tiên!" />
            )
          )}
        </div>
      </div>

      {/* Global Chat Box */}
      {activeChatOrderId && (
        <ChatBox 
          orderId={activeChatOrderId} 
          currentUser={user} 
          senderName="Shipper" 
          onClose={() => setActiveChatOrderId(null)}
        />
      )}
    </div>
  )
}

function OrderCard({ order, type, onAction, onChat }) {
  return (
    <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
      <div className="p-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <span className="text-xs font-bold text-emerald-500 bg-emerald-50 px-3 py-1 rounded-full uppercase tracking-wider mb-2 inline-block">Order #{order.id}</span>
            <h3 className="text-lg font-bold text-gray-900">{order.customerName || 'Khách hàng ẩn danh'}</h3>
          </div>
          <div className="text-right">
             <p className="text-xl font-bold text-emerald-500">{order.totalAmount?.toLocaleString('vi-VN')}đ</p>
             <p className="text-xs text-gray-400 capitalize">{order.paymentMethod}</p>
          </div>
        </div>

        <div className="space-y-3 mb-6">
          <div className="flex items-start gap-3 text-gray-600">
            <MapPin size={18} className="text-gray-400 mt-0.5" />
            <p className="text-sm">{order.deliveryAddress}</p>
          </div>
          <div className="flex items-center gap-3 text-gray-600">
            <Phone size={18} className="text-gray-400" />
            <p className="text-sm">{order.phoneNumber}</p>
          </div>
          <div className="flex items-center gap-3 text-gray-600">
            <Clock size={18} className="text-gray-400" />
            <p className="text-sm">{new Date(order.updatedAt || order.createdAt).toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'})} - Hôm nay</p>
          </div>
        </div>

        <div className="border-t border-gray-50 pt-4 flex flex-col gap-3">
           <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-gray-500">Số sản phẩm: {order.items?.length || 0}</span>
              {type === 'current' && (
                <button 
                  onClick={(e) => { e.stopPropagation(); onChat(); }}
                  className="text-xs text-emerald-500 font-bold flex items-center gap-1 hover:underline"
                >
                  <MessageSquare size={14} /> Nhắn tin khách
                </button>
              )}
           </div>
           
           <button 
              onClick={() => onAction(order.id)}
              className={`w-full py-3.5 rounded-2xl font-bold text-white transition-all transform hover:-translate-y-1 shadow-lg ${type === 'ready' ? 'bg-emerald-500 hover:bg-emerald-600 shadow-emerald-200' : 'bg-green-500 hover:bg-green-600 shadow-green-200'}`}
           >
             {type === 'ready' ? 'Bắt đầu giao hàng' : 'Xác nhận đã giao'}
           </button>
        </div>
      </div>
    </div>
  )
}

function EmptyState({ icon, title, subtitle }) {
    return (
        <div className="col-span-full py-20 text-center">
            <div className="text-gray-200 mb-4 flex justify-center">{icon}</div>
            <h3 className="text-xl font-bold text-gray-800 mb-2">{title}</h3>
            <p className="text-gray-500">{subtitle}</p>
        </div>
    )
}
