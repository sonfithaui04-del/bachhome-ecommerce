import React, { createContext, useState, useContext, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuth } from './AuthContext'

const WishlistContext = createContext()

// Danh sách yêu thích lưu riêng cho từng tài khoản, không dùng chung một khoá
const storageKey = (userId) => `bachhome_wishlist_u${userId}`

const readWishlist = (userId) => {
  if (!userId) return []
  try {
    const saved = localStorage.getItem(storageKey(userId))
    return saved ? JSON.parse(saved) : []
  } catch {
    return []
  }
}

export const WishlistProvider = ({ children }) => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const userId = user?.userId || user?.id || null
  const [wishlist, setWishlist] = useState([])
  const loadedFor = useRef(null)

  // Đổi tài khoản thì nạp lại danh sách yêu thích của đúng tài khoản đó
  useEffect(() => {
    setWishlist(readWishlist(userId))
    loadedFor.current = userId
    // Dọn khoá dùng chung của phiên bản cũ để dữ liệu khách không còn sót lại
    localStorage.removeItem('bachhome_wishlist')
  }, [userId])

  useEffect(() => {
    if (!userId || loadedFor.current !== userId) return
    localStorage.setItem(storageKey(userId), JSON.stringify(wishlist))
  }, [wishlist, userId])

  const isWished = (id) => wishlist.some(i => i.id === id)

  const toggleWishlist = (item) => {
    if (!userId) {
      toast.error('Vui lòng đăng nhập để lưu sản phẩm yêu thích')
      navigate('/login')
      return false
    }
    setWishlist(prev => {
      if (prev.some(i => i.id === item.id)) {
        toast(`Đã bỏ "${item.name}" khỏi yêu thích`, { icon: '💔' })
        return prev.filter(i => i.id !== item.id)
      }
      toast.success(`Đã thêm "${item.name}" vào yêu thích`)
      return [...prev, item]
    })
    return true
  }

  const removeWishlist = (id) => {
    setWishlist(prev => prev.filter(i => i.id !== id))
  }

  const clearWishlist = () => setWishlist([])

  return (
    <WishlistContext.Provider value={{ wishlist, isWished, toggleWishlist, removeWishlist, clearWishlist }}>
      {children}
    </WishlistContext.Provider>
  )
}

export const useWishlist = () => useContext(WishlistContext)
