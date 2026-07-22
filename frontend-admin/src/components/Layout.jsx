import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  LayoutDashboard,
  Package,
  FolderOpen,
  Boxes,
  ShoppingBag,
  Users,
  LogOut,
  Home,
  Menu,
  Bell
} from 'lucide-react'
import { useState, useEffect, useRef } from 'react'
import { Toaster, toast } from 'react-hot-toast'
import { notificationService } from '../services/notificationService'
import { connectSocket } from '../services/socketService'

export default function Layout({ children }) {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [isSidebarOpen, setIsSidebarOpen] = useState(true)
  
  // Notification State
  const [notifications, setNotifications] = useState([])
  const [showNotifications, setShowNotifications] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  const notificationRef = useRef(null)

  // Fetch notifications & Connect Socket
  useEffect(() => {
    const stompClientRef = { current: null };

    if (user) {
      fetchNotifications()
      
      stompClientRef.current = connectSocket((newNotification) => {
        setNotifications(prev => {
          if (prev.some(n => n.id === newNotification.id && newNotification.id)) return prev;
          return [newNotification, ...prev];
        })
        
        setUnreadCount(prev => prev + 1)
        
        toast(newNotification.message, {
          id: newNotification.id || `notif-${Date.now()}`,
          icon: '🔔',
          duration: 5000
        })
        
        window.dispatchEvent(new CustomEvent('notification_received', { detail: newNotification }))
      })
    }

    return () => {}
  }, [user])

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotifications(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const fetchNotifications = async () => {
    try {
      const data = await notificationService.getAllNotifications()
      setNotifications(data)
      const lastReadTime = localStorage.getItem('adminLastReadNotificationTime')
      const newCount = data.filter(n => !lastReadTime || new Date(n.createdAt) > new Date(lastReadTime)).length
      setUnreadCount(newCount)
    } catch (error) {
      console.error('Failed to fetch notifications', error)
    }
  }

  const handleNotificationClick = () => {
    setShowNotifications(!showNotifications)
    if (!showNotifications && notifications.length > 0) {
      setUnreadCount(0)
      localStorage.setItem('adminLastReadNotificationTime', new Date().toISOString())
    }
  }

  const handleLogout = () => {
    import('../services/socketService').then(module => module.disconnectSocket());
    logout()
    navigate('/login')
  }

  const menuItems = [
    { path: '/', icon: LayoutDashboard, label: 'Tổng quan' },
    { path: '/orders', icon: ShoppingBag, label: 'Đơn hàng' },
    { path: '/menu', icon: Package, label: 'Sản phẩm' },
    { path: '/categories', icon: FolderOpen, label: 'Danh mục' },
    { path: '/ingredients', icon: Boxes, label: 'Tồn kho' },
    { path: '/users', icon: Users, label: 'Người dùng' }
  ]

  return (
    <div className="min-h-screen bg-gray-50 flex font-inter">
      <Toaster position="top-right" />
      {/* Sidebar */}
      <aside 
        className={`fixed inset-y-0 left-0 z-50 bg-white border-r border-gray-200 transition-all duration-300 flex flex-col ${
          isSidebarOpen ? 'w-64' : 'w-20'
        }`}
      >
        {/* Logo */}
        <div className="h-16 flex items-center justify-center border-b border-gray-100">
          <div className="flex items-center gap-2 text-emerald-600 font-bold text-xl">
            <div className="bg-emerald-600 text-white p-1.5 rounded-lg">
              <Home size={24} />
            </div>
            {isSidebarOpen && <span className="font-poppins text-gray-800">BachHome<span className="text-emerald-600"> Admin</span></span>}
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-6 px-3 space-y-1 overflow-y-auto custom-scrollbar">
          {menuItems.map((item) => {
            const Icon = item.icon
            const isActive = location.pathname === item.path
            
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-3 py-3 rounded-xl transition-all duration-200 group ${
                  isActive
                    ? 'bg-emerald-50 text-emerald-600 font-semibold shadow-sm'
                    : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'
                }`}
                title={!isSidebarOpen ? item.label : ''}
              >
                <div className={`${isActive ? 'text-emerald-600' : 'text-gray-400 group-hover:text-gray-600'}`}>
                  <Icon size={22} strokeWidth={isActive ? 2.5 : 2} />
                </div>
                {isSidebarOpen && <span>{item.label}</span>}
                
                {isActive && isSidebarOpen && (
                  <div className="ml-auto w-1.5 h-1.5 rounded-full bg-emerald-600"></div>
                )}
              </Link>
            )
          })}
        </nav>

        {/* User Profile */}
        <div className="p-4 border-t border-gray-100">
          <div className={`flex items-center gap-3 ${!isSidebarOpen && 'justify-center'}`}>
            <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-500 flex items-center justify-center text-white font-bold shadow-md">
              {user?.fullName?.charAt(0) || 'A'}
            </div>
            
            {isSidebarOpen && (
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-gray-900 truncate">{user?.fullName || 'Admin'}</p>
                <p className="text-xs text-gray-500 truncate">{user?.email}</p>
              </div>
            )}
            
            {isSidebarOpen && (
              <button 
                onClick={handleLogout}
                className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                title="Đăng xuất"
              >
                <LogOut size={18} />
              </button>
            )}
          </div>
        </div>
      </aside>

      {/* Main Content Wrapper */}
      <div className={`flex-1 flex flex-col transition-all duration-300 ${isSidebarOpen ? 'ml-64' : 'ml-20'}`}>
        
        {/* Top Header */}
        <header className="h-16 bg-white border-b border-gray-200 sticky top-0 z-40 px-8 flex items-center justify-between shadow-sm">
          <div className="flex items-center gap-4">
            <button 
              onClick={() => setIsSidebarOpen(!isSidebarOpen)}
              className="p-2 text-gray-500 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <Menu size={20} />
            </button>
            <h2 className="text-lg font-semibold text-gray-800">
              {menuItems.find(i => i.path === location.pathname)?.label || 'Tổng quan'}
            </h2>
          </div>

          <div className="flex items-center gap-4">
            {/* Notification Bell */}
            <div className="relative" ref={notificationRef}>
              <button 
                onClick={handleNotificationClick}
                className="p-2 text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-full transition-all relative"
              >
                <Bell size={20} />
                {unreadCount > 0 && (
                  <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border border-white animate-pulse"></span>
                )}
              </button>

              {/* Notification Dropdown */}
              {showNotifications && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border border-gray-100 overflow-hidden animate-in slide-in-from-top-2 z-50">
                  <div className="p-4 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
                    <h3 className="font-bold text-gray-800">Thông báo</h3>
                    <span className="text-xs text-gray-500">Tổng: {notifications.length}</span>
                  </div>
                  <div className="max-h-96 overflow-y-auto custom-scrollbar">
                    {notifications.length === 0 ? (
                      <div className="p-8 text-center text-gray-500">
                        <Bell size={32} className="mx-auto mb-2 opacity-20" />
                        <p>Chưa có thông báo mới</p>
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
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-8 overflow-auto">
          {children}
        </main>
      </div>
    </div>
  )
}
