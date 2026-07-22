import React, { createContext, useState, useContext, useEffect } from 'react'
import toast from 'react-hot-toast'

const WishlistContext = createContext()

const STORAGE_KEY = 'bachhome_wishlist'

export const WishlistProvider = ({ children }) => {
  const [wishlist, setWishlist] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      return saved ? JSON.parse(saved) : []
    } catch {
      return []
    }
  })

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(wishlist))
  }, [wishlist])

  const isWished = (id) => wishlist.some(i => i.id === id)

  const toggleWishlist = (item) => {
    setWishlist(prev => {
      if (prev.some(i => i.id === item.id)) {
        toast(`Đã bỏ "${item.name}" khỏi yêu thích`, { icon: '💔' })
        return prev.filter(i => i.id !== item.id)
      }
      toast.success(`Đã thêm "${item.name}" vào yêu thích`)
      return [...prev, item]
    })
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
