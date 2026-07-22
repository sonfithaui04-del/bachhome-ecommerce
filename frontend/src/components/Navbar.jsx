import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { useWishlist } from '../context/WishlistContext'
import { ShoppingBag, User, LogOut, Menu, X, Home, Bell, Heart } from 'lucide-react'
import { useState, useEffect, useRef } from 'react'
import { notificationService } from '../services/notificationService'
import { connectSocket } from '../services/socketService'
import toast from 'react-hot-toast'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { cart } = useCart()
  const { wishlist } = useWishlist()
  const navigate = useNavigate()
  const location = useLocation()
  const [isScrolled, setIsScrolled] = useState(false)
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
  
  // Notification State
  const [notifications, setNotifications] = useState([])
  const [showNotifications, setShowNotifications] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  const notificationRef = useRef(null)

  // Handle scroll effect
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20)
    }
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  const fetchNotifications = async () => {
    try {
      const data = await notificationService.getUserNotifications(user.userId)
      setNotifications(data)
      const lastReadTime = localStorage.getItem('lastReadNotificationTime')
      const newCount = data.filter(n => !lastReadTime || new Date(n.createdAt) > new Date(lastReadTime)).length
      setUnreadCount(newCount)
    } catch (error) {
      console.error('Failed to fetch notifications', error)
    }
  }

  // Fetch notifications & Connect Socket
  useEffect(() => {
    const stompClientRef = { current: null };

    if (user) {
      fetchNotifications()
      
      // Connect to socket with userId
      stompClientRef.current = connectSocket((newNotification) => {
        setNotifications(prev => [newNotification, ...prev])
        setUnreadCount(prev => prev + 1)
        
        toast(newNotification.message, {
          id: newNotification.id || `notif-${Date.now()}`, // Prevent duplicate toasts
          icon: '🔔',
          duration: 5000
        })
        
        // Dispatch event for other components (like MyOrdersPage)
        window.dispatchEvent(new CustomEvent('notification_received', { detail: newNotification }))
      }, user.userId || user.id) // Ensure userId is passed
    }

    return () => {
       // Don't disconnect here in Strict Mode to avoid reconnection loops.
       // The socket service is now a singleton and handles reuse.
    }
  }, [user])

  const handleNotificationClick = () => {
    setShowNotifications(!showNotifications)
    if (!showNotifications && notifications.length > 0) {
      setUnreadCount(0)
      localStorage.setItem('lastReadNotificationTime', new Date().toISOString())
    }
  }

  const handleLogout = () => {
    import('../services/socketService').then(module => module.disconnectSocket());
    logout()
    navigate('/')
  }

  const navLinks = [
    { name: 'Trang chủ', path: '/' },
    { name: 'Sản phẩm', path: '/menu' },
    { name: 'Yêu thích', path: '/wishlist' },
    ...(user ? [{ name: 'Đơn hàng', path: '/my-orders' }] : []),
    ...(user?.role === 'SHIPPER' ? [{ name: 'Shipper', path: '/shipper' }] : [])
  ]

  const isActive = (path) => location.pathname === path

  return (
    <nav 
      className={`fixed w-full z-50 transition-all duration-300 ${
        isScrolled
          ? 'bg-white/90 backdrop-blur-md shadow-lg py-3'
          : 'bg-white/80 backdrop-blur-md shadow-sm py-4'
      }`}
    >
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 group">
            <div className="bg-emerald-500 p-2 rounded-xl text-white transform group-hover:rotate-12 transition-transform duration-300">
              <Home size={28} />
            </div>
            <span className={`text-2xl font-bold font-poppins ${
              isScrolled ? 'text-gray-800' : 'text-gray-800'
            }`}>
              Bach<span className="text-emerald-500">Home</span>
            </span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-8">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={`font-medium transition-colors duration-300 relative group ${
                  isActive(link.path) ? 'text-emerald-500' : 'text-gray-600 hover:text-emerald-500'
                }`}
              >
                {link.name}
                <span className={`absolute -bottom-1 left-0 w-0 h-0.5 bg-emerald-500 transition-all duration-300 group-hover:w-full ${
                  isActive(link.path) ? 'w-full' : ''
                }`}></span>
              </Link>
            ))}
          </div>

          {/* Right Actions */}
          <div className="hidden md:flex items-center gap-6">
            {user ? (
              <>
                {/* Notification Bell */}
                <div className="relative" ref={notificationRef}>
                  <button 
                    onClick={handleNotificationClick}
                    className="p-2 rounded-full hover:bg-emerald-100 transition-colors text-gray-600 hover:text-emerald-500 relative"
                  >
                    <Bell size={24} />
                    {unreadCount > 0 && (
                      <span className="absolute top-0 right-0 bg-red-500 text-white text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full border-2 border-white animate-pulse">
                        {unreadCount}
                      </span>
                    )}
                  </button>

                {/* Notification Dropdown */}
                  {showNotifications && (
                    <div className="absolute right-0 mt-2 w-80 bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden animate-in slide-in-from-top-2 z-50">
                      <div className="p-4 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
                        <h3 className="font-bold text-gray-800">Thông báo</h3>
                        <span className="text-xs text-gray-500">{notifications.length} tin mới</span>
                      </div>
                      <div className="max-h-96 overflow-y-auto custom-scrollbar">
                        {notifications.length === 0 ? (
                          <div className="p-8 text-center text-gray-500">
                            <Bell size={32} className="mx-auto mb-2 opacity-20" />
                            <p>Chưa có thông báo nào</p>
                          </div>
                        ) : (
                          notifications.map((notif) => (
                            <div key={notif.id} className="p-4 border-b border-gray-50 hover:bg-emerald-50 transition-colors">
                              <div className="flex gap-3">
                                <div className="mt-1">
                                  <div className="w-2 h-2 rounded-full bg-emerald-500"></div>
                                </div>
                                <div>
                                  <h4 className="font-semibold text-gray-800 text-sm">{notif.subject}</h4>
                                  <p className="text-xs text-gray-500 mt-1">{notif.message}</p>
                                  <span className="text-[10px] text-gray-400 mt-2 block">
                                    {new Date(notif.createdAt).toLocaleString('vi-VN')}
                                  </span>
                                </div>
                              </div>
                            </div>
                          ))
                        )}
                      </div>
                    </div>
                  )}
                </div>

                <Link to="/cart" className="relative group">
                  <div className="p-2 rounded-full hover:bg-emerald-100 transition-colors text-gray-600 group-hover:text-emerald-500">
                    <ShoppingBag size={24} />
                    {cart.length > 0 && (
                      <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold w-5 h-5 flex items-center justify-center rounded-full border-2 border-white animate-bounce">
                        {cart.length}
                      </span>
                    )}
                  </div>
                </Link>

                <div className="flex items-center gap-4 pl-4 border-l border-gray-200">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-gradient-to-r from-emerald-400 to-teal-500 flex items-center justify-center text-white font-bold text-sm">
                      {user.email?.charAt(0).toUpperCase()}
                    </div>
                    <span className="text-sm font-medium text-gray-700 max-w-[100px] truncate">
                      {user.email}
                    </span>
                  </div>
                  <button 
                    onClick={handleLogout}
                    className="p-2 text-gray-400 hover:text-red-500 transition-colors"
                    title="Đăng xuất"
                  >
                    <LogOut size={20} />
                  </button>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-4">
                <Link 
                  to="/login"
                  className="text-gray-600 hover:text-emerald-500 font-medium transition-colors"
                >
                  Đăng nhập
                </Link>
                <Link 
                  to="/register"
                  className="bg-gradient-to-r from-emerald-500 to-teal-600 text-white px-6 py-2.5 rounded-full font-medium hover:shadow-lg hover:shadow-emerald-500/30 transform hover:-translate-y-0.5 transition-all duration-300"
                >
                  Đăng ký
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button 
            className="md:hidden text-gray-700"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          >
            {isMobileMenuOpen ? <X size={28} /> : <Menu size={28} />}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden absolute top-full left-0 w-full bg-white shadow-xl border-t border-gray-100 py-4 px-4 flex flex-col gap-4 animate-in slide-in-from-top-5">
          {navLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              onClick={() => setIsMobileMenuOpen(false)}
              className={`text-lg font-medium py-2 ${
                isActive(link.path) ? 'text-emerald-500' : 'text-gray-600'
              }`}
            >
              {link.name}
            </Link>
          ))}
          <hr className="border-gray-100" />
          {user ? (
            <>
              <Link 
                to="/cart"
                onClick={() => setIsMobileMenuOpen(false)}
                className="flex items-center justify-between text-gray-600 py-2"
              >
                <span>Giỏ hàng ({cart.length})</span>
                <ShoppingBag size={20} />
              </Link>
              <button 
                onClick={() => {
                  handleLogout()
                  setIsMobileMenuOpen(false)
                }}
                className="text-left text-red-500 font-medium py-2"
              >
                Đăng xuất
              </button>
            </>
          ) : (
            <div className="flex flex-col gap-3 mt-2">
              <Link 
                to="/login"
                onClick={() => setIsMobileMenuOpen(false)}
                className="text-center py-2.5 border border-gray-200 rounded-xl font-medium text-gray-700"
              >
                Đăng nhập
              </Link>
              <Link 
                to="/register"
                onClick={() => setIsMobileMenuOpen(false)}
                className="text-center py-2.5 bg-emerald-500 text-white rounded-xl font-medium"
              >
                Đăng ký
              </Link>
            </div>
          )}
        </div>
      )}
    </nav>
  )
}
