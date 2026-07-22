import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { orderService } from '../services/orderService'

import { Package, Clock, MapPin, Phone, ChevronRight, ShoppingBag, CheckCircle, Truck, XCircle, AlertCircle, QrCode, X, Copy, Check, MessageSquare } from 'lucide-react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import toast from 'react-hot-toast'
import ChatBox from '../components/ChatBox'
import { OrderSkeleton } from '../components/Skeleton'

export default function MyOrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { user, getToken } = useAuth()
  const [showQRModal, setShowQRModal] = useState(false)
  const [qrPaymentInfo, setQrPaymentInfo] = useState(null)
  const [copied, setCopied] = useState(false)
  const [paymentSuccess, setPaymentSuccess] = useState(false)
  const [paidOrderId, setPaidOrderId] = useState(null)
  const [cancelOrderId, setCancelOrderId] = useState(null)
  const [activeChatOrderId, setActiveChatOrderId] = useState(null)

  useEffect(() => {
    if (user) {
      loadOrders()
    }

    const handleNotification = (event) => {
      const notification = event.detail
      
      // Xử lý thông báo thanh toán thành công
      if (notification?.type === 'PAYMENT_SUCCESS') {
        if (showQRModal && qrPaymentInfo?.orderId === notification.orderId) {
          setShowQRModal(false)
          setQrPaymentInfo(null)
          setPaidOrderId(notification.orderId)
          setPaymentSuccess(true)
        } else {
          setPaidOrderId(notification.orderId)
          setPaymentSuccess(true)
        }
      }
      
      // Reload danh sách đơn hàng cho mọi thông báo (bao gồm ORDER_STATUS_CHANGED)
      if (notification?.type === 'ORDER_STATUS_CHANGED') {
        const labels = {
          PENDING: 'Chờ xác nhận',
          CONFIRMED: 'Đã xác nhận',
          PREPARING: 'Đang chuẩn bị',
          DELIVERING: 'Đang giao hàng',
          COMPLETED: 'Hoàn thành',
          DELIVERED: 'Đã giao',
          CANCELLED: 'Đã hủy'
        }
        const statusLabel = labels[notification.status] || notification.status
        toast.success(`Đơn hàng #${notification.orderId}: ${statusLabel}`, {
          icon: '🔔',
          duration: 4000
        })
      }
      
      loadOrders()
    }

    window.addEventListener('notification_received', handleNotification)
    return () => window.removeEventListener('notification_received', handleNotification)
  }, [user, showQRModal, qrPaymentInfo])

  const loadOrders = async () => {
    try {
      const data = await orderService.getMyOrders(user.userId || user.id)
      setOrders(data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)))
    } catch (err) {
      setError('Không thể tải danh sách đơn hàng. Vui lòng thử lại.')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmReceipt = async (orderId) => {
    if (window.confirm('Bạn xác nhận đã nhận được hàng và muốn hoàn tất đơn hàng?')) {
      try {
        await orderService.confirmReceipt(orderId)
        toast.success('Đã xác nhận nhận hàng thành công!')
        loadOrders()
      } catch (err) {
        toast.error('Có lỗi xảy ra: ' + (err.response?.data?.message || err.message))
      }
    }
  }

  const getStatusConfig = (status) => {
    const configs = {
      PENDING: { color: 'text-yellow-600 bg-yellow-50 border-yellow-200', icon: Clock, label: 'Chờ xác nhận' },
      CONFIRMED: { color: 'text-blue-600 bg-blue-50 border-blue-200', icon: CheckCircle, label: 'Đã xác nhận' },
      PREPARING: { color: 'text-purple-600 bg-purple-50 border-purple-200', icon: Package, label: 'Đang chuẩn bị' },
      DELIVERING: { color: 'text-indigo-600 bg-indigo-50 border-indigo-200', icon: Truck, label: 'Đang giao hàng' },
      COMPLETED: { color: 'text-green-600 bg-green-50 border-green-200', icon: Package, label: 'Hoàn thành' }, 
      DELIVERED: { color: 'text-green-600 bg-green-50 border-green-200', icon: Package, label: 'Đã giao' },
      CANCELLED: { color: 'text-red-600 bg-red-50 border-red-200', icon: XCircle, label: 'Đã hủy' }
    }
    if (status === 'COMPLETED') return { color: 'text-green-600 bg-green-50 border-green-200', icon: CheckCircle, label: 'Hoàn thành' };

    return configs[status] || { color: 'text-gray-600 bg-gray-50 border-gray-200', icon: AlertCircle, label: status }
  }

  const handlePayNow = async (order) => {
    try {
      const token = getToken()
      const response = await axios.post('/api/payments/sepay/init', {
        orderId: order.id,
        userId: user?.userId || user?.id,
        amount: order.totalAmount
      }, {
        headers: { Authorization: `Bearer ${token}` }
      })
      setQrPaymentInfo(response.data)
      setShowQRModal(true)
    } catch (err) {
      toast.error('Không thể tạo mã thanh toán: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleCopyAccount = () => {
    navigator.clipboard.writeText(qrPaymentInfo?.accountNumber || '')
    setCopied(true)
    toast.success('Đã copy số tài khoản!')
    setTimeout(() => setCopied(false), 2000)
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // Handle Cancel Click (Show Modal)
  const handleCancelClick = (orderId) => {
    setCancelOrderId(orderId)
  }

  // Confirm Cancel (Call API)
  const confirmCancelOrder = async () => {
    if (!cancelOrderId) return
    try {
      await orderService.cancelOrder(cancelOrderId)
      toast.success('Đã hủy đơn hàng thành công')
      loadOrders()
    } catch (err) {
      toast.error('Không thể hủy đơn: ' + (err.response?.data?.message || err.message))
    } finally {
      setCancelOrderId(null)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      
      <div className="container mx-auto px-4 pt-32 pb-20">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Đơn hàng của tôi</h1>
          <p className="text-gray-500 mb-8">Theo dõi và quản lý các đơn hàng gần đây</p>

          {loading ? (
            <div className="space-y-6">
              {[1, 2, 3].map(i => (
                <OrderSkeleton key={i} />
              ))}
            </div>
          ) : error ? (
            <div className="text-center py-12 bg-white rounded-3xl shadow-sm border border-red-100">
              <div className="text-red-500 mb-4 flex justify-center"><AlertCircle size={48} /></div>
              <p className="text-gray-800 font-medium">{error}</p>
              <button onClick={loadOrders} className="mt-4 text-emerald-500 font-bold hover:underline">Thử lại</button>
            </div>
          ) : orders.length === 0 ? (
            <div className="text-center py-20 bg-white rounded-3xl shadow-sm border border-gray-100">
              <div className="bg-emerald-50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-6 text-emerald-500">
                <ShoppingBag size={48} />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Chưa có đơn hàng nào</h2>
              <p className="text-gray-500 mb-8">Có vẻ như bạn chưa đặt sản phẩm nào.</p>
              <Link 
                to="/menu" 
                className="inline-flex items-center justify-center gap-2 px-8 py-3 bg-emerald-500 text-white rounded-xl font-bold hover:bg-emerald-600 transition-all shadow-lg shadow-emerald-500/30"
              >
                Mua ngay <ChevronRight size={20} />
              </Link>
            </div>
          ) : (
            <div className="space-y-6">
              {orders.map((order) => {
                const statusConfig = getStatusConfig(order.status)
                const StatusIcon = statusConfig.icon

                return (
                  <div key={order.id} className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-all duration-300 group">
                    {/* Header */}
                    <div className="p-6 border-b border-gray-50 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                      <div className="flex items-center gap-4">
                        <div className={`p-3 rounded-xl ${statusConfig.color.replace('text-', 'bg-').replace('bg-', 'bg-opacity-10 ')}`}>
                          <StatusIcon size={24} className={statusConfig.color.split(' ')[0]} />
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <h3 className="font-bold text-gray-900">Đơn hàng #{order.id}</h3>
                            <span className={`px-3 py-1 rounded-full text-xs font-bold border ${statusConfig.color}`}>
                              {statusConfig.label}
                            </span>
                          </div>
                          <p className="text-sm text-gray-500 mt-1 flex items-center gap-1">
                            <Clock size={14} /> {formatDate(order.createdAt)}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-gray-500">Tổng tiền</p>
                        <p className="text-xl font-bold text-emerald-500">{order.totalAmount?.toLocaleString('vi-VN')}đ</p>
                      </div>
                    </div>

                    {/* Body */}
                    <div className="p-6">
                      <div className="space-y-4 mb-6">
                        {order.items?.map((item, index) => (
                          <div key={index} className="flex justify-between items-center py-2 border-b border-dashed border-gray-100 last:border-0">
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
                                <span className="font-medium text-gray-700">{item.menuItemName}</span>
                                <p className="text-sm text-gray-400">x{item.quantity}</p>
                              </div>
                            </div>
                            <span className="font-bold text-gray-900">
                              {(item.price * item.quantity)?.toLocaleString('vi-VN')}đ
                            </span>
                          </div>
                        ))}
                      </div>

                      <div className="flex flex-col sm:flex-row gap-6 pt-4 bg-gray-50/50 rounded-2xl p-4">
                        <div className="flex-1 flex items-start gap-3">
                          <MapPin className="text-gray-400 mt-1" size={20} />
                          <div>
                            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Địa chỉ giao hàng</p>
                            <p className="text-gray-800 font-medium text-sm">{order.deliveryAddress}</p>
                          </div>
                        </div>
                        <div className="flex-1 flex items-start gap-3">
                          <Phone className="text-gray-400 mt-1" size={20} />
                          <div>
                            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Số điện thoại</p>
                            <p className="text-gray-800 font-medium text-sm">{order.phoneNumber}</p>
                          </div>
                        </div>
                        {order.notes && (
                          <div className="flex-1 flex items-start gap-3">
                            <span className="text-gray-400 mt-1">📝</span>
                            <div>
                              <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Ghi chú</p>
                              <p className="text-gray-800 font-medium text-sm">{order.notes}</p>
                            </div>
                          </div>
                        )}
                      </div>

                      {/* Payment Info */}
                      <div className="flex flex-wrap gap-4 mt-4 pt-4 border-t border-gray-100 items-center">
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-bold text-gray-500 uppercase">Phương thức:</span>
                          <span className={`px-2 py-1 rounded-lg text-xs font-bold ${
                            order.paymentMethod === 'SEPAY' 
                              ? 'bg-blue-100 text-blue-700' 
                              : 'bg-green-100 text-green-700'
                          }`}>
                            {order.paymentMethod === 'SEPAY' ? '🏦 Chuyển khoản' : '💵 COD'}
                          </span>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-bold text-gray-500 uppercase">Thanh toán:</span>
                          <span className={`px-2 py-1 rounded-lg text-xs font-bold ${
                            order.paymentStatus === 'SUCCESS' 
                              ? 'bg-green-100 text-green-700'
                              : order.paymentStatus === 'PENDING_VERIFICATION'
                              ? 'bg-yellow-100 text-yellow-700'
                              : 'bg-gray-100 text-gray-700'
                          }`}>
                            {order.paymentStatus === 'SUCCESS' ? '✓ Đã thanh toán' 
                              : order.paymentStatus === 'PENDING_VERIFICATION' ? 'Đang xác nhận'
                              : 'Chưa thanh toán'}
                          </span>
                        </div>
                        
                        {/* Action Buttons */}
                        <div className="flex flex-wrap gap-3 ml-auto">
                          {/* Chat Button - Show for all active orders */}
                          {['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERING'].includes(order.status) && (
                            <button
                              onClick={() => setActiveChatOrderId(order.id)}
                              className="px-4 py-2 bg-white text-gray-700 border border-gray-200 rounded-lg text-xs font-bold hover:bg-gray-50 transition-all flex items-center gap-2"
                            >
                              <MessageSquare size={16} className="text-emerald-500" /> Nhắn tin hỗ trợ
                            </button>
                          )}

                          {/* Cancel Button - PENDING or CONFIRMED */}
                          {['PENDING', 'CONFIRMED'].includes(order.status) && (
                            <button
                              onClick={() => handleCancelClick(order.id)}
                              className="px-4 py-2 bg-red-50 text-red-600 border border-red-200 rounded-lg text-xs font-bold hover:bg-red-100 transition-all flex items-center gap-2"
                            >
                              <XCircle size={16} /> Hủy đơn
                            </button>
                          )}

                          {/* NÚT XÁC NHẬN ĐÃ NHẬN HÀNG - Chỉ hiển thị khi trạng thái là DELIVERING */}
                          {order.status === 'DELIVERING' && (
                            <button
                              onClick={() => handleConfirmReceipt(order.id)}
                              className="px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg text-xs font-bold hover:shadow-lg transition-all flex items-center gap-2 animate-bounce-subtle"
                            >
                              <CheckCircle size={16} /> Đã nhận được hàng
                            </button>
                          )}

                          {/* Pay Now button for unpaid SEPAY orders */}
                          {order.paymentMethod === 'SEPAY' && order.paymentStatus !== 'SUCCESS' && order.status !== 'CANCELLED' && order.status !== 'COMPLETED' && (
                            <button
                              onClick={() => handlePayNow(order)}
                              className="px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-600 text-white rounded-lg text-xs font-bold hover:shadow-lg transition-all flex items-center gap-2"
                            >
                              💳 Thanh toán ngay
                            </button>
                          )}
                        </div>
                      </div>
                      
                      {/* Warning for unpaid SEPAY */}
                      {order.paymentMethod === 'SEPAY' && order.paymentStatus !== 'SUCCESS' && order.status !== 'CANCELLED' && (
                        <div className="mt-3 bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                          <p className="text-xs text-yellow-800">
                            ⚠️ <strong>Chưa thanh toán:</strong> Đơn hàng chuyển khoản cần được thanh toán để được xử lý.
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>

      {/* Global Chat Box */}
      {activeChatOrderId && (
        <ChatBox 
          orderId={activeChatOrderId} 
          currentUser={user} 
          senderName={user.fullName || user.email}
          onClose={() => setActiveChatOrderId(null)}
        />
      )}
      
      {/* Cancel Confirmation Modal */}
      {cancelOrderId && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-3xl max-w-sm w-full p-6 text-center animate-in fade-in zoom-in duration-300">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <AlertCircle size={32} className="text-red-500" />
            </div>
            
            <h3 className="text-xl font-bold text-gray-900 mb-2">Hủy đơn hàng?</h3>
            <p className="text-gray-500 mb-6">
              Bạn có chắc chắn muốn hủy đơn hàng #{cancelOrderId}? 
              <br/>Hành động này không thể hoàn tác.
            </p>
            
            <div className="flex gap-3">
              <button
                onClick={() => setCancelOrderId(null)}
                className="flex-1 py-3 text-gray-700 font-bold bg-gray-100 hover:bg-gray-200 rounded-xl transition-all"
              >
                Không
              </button>
              <button
                onClick={confirmCancelOrder}
                className="flex-1 py-3 text-white font-bold bg-red-500 hover:bg-red-600 rounded-xl transition-all shadow-lg shadow-red-500/30"
              >
                Hủy đơn
              </button>
            </div>
          </div>
        </div>
      )}

      {/* QR Payment Modal */}
      {showQRModal && qrPaymentInfo && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 relative animate-in fade-in zoom-in duration-300">
            <button 
              onClick={() => setShowQRModal(false)}
              className="absolute top-4 right-4 p-2 hover:bg-gray-100 rounded-full transition-colors"
            >
              <X size={24} />
            </button>
            
            <div className="text-center mb-4">
              <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                <QrCode size={24} className="text-blue-500" />
              </div>
              <h3 className="text-xl font-bold text-gray-900">Quét mã để thanh toán</h3>
              <p className="text-gray-500 text-sm">Đơn hàng #{qrPaymentInfo.orderId}</p>
            </div>
            
            <div className="bg-gray-50 rounded-2xl p-4 mb-4">
              <img 
                src={qrPaymentInfo.qrCodeUrl} 
                alt="QR Payment" 
                className="w-full max-w-[200px] mx-auto rounded-xl shadow-lg"
              />
            </div>
            
            <div className="space-y-2 text-sm mb-4">
              <div className="flex justify-between items-center py-2 border-b border-gray-100">
                <span className="text-gray-500">Ngân hàng</span>
                <span className="font-bold">{qrPaymentInfo.bankCode}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-100">
                <span className="text-gray-500">Số tài khoản</span>
                <div className="flex items-center gap-2">
                  <span className="font-bold">{qrPaymentInfo.accountNumber}</span>
                  <button 
                    onClick={handleCopyAccount}
                    className="p-1 hover:bg-gray-100 rounded transition-colors"
                  >
                    {copied ? <Check size={14} className="text-green-500" /> : <Copy size={14} />}
                  </button>
                </div>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-100">
                <span className="text-gray-500">Chủ tài khoản</span>
                <span className="font-bold">{qrPaymentInfo.accountName}</span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-100">
                <span className="text-gray-500">Số tiền</span>
                <span className="font-bold text-emerald-500 text-lg">{qrPaymentInfo.amount?.toLocaleString('vi-VN')}đ</span>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-gray-500">Nội dung CK</span>
                <span className="font-bold text-blue-600">{qrPaymentInfo.description}</span>
              </div>
            </div>
            
            <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-3 mb-4">
              <p className="text-xs text-yellow-800">
                ⚠️ <strong>Quan trọng:</strong> Nhập đúng nội dung CK <strong className="text-blue-600">{qrPaymentInfo.description}</strong>
              </p>
            </div>
            
            <button
              onClick={() => {
                setShowQRModal(false)
                toast.success('Đã ghi nhận! Đơn hàng sẽ được cập nhật khi thanh toán thành công.')
                loadOrders()
              }}
              className="w-full py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-xl font-bold hover:shadow-lg transition-all"
            >
              ✓ Tôi đã thanh toán
            </button>
          </div>
        </div>
      )}
      
      {/* Payment Success Modal */}
      {paymentSuccess && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-8 text-center animate-in fade-in zoom-in duration-300">
            <div className="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <CheckCircle size={48} className="text-green-500" />
            </div>
            
            <h3 className="text-2xl font-bold text-gray-900 mb-2">Thanh toán thành công! 🎉</h3>
            <p className="text-gray-500 mb-6">
              Đơn hàng #{paidOrderId} đã được thanh toán và đang được xử lý.
            </p>
            
            <div className="bg-green-50 border border-green-200 rounded-xl p-4 mb-6">
              <p className="text-sm text-green-800">
                ✓ Đơn hàng của bạn sẽ được chuẩn bị và giao đến địa chỉ đã đăng ký.
              </p>
            </div>
            
            <button
              onClick={() => {
                setPaymentSuccess(false)
                setPaidOrderId(null)
                loadOrders()
              }}
              className="w-full py-4 bg-gradient-to-r from-emerald-500 to-teal-600 text-white rounded-xl font-bold text-lg hover:shadow-lg transition-all"
            >
              Đóng
            </button>
          </div>
        </div>
      )}

    </div>
  )
}
