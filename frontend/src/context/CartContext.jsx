import React, { createContext, useState, useContext, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuth } from './AuthContext'

const CartContext = createContext()

// Giỏ hàng lưu riêng cho từng tài khoản, không dùng chung một khoá
const storageKey = (userId) => `bachhome_cart_u${userId}`

const readCart = (userId) => {
  if (!userId) return []
  try {
    const saved = localStorage.getItem(storageKey(userId))
    return saved ? JSON.parse(saved) : []
  } catch {
    return []
  }
}

export const CartProvider = ({ children }) => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const userId = user?.userId || user?.id || null
  const [cart, setCart] = useState([])
  const loadedFor = useRef(null)

  // Đổi tài khoản (đăng nhập, đăng xuất, đăng ký mới) thì nạp lại giỏ của đúng tài khoản đó
  useEffect(() => {
    setCart(readCart(userId))
    loadedFor.current = userId
  }, [userId])

  // Chỉ ghi xuống localStorage sau khi đã nạp xong giỏ của tài khoản hiện tại
  useEffect(() => {
    if (!userId || loadedFor.current !== userId) return
    localStorage.setItem(storageKey(userId), JSON.stringify(cart))
  }, [cart, userId])

  // Khách chưa đăng nhập thì không giữ giỏ hàng tạm, tránh dữ liệu lẫn sang tài khoản khác
  const requireLogin = (action = 'mua hàng') => {
    if (userId) return true
    toast.error(`Vui lòng đăng nhập để ${action}`)
    navigate('/login')
    return false
  }

  const addToCart = (item) => {
    if (!requireLogin('thêm sản phẩm vào giỏ hàng')) return false
    setCart(prev => {
      const existing = prev.find(i => i.id === item.id)
      if (existing) {
        toast.success(`Đã tăng số lượng "${item.name}"`)
        return prev.map(i =>
          i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i
        )
      }
      toast.success(`Đã thêm "${item.name}" vào giỏ hàng`)
      return [...prev, { ...item, quantity: 1 }]
    })
    return true
  }

  const removeFromCart = (itemId) => {
    setCart(prev => prev.filter(i => i.id !== itemId))
  }

  const updateQuantity = (itemId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(itemId)
      return
    }
    setCart(prev => prev.map(i =>
      i.id === itemId ? { ...i, quantity } : i
    ))
  }

  const clearCart = () => setCart([])

  const total = cart.reduce((sum, item) => {
    const price = parseFloat(item.price) || 0
    return sum + (price * item.quantity)
  }, 0)

  return (
    <CartContext.Provider value={{
      cart, addToCart, removeFromCart, updateQuantity, clearCart, total, requireLogin
    }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
