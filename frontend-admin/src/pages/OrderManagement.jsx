import { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'
import { 
  Search, 
  Filter, 
  Eye, 
  Clock, 
  MapPin, 
  Phone, 
  User, 
  X, 
  CheckCircle, 
  Truck, 
  Package,
  AlertCircle,
  MoreVertical
} from 'lucide-react'

export default function OrderManagement() {
  const { getToken } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [filterStatus, setFilterStatus] = useState('ALL')
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    loadOrders()

    const handleNotification = () => {
      loadOrders()
    }

    window.addEventListener('notification_received', handleNotification)
    return () => window.removeEventListener('notification_received', handleNotification)
  }, [])

  const loadOrders = async () => {
    try {
      const token = getToken()
      const response = await axios.get('/api/orders', {
        headers: { Authorization: `Bearer ${token}` }
      })
      // Sort by newest first
      const sortedOrders = (response.data || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setOrders(sortedOrders)
    } catch (error) {
      console.log('Orders endpoint not available yet - showing empty state')
      setOrders([])
    } finally {
      setLoading(false)
    }
  }

  const updateOrderStatus = async (orderId, newStatus) => {
    const token = getToken()
    try {
      await axios.patch(`/api/orders/${orderId}/status`, 
        { status: newStatus },
        { headers: { Authorization: `Bearer ${token}` }}
      )
      loadOrders()
      setSelectedOrder(null)
    } catch (error) {
      console.error('Update status error:', error)
      const errorMsg = error.response?.data?.message || error.response?.data || error.message
      alert('Không thể cập nhật trạng thái: ' + errorMsg)
    }
  }

  const getStatusConfig = (status) => {
    const configs = {
      PENDING: { color: 'text-yellow-600 bg-yellow-50 border-yellow-200', icon: Clock, label: 'Chờ xác nhận' },
      CONFIRMED: { color: 'text-blue-600 bg-blue-50 border-blue-200', icon: CheckCircle, label: 'Đã xác nhận' },
      PREPARING: { color: 'text-purple-600 bg-purple-50 border-purple-200', icon: Package, label: 'Đang chuẩn bị' },
      DELIVERING: { color: 'text-emerald-600 bg-emerald-50 border-emerald-200', icon: Truck, label: 'Đang giao hàng' },
      COMPLETED: { color: 'text-green-600 bg-green-50 border-green-200', icon: Package, label: 'Hoàn thành' },
      CANCELLED: { color: 'text-red-600 bg-red-50 border-red-200', icon: X, label: 'Đã hủy' }
    }
    return configs[status] || { color: 'text-gray-600 bg-gray-50 border-gray-200', icon: AlertCircle, label: status }
  }

  const filteredOrders = orders.filter(order => {
    const matchesStatus = filterStatus === 'ALL' || order.status === filterStatus
    const matchesSearch = 
      order.id.toString().includes(searchTerm) || 
      order.phoneNumber?.includes(searchTerm) ||
      order.deliveryAddress?.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesStatus && matchesSearch
  })

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header & Filters */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý đơn hàng</h1>
          <p className="text-gray-500">Quản lý và theo dõi tất cả đơn đặt hàng của khách hàng</p>
        </div>
        
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <div className="relative flex-1 sm:flex-none">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <input 
              type="text" 
              placeholder="Tìm kiếm đơn hàng..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500 w-full sm:w-64"
            />
          </div>
          <button className="p-2.5 border border-gray-200 rounded-xl hover:bg-gray-50 text-gray-600">
            <Filter size={20} />
          </button>
        </div>
      </div>

      {/* Status Tabs */}
      <div className="flex overflow-x-auto pb-2 gap-2 no-scrollbar">
        {[
            { id: 'ALL', label: 'Tất cả' },
            { id: 'PENDING', label: 'Chờ xác nhận' },
            { id: 'CONFIRMED', label: 'Đã xác nhận' },
            { id: 'PREPARING', label: 'Đang chuẩn bị' },
            { id: 'DELIVERING', label: 'Đang giao' },
            { id: 'COMPLETED', label: 'Hoàn thành' },
            { id: 'CANCELLED', label: 'Đã hủy' }
        ].map(status => (
          <button
            key={status.id}
            onClick={() => setFilterStatus(status.id)}
            className={`px-4 py-2 rounded-lg font-medium whitespace-nowrap transition-all ${
              filterStatus === status.id
                ? 'bg-emerald-600 text-white shadow-md shadow-emerald-200'
                : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'
            }`}
          >
            {status.label}
          </button>
        ))}
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {filteredOrders.length === 0 ? (
          <div className="text-center py-20">
            <div className="bg-gray-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <Package size={40} className="text-gray-400" />
            </div>
            <h3 className="text-lg font-bold text-gray-900">Không tìm thấy đơn hàng</h3>
            <p className="text-gray-500">Thử điều chỉnh bộ lọc hoặc tìm kiếm</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50/50 border-b border-gray-100">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Mã đơn</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Khách hàng</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Tổng tiền</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Ngày đặt</th>
                  <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredOrders.map((order) => {
                  const statusConfig = getStatusConfig(order.status)
                  return (
                    <tr key={order.id} className="hover:bg-gray-50/50 transition-colors group">
                      <td className="px-6 py-4">
                        <span className="font-bold text-gray-900">#{order.id}</span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex flex-col">
                          <span className="font-medium text-gray-900">{order.phoneNumber}</span>
                          <span className="text-xs text-gray-500 truncate max-w-[200px]">{order.deliveryAddress}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className="font-bold text-emerald-600">
                          {order.totalAmount?.toLocaleString('vi-VN')}đ
                        </span>
                        <span className="text-xs text-gray-500 block">{order.items?.length} sản phẩm</span>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${statusConfig.color}`}>
                          <statusConfig.icon size={12} />
                          {statusConfig.label}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {new Date(order.createdAt).toLocaleString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={() => setSelectedOrder(order)}
                          className="p-2 text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-all"
                          title="Xem chi tiết"
                        >
                          <Eye size={18} />
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Order Details Modal */}
      {selectedOrder && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-2xl max-w-3xl w-full max-h-[90vh] overflow-hidden flex flex-col animate-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
              <div>
                <div className="flex items-center gap-3 mb-1">
                  <h2 className="text-2xl font-bold text-gray-900">Đơn hàng #{selectedOrder.id}</h2>
                  {(() => {
                    const config = getStatusConfig(selectedOrder.status)
                    return (
                      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${config.color} flex items-center gap-1`}>
                        <config.icon size={14} /> {config.label}
                      </span>
                    )
                  })()}
                </div>
                <p className="text-gray-500 text-sm flex items-center gap-1">
                  <Clock size={14} /> Đặt lúc {new Date(selectedOrder.createdAt).toLocaleString('vi-VN')}
                </p>
              </div>
              <button 
                onClick={() => setSelectedOrder(null)}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
              >
                <X size={24} />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 overflow-y-auto flex-1 custom-scrollbar">
              <div className="grid md:grid-cols-2 gap-8 mb-8">
                {/* Customer Info */}
                <div className="space-y-4">
                  <h3 className="font-bold text-gray-900 flex items-center gap-2">
                    <User size={18} className="text-emerald-600" /> Thông tin khách hàng
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-xl space-y-3 border border-gray-100">
                    <div className="flex items-start gap-3">
                      <Phone size={18} className="text-gray-400 mt-0.5" />
                      <div>
                        <p className="text-xs font-bold text-gray-500 uppercase">Số điện thoại</p>
                        <p className="font-medium text-gray-900">{selectedOrder.phoneNumber}</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3">
                      <MapPin size={18} className="text-gray-400 mt-0.5" />
                      <div>
                        <p className="text-xs font-bold text-gray-500 uppercase">Địa chỉ giao hàng</p>
                        <p className="font-medium text-gray-900">{selectedOrder.deliveryAddress}</p>
                      </div>
                    </div>
                    {selectedOrder.notes && (
                      <div className="flex items-start gap-3">
                        <span className="text-gray-400 mt-0.5">📝</span>
                        <div>
                          <p className="text-xs font-bold text-gray-500 uppercase">Ghi chú</p>
                          <p className="font-medium text-gray-900">{selectedOrder.notes}</p>
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                {/* Order Summary */}
                <div className="space-y-4">
                  <h3 className="font-bold text-gray-900 flex items-center gap-2">
                    <Package size={18} className="text-emerald-600" /> Chi tiết đơn hàng
                  </h3>
                  <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                    <div className="space-y-3 mb-4 max-h-48 overflow-y-auto custom-scrollbar pr-2">
                      {selectedOrder.items?.map((item, index) => (
                        <div key={index} className="flex justify-between items-center text-sm bg-white p-2 rounded-lg">
                          <div className="flex items-center gap-3">
                            <div className="w-12 h-12 rounded-lg overflow-hidden bg-gray-100 shrink-0">
                              <img 
                                src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=100&q=80'} 
                                alt={item.menuItemName}
                                className="w-full h-full object-cover"
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.src = 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=100&q=80';
                                }}
                              />
                            </div>
                            <div>
                              <span className="font-medium text-gray-700 block">{item.menuItemName}</span>
                              <span className="text-xs text-gray-500">x{item.quantity} • {item.price?.toLocaleString('vi-VN')}đ/sản phẩm</span>
                            </div>
                          </div>
                          <span className="font-bold text-gray-900">
                            {(item.price * item.quantity).toLocaleString('vi-VN')}đ
                          </span>
                        </div>
                      ))}
                    </div>
                    <div className="border-t border-gray-200 pt-3 flex justify-between items-center">
                      <span className="font-bold text-gray-900">Tổng cộng</span>
                      <span className="text-xl font-bold text-emerald-600">
                        {selectedOrder.totalAmount?.toLocaleString('vi-VN')}đ
                      </span>
                    </div>
                  </div>
                  
                  {/* Payment Info */}
                  <div className="flex gap-3 mt-3">
                    <span className={`px-3 py-1 rounded-lg text-xs font-bold ${
                      selectedOrder.paymentMethod === 'SEPAY' 
                        ? 'bg-blue-100 text-blue-700' 
                        : 'bg-green-100 text-green-700'
                    }`}>
                      {selectedOrder.paymentMethod === 'SEPAY' ? '🏦 Chuyển khoản' : '💵 COD'}
                    </span>
                    <span className={`px-3 py-1 rounded-lg text-xs font-bold ${
                      selectedOrder.paymentStatus === 'SUCCESS' 
                        ? 'bg-green-100 text-green-700'
                        : selectedOrder.paymentStatus === 'PENDING_VERIFICATION'
                        ? 'bg-yellow-100 text-yellow-700'
                        : 'bg-gray-100 text-gray-700'
                    }`}>
                      {selectedOrder.paymentStatus === 'SUCCESS' ? '✓ Đã thanh toán' 
                        : selectedOrder.paymentStatus === 'PENDING_VERIFICATION' ? 'Đang xác nhận'
                        : 'Chưa thanh toán'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div>
                <h3 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
                  <CheckCircle size={18} className="text-emerald-600" /> Cập nhật trạng thái
                </h3>
                
                {/* Warning for unpaid SEPAY orders */}
                {selectedOrder.paymentMethod === 'SEPAY' && selectedOrder.paymentStatus !== 'SUCCESS' && (
                  <div className="bg-yellow-50 border border-yellow-300 rounded-xl p-3 mb-4">
                    <p className="text-sm text-yellow-800 flex items-center gap-2">
                      ⚠️ <strong>Đơn chuyển khoản chưa thanh toán.</strong> Chỉ có thể chuyển sang "Đang chuẩn bị" sau khi khách thanh toán.
                    </p>
                  </div>
                )}
                
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3">
                  {[
                    { status: 'CONFIRMED', label: 'Xác nhận đơn', color: 'bg-blue-600 hover:bg-blue-700', allowedFrom: ['PENDING'], requiresPayment: false },
                    { status: 'PREPARING', label: 'Chuẩn bị hàng', color: 'bg-purple-600 hover:bg-purple-700', allowedFrom: ['CONFIRMED'], requiresPayment: true },
                    { status: 'DELIVERING', label: 'Giao hàng', color: 'bg-emerald-600 hover:bg-emerald-700', allowedFrom: ['PREPARING'], requiresPayment: true },
                    { status: 'COMPLETED', label: 'Hoàn thành', color: 'bg-green-600 hover:bg-green-700', allowedFrom: ['DELIVERING'], requiresPayment: true },
                    { status: 'CANCELLED', label: 'Hủy đơn', color: 'bg-red-600 hover:bg-red-700', allowedFrom: ['PENDING', 'CONFIRMED', 'PREPARING'], requiresPayment: false }
                  ].map((action) => {
                    const isAllowed = action.allowedFrom.includes(selectedOrder.status);
                    const isCurrent = selectedOrder.status === action.status;
                    // SEPAY orders must be paid before PREPARING+
                    const isBlockedByPayment = action.requiresPayment && 
                      selectedOrder.paymentMethod === 'SEPAY' && 
                      selectedOrder.paymentStatus !== 'SUCCESS';
                    
                    return (
                      <button
                        key={action.status}
                        onClick={() => updateOrderStatus(selectedOrder.id, action.status)}
                        disabled={!isAllowed || isCurrent || isBlockedByPayment}
                        className={`py-3 px-2 rounded-xl font-semibold text-white text-sm transition-all shadow-sm ${
                          !isAllowed || isCurrent || isBlockedByPayment
                            ? 'bg-gray-300 cursor-not-allowed opacity-50' 
                            : `${action.color} hover:shadow-md transform hover:-translate-y-0.5`
                        }`}
                      >
                        {action.label}
                      </button>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
