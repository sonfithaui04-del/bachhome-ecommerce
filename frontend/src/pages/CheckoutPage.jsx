import { useState, useEffect } from 'react'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

import { MapPin, Phone, CreditCard, CheckCircle, Loader, ArrowLeft, QrCode, X, Copy, Check, PartyPopper } from 'lucide-react'
import toast from 'react-hot-toast'

export default function CheckoutPage() {
  const { cart, total, clearCart } = useCart()
  const { user, getToken } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('COD')
  const [showQRModal, setShowQRModal] = useState(false)
  const [qrPaymentInfo, setQrPaymentInfo] = useState(null)
  const [copied, setCopied] = useState(false)
  const [paymentSuccess, setPaymentSuccess] = useState(false)
  const [formData, setFormData] = useState({
    address: '',
    phone: '',
    note: ''
  })
  const [pointsToUse, setPointsToUse] = useState(0)
  
  // Custom total after applying points. 1 point = 1000 VND
  const discountAmount = pointsToUse * 1000
  const finalTotal = Math.max(0, total - discountAmount)

  // Listen for PAYMENT_SUCCESS notification from WebSocket
  useEffect(() => {
    const handlePaymentSuccess = (event) => {
      const notification = event.detail
      if (notification?.type === 'PAYMENT_SUCCESS' && 
          qrPaymentInfo?.orderId === notification.orderId) {
        // Thanh toán thành công - hiển thị modal success
        setPaymentSuccess(true)
        clearCart()
      }
    }

    window.addEventListener('notification_received', handlePaymentSuccess)
    return () => window.removeEventListener('notification_received', handlePaymentSuccess)
  }, [qrPaymentInfo, clearCart])

  if (cart.length === 0 && !showQRModal && !paymentSuccess) {
    setTimeout(() => navigate('/menu'), 0)
    return null
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // Validate Phone (10-11 digits)
    const phoneRegex = /^[0-9]{10,11}$/
    if (!phoneRegex.test(formData.phone)) {
      toast.error('Số điện thoại phải từ 10-11 số')
      return 
    }

    setLoading(true)

    try {
      const token = getToken()
      const orderData = {
        userId: user?.userId,
        email: user?.email,
        customerName: user?.fullName || user?.email || 'Khách hàng',
        items: cart.map(item => ({
          menuItemId: item.id,
          menuItemName: item.name,
          quantity: item.quantity,
          price: item.price,
          imageUrl: item.imageUrl
        })),
        deliveryAddress: formData.address,
        phoneNumber: formData.phone,
        notes: formData.note,
        paymentMethod: paymentMethod,
        pointsToUse: pointsToUse
      }

      // Tạo order trước
      const orderResponse = await axios.post('/api/orders', orderData, {
        headers: { Authorization: `Bearer ${token}` }
      })

      const orderId = orderResponse.data.id

      if (paymentMethod === 'SEPAY') {
        // Thanh toán SePay - Lấy QR code
        const paymentResponse = await axios.post('/api/payments/sepay/init', {
          orderId: orderId,
          userId: user?.userId,
          amount: finalTotal
        }, {
          headers: { Authorization: `Bearer ${token}` }
        })

        setQrPaymentInfo(paymentResponse.data)
        setShowQRModal(true)
        setLoading(false)
      } else {
        // COD - Hoàn tất ngay
        clearCart()
        navigate('/my-orders')
        toast.success('Đặt hàng thành công! 🎉')
      }
    } catch (error) {
      toast.error('Đặt hàng thất bại: ' + (error.response?.data?.message || error.message))
      setLoading(false)
    }
  }

  const handleCopyAccount = () => {
    navigator.clipboard.writeText(qrPaymentInfo?.accountNumber || '')
    setCopied(true)
    toast.success('Đã copy số tài khoản!')
    setTimeout(() => setCopied(false), 2000)
  }


  const handlePaymentConfirmed = async () => {
    try {
      const token = getToken()
      await axios.post(`/api/payments/sepay/confirm/${qrPaymentInfo?.orderId}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      })
      clearCart()
      setShowQRModal(false)
      navigate('/my-orders')
      toast.success('Đã ghi nhận thanh toán! Đơn hàng đang được xử lý.')
    } catch (error) {
      toast.error('Lỗi xác nhận thanh toán: ' + error.message)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      
      <div className="container mx-auto px-4 pt-32 pb-20">
        <div className="max-w-6xl mx-auto">
          <button onClick={() => navigate('/cart')} className="flex items-center gap-2 text-gray-500 hover:text-emerald-500 mb-8 transition-colors">
            <ArrowLeft size={20} /> Quay lại giỏ hàng
          </button>
          
          <div className="flex flex-col lg:flex-row gap-12">
            
            {/* Checkout Form */}
            <div className="flex-1">
              <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 mb-8">
                <div className="flex items-center gap-4 mb-8">
                  <div className="w-12 h-12 bg-emerald-100 rounded-full flex items-center justify-center text-emerald-500">
                    <MapPin size={24} />
                  </div>
                  <div>
                    <h2 className="text-2xl font-bold text-gray-900">Thông tin giao hàng</h2>
                    <p className="text-gray-500">Chúng tôi sẽ giao đến đâu?</p>
                  </div>
                </div>

                <form id="checkout-form" onSubmit={handleSubmit} className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Địa chỉ đầy đủ</label>
                    <div className="relative">
                      <input
                        type="text"
                        required
                        value={formData.address}
                        onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                        className="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                        placeholder="VD: 123 Nguyễn Huệ, Quận 1, TP.HCM"
                      />
                      <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
                    <div className="relative">
                      <input
                        type="tel"
                        required
                        value={formData.phone}
                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                        className="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                        placeholder="VD: 0901234567"
                      />
                      <Phone className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Ghi chú (Không bắt buộc)</label>
                    <textarea
                      value={formData.note}
                      onChange={(e) => setFormData({ ...formData, note: e.target.value })}
                      rows="3"
                      className="w-full px-4 py-4 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                      placeholder="VD: Giao giờ hành chính, gọi trước khi giao..."
                    />
                  </div>
                </form>
              </div>

              <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100">
                <div className="flex items-center gap-4 mb-8">
                  <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-500">
                    <CreditCard size={24} />
                  </div>
                  <div>
                    <h2 className="text-2xl font-bold text-gray-900">Phương thức thanh toán</h2>
                    <p className="text-gray-500">Chọn cách thanh toán</p>
                  </div>
                </div>

                <div className="space-y-4">
                  <label 
                    className={`flex items-center gap-4 p-4 border-2 rounded-xl cursor-pointer transition-all ${
                      paymentMethod === 'COD' 
                        ? 'border-emerald-500 bg-emerald-50' 
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                    onClick={() => setPaymentMethod('COD')}
                  >
                    <input 
                      type="radio" 
                      name="payment" 
                      checked={paymentMethod === 'COD'}
                      onChange={() => setPaymentMethod('COD')}
                      className="w-5 h-5 text-emerald-600 focus:ring-emerald-500" 
                    />
                    <div className="flex-1">
                      <span className="font-bold text-gray-800">💵 Thanh toán khi nhận hàng (COD)</span>
                      <p className="text-sm text-gray-500">Trả tiền mặt khi nhận đơn</p>
                    </div>
                    {paymentMethod === 'COD' && <CheckCircle className="text-emerald-500" size={20} />}
                  </label>
                  
                  <label 
                    className={`flex items-center gap-4 p-4 border-2 rounded-xl cursor-pointer transition-all ${
                      paymentMethod === 'SEPAY' 
                        ? 'border-blue-500 bg-blue-50' 
                        : 'border-gray-200 hover:bg-gray-50'
                    }`}
                    onClick={() => setPaymentMethod('SEPAY')}
                  >
                    <input 
                      type="radio" 
                      name="payment"
                      checked={paymentMethod === 'SEPAY'}
                      onChange={() => setPaymentMethod('SEPAY')}
                      className="w-5 h-5 text-blue-600 focus:ring-blue-500" 
                    />
                    <div className="flex-1">
                      <span className="font-bold text-gray-800">🏦 Chuyển khoản ngân hàng (QR)</span>
                      <p className="text-sm text-gray-500">Quét mã QR để thanh toán</p>
                    </div>
                    <QrCode className={paymentMethod === 'SEPAY' ? 'text-blue-500' : 'text-gray-400'} size={24} />
                  </label>
                </div>
              </div>
              
              {/* Loyalty Points Section */}
              <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 mt-8">
                <div className="flex items-center gap-4 mb-4">
                  <div className="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center text-yellow-500">
                    <CheckCircle size={24} />
                  </div>
                  <div>
                    <h2 className="text-2xl font-bold text-gray-900">Điểm thưởng (Loyalty Points)</h2>
                    <p className="text-gray-500">Bạn đang có <strong className="text-yellow-600">{user?.loyaltyPoints || 0}</strong> điểm</p>
                  </div>
                </div>
                
                {(!user || (user.loyaltyPoints || 0) === 0) ? (
                   <p className="text-sm text-gray-500 italic">Bạn chưa gắn kết đủ để có điểm thưởng hoặc cần phải tải lại trang để thấy điểm mới.</p>
                ) : (
                   <div>
                     <label className="block text-sm font-medium text-gray-700 mb-2">Số điểm muốn dùng (1 điểm = 1.000đ)</label>
                     <div className="flex gap-4 items-center">
                       <input
                         type="number"
                         min="0"
                         max={Math.min(user?.loyaltyPoints || 0, Math.ceil(total / 1000))}
                         value={pointsToUse}
                         onChange={(e) => setPointsToUse(Math.min(parseInt(e.target.value) || 0, user?.loyaltyPoints || 0, Math.ceil(total / 1000)))}
                         className="flex-1 px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all outline-none"
                         placeholder="Nhập số điểm..."
                       />
                       <button 
                         type="button" 
                         onClick={() => setPointsToUse(Math.min(user?.loyaltyPoints || 0, Math.ceil(total / 1000)))}
                         className="px-6 py-3 bg-yellow-100 text-yellow-700 font-bold rounded-xl hover:bg-yellow-200 transition-colors"
                       >
                         Dùng tối đa
                       </button>
                     </div>
                     {pointsToUse > 0 && (
                        <p className="text-sm text-green-600 font-medium mt-2">
                          Bạn sẽ được giảm <strong className="text-lg">{(pointsToUse * 1000).toLocaleString('vi-VN')}đ</strong>
                        </p>
                     )}
                   </div>
                )}
              </div>
            </div>

            {/* Order Summary Sidebar */}
            <div className="lg:w-96">
              <div className="bg-white p-8 rounded-3xl shadow-lg border border-gray-100 sticky top-32">
                <h2 className="text-xl font-bold text-gray-900 mb-6">Đơn hàng của bạn</h2>
                
                <div className="max-h-60 overflow-y-auto mb-6 pr-2 space-y-4 custom-scrollbar">
                  {cart.map((item) => (
                    <div key={item.id} className="flex items-center gap-4">
                      <div className="w-16 h-16 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                        <img 
                          src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=100&q=80'} 
                          alt="" 
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=100&q=80';
                          }}
                        />
                      </div>
                      <div className="flex-1">
                        <h4 className="font-bold text-gray-800 text-sm line-clamp-1">{item.name}</h4>
                        <div className="flex justify-between text-sm mt-1">
                          <span className="text-gray-500">x{item.quantity}</span>
                          <span className="font-medium text-gray-900">{((item.price || 0) * item.quantity).toLocaleString('vi-VN')}đ</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="border-t border-dashed border-gray-200 pt-4 space-y-3 mb-8">
                  <div className="flex justify-between text-gray-600">
                    <span>Tạm tính</span>
                    <span>{(total || 0).toLocaleString('vi-VN')}đ</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Phí giao hàng</span>
                    <span className="text-green-500">Miễn phí</span>
                  </div>
                  {discountAmount > 0 && (
                    <div className="flex justify-between text-green-600 font-medium">
                      <span>Dùng điểm thưởng (-{pointsToUse}d)</span>
                      <span>-{discountAmount.toLocaleString('vi-VN')}đ</span>
                    </div>
                  )}
                  <div className="flex justify-between text-xl font-bold text-gray-900 pt-2 border-t border-gray-100 mt-2">
                    <span>Tổng cộng</span>
                    <span className="text-emerald-500">{finalTotal.toLocaleString('vi-VN')}đ</span>
                  </div>
                </div>

                <button
                  type="submit"
                  form="checkout-form"
                  disabled={loading}
                  className={`w-full py-4 rounded-xl font-bold text-lg transform hover:-translate-y-1 transition-all duration-300 flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed ${
                    paymentMethod === 'SEPAY'
                      ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white hover:shadow-lg hover:shadow-blue-500/40'
                      : 'bg-gradient-to-r from-emerald-500 to-teal-600 text-white hover:shadow-lg hover:shadow-emerald-500/40'
                  }`}
                >
                  {loading ? (
                    <>
                      <Loader className="animate-spin" size={20} /> Đang xử lý...
                    </>
                  ) : paymentMethod === 'SEPAY' ? (
                    <>
                      <QrCode size={20} /> Thanh toán QR
                    </>
                  ) : (
                    'Đặt hàng'
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      {/* QR Payment Modal - Horizontal Layout */}
      {showQRModal && qrPaymentInfo && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-3xl max-w-4xl w-full p-6 relative animate-in fade-in zoom-in duration-300 max-h-[90vh] overflow-y-auto">
            <button 
              onClick={() => setShowQRModal(false)}
              className="absolute top-4 right-4 p-2 hover:bg-gray-100 rounded-full transition-colors z-10"
            >
              <X size={24} />
            </button>
            
            <div className="flex flex-col lg:flex-row gap-6">
              {/* Left Side - QR & Payment Info */}
              <div className="lg:w-1/2">
                <div className="text-center mb-4">
                  <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <QrCode size={24} className="text-blue-500" />
                  </div>
                  <h3 className="text-xl font-bold text-gray-900">Quét mã để thanh toán</h3>
                  <p className="text-gray-500 text-sm">Sử dụng app ngân hàng để quét mã QR</p>
                </div>
                
                <div className="bg-gray-50 rounded-2xl p-4 mb-4">
                  <img 
                    src={qrPaymentInfo.qrCodeUrl} 
                    alt="QR Payment" 
                    className="w-full max-w-[200px] mx-auto rounded-xl shadow-lg"
                  />
                </div>
                
                <div className="space-y-2 text-sm">
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
              </div>
              
              {/* Right Side - Order Summary */}
              <div className="lg:w-1/2 lg:border-l lg:border-gray-200 lg:pl-6">
                <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                  📦 Chi tiết đơn hàng #{qrPaymentInfo.orderId}
                </h3>
                
                <div className="space-y-3 mb-4 max-h-48 overflow-y-auto pr-2">
                  {cart.map((item, index) => (
                    <div key={index} className="flex items-center gap-3 bg-gray-50 p-2 rounded-lg">
                      <div className="w-10 h-10 rounded-lg overflow-hidden bg-gray-200 shrink-0">
                        <img 
                          src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=80&q=80'}
                          alt={item.name}
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=80&q=80';
                          }}
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-gray-800 text-sm truncate">{item.name}</p>
                        <p className="text-xs text-gray-500">x{item.quantity} • {item.price?.toLocaleString('vi-VN')}đ</p>
                      </div>
                      <span className="font-bold text-gray-900 text-sm">{(item.price * item.quantity)?.toLocaleString('vi-VN')}đ</span>
                    </div>
                  ))}
                </div>
                
                <div className="border-t border-gray-200 pt-3 mb-4">
                  <div className="flex justify-between items-center text-sm mb-2">
                    <span className="text-gray-500">Địa chỉ</span>
                    <span className="font-medium text-gray-800 text-right max-w-[200px] truncate">{formData.address}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm mb-2">
                    <span className="text-gray-500">SĐT</span>
                    <span className="font-medium text-gray-800">{formData.phone}</span>
                  </div>
                  {formData.note && (
                    <div className="flex justify-between items-center text-sm">
                      <span className="text-gray-500">Ghi chú</span>
                      <span className="font-medium text-gray-800 text-right max-w-[200px] truncate">{formData.note}</span>
                    </div>
                  )}
                </div>
                
                <div className="bg-emerald-50 rounded-xl p-3 mb-4">
                  <div className="flex justify-between items-center">
                    <span className="font-bold text-gray-900">Tổng thanh toán</span>
                    <span className="text-xl font-bold text-emerald-500">{finalTotal?.toLocaleString('vi-VN')}đ</span>
                  </div>
                </div>
                
                <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-3 mb-4">
                  <p className="text-xs text-yellow-800">
                    ⚠️ <strong>Quan trọng:</strong> Nhập đúng nội dung CK <strong className="text-blue-600">{qrPaymentInfo.description}</strong>
                  </p>
                </div>
                
                <button
                  onClick={handlePaymentConfirmed}
                  className="w-full py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-xl font-bold hover:shadow-lg transition-all"
                >
                  ✓ Tôi đã thanh toán
                </button>
              </div>
            </div>
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
              Đơn hàng #{qrPaymentInfo?.orderId} đã được thanh toán.
              <br />Cảm ơn bạn đã sử dụng dịch vụ!
            </p>
            
            <div className="bg-green-50 rounded-xl p-4 mb-6">
              <div className="flex justify-between text-lg">
                <span className="text-gray-600">Tổng thanh toán:</span>
                <span className="font-bold text-green-600">{qrPaymentInfo?.amount?.toLocaleString('vi-VN')}đ</span>
              </div>
            </div>
            
            <button
              onClick={() => {
                setPaymentSuccess(false)
                setShowQRModal(false)
                navigate('/my-orders')
              }}
              className="w-full py-4 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-xl font-bold text-lg hover:shadow-lg transition-all flex items-center justify-center gap-2"
            >
              <CheckCircle size={20} /> Xem đơn hàng
            </button>
          </div>
        </div>
      )}
      
    </div>
  )
}
