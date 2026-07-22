# 🎯 FRONTEND TODOLIST - React App for Food Ordering System

## 📋 CONTEXT (Cho conversation mới)

**Project:** Food Ordering System Microservices  
**Backend:** 100% Complete - 6 microservices running on Docker  
**Frontend:** Đã setup cơ bản, cần implement full React app  
**Location:** `/food-ordering/frontend/`  
**Tech Stack:** React 18 + Vite + TailwindCSS + React Router + Axios

---

## ✅ COMPLETED (Đã xong)

- [x] Created folder structure: `frontend/`
- [x] Created `package.json` with dependencies
- [x] Created `vite.config.js` with proxy to API Gateway
- [x] Created `index.html` entry point
- [x] Created `REACT_APP_GUIDE.md` with full code examples

---

## ⏳ TODO - Files to Create (20 files)

### 1. Configuration Files (3 files)

#### □ `tailwind.config.js`
```javascript
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#3B82F6',
        secondary: '#10B981'
      }
    }
  },
  plugins: []
}
```

#### □ `postcss.config.js`
```javascript
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {}
  }
}
```

#### □ `.gitignore`
```
node_modules
dist
.env
.DS_Store
```

---

### 2. Core Files (3 files)

#### □ `src/main.jsx`
```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <App />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
)
```

#### □ `src/App.jsx`
```jsx
import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MenuPage from './pages/MenuPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import MyOrdersPage from './pages/MyOrdersPage'

function App() {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Navbar />
      <main className="flex-grow">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/menu" element={<MenuPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/my-orders" element={<MyOrdersPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}

export default App
```

#### □ `src/index.css`
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

body {
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  margin: 0;
  padding: 0;
}
```

---

### 3. Services (4 files)

#### □ `src/services/authService.js`
```javascript
import axios from 'axios'

const API_URL = '/api/auth'

export const authService = {
  async register(userData) {
    const response = await axios.post(`${API_URL}/register`, userData)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data))
    }
    return response.data
  },

  async login(credentials) {
    const response = await axios.post(`${API_URL}/login`, credentials)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data))
    }
    return response.data
  },

  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  getCurrentUser() {
    return JSON.parse(localStorage.getItem('user'))
  },

  getToken() {
    return localStorage.getItem('token')
  }
}
```

#### □ `src/services/menuService.js`
```javascript
import axios from 'axios'

const API_URL = '/api'

export const menuService = {
  async getMenu() {
    const response = await axios.get(`${API_URL}/menu?availableOnly=true`)
    return response.data
  },

  async getCategories() {
    const response = await axios.get(`${API_URL}/categories?activeOnly=true`)
    return response.data
  }
}
```

#### □ `src/services/orderService.js`
```javascript
import axios from 'axios'
import { authService } from './authService'

const API_URL = '/api/orders'

export const orderService = {
  async createOrder(orderData) {
    const token = authService.getToken()
    const response = await axios.post(API_URL, orderData, {
      headers: { Authorization: `Bearer ${token}` }
    })
    return response.data
  },

  async getMyOrders(userId) {
    const token = authService.getToken()
    const response = await axios.get(`${API_URL}/user/${userId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    return response.data
  }
}
```

#### □ `src/services/paymentService.js`
```javascript
import axios from 'axios'

const API_URL = '/api/payments'

export const paymentService = {
  async processPayment(paymentData) {
    const response = await axios.post(API_URL, paymentData)
    return response.data
  }
}
```

---

### 4. Context (2 files)

#### □ `src/context/AuthContext.jsx`
```jsx
import React, { createContext, useState, useContext, useEffect } from 'react'
import { authService } from '../services/authService'

const AuthContext = createContext()

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const currentUser = authService.getCurrentUser()
    setUser(currentUser)
    setLoading(false)
  }, [])

  const login = async (credentials) => {
    const data = await authService.login(credentials)
    setUser(data)
    return data
  }

  const register = async (userData) => {
    const data = await authService.register(userData)
    setUser(data)
    return data
  }

  const logout = () => {
    authService.logout()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
```

#### □ `src/context/CartContext.jsx`
```jsx
import React, { createContext, useState, useContext } from 'react'

const CartContext = createContext()

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState([])

  const addToCart = (item) => {
    setCart(prev => {
      const existing = prev.find(i => i.id === item.id)
      if (existing) {
        return prev.map(i => 
          i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i
        )
      }
      return [...prev, { ...item, quantity: 1 }]
    })
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

  const getTotal = () => {
    return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  }

  return (
    <CartContext.Provider value={{ 
      cart, addToCart, removeFromCart, updateQuantity, clearCart, getTotal 
    }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
```

---

### 5. Components (2 files)

#### □ `src/components/Navbar.jsx`
```jsx
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const { cart } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <nav className="bg-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="text-2xl font-bold text-blue-600">
            🍔 FoodOrder
          </Link>
          
          <div className="flex gap-6 items-center">
            <Link to="/menu" className="hover:text-blue-600">Menu</Link>
            
            {user ? (
              <>
                <Link to="/my-orders" className="hover:text-blue-600">My Orders</Link>
                <Link to="/cart" className="relative hover:text-blue-600">
                  🛒 Cart
                  {cart.length > 0 && (
                    <span className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 text-xs flex items-center justify-center">
                      {cart.length}
                    </span>
                  )}
                </Link>
                <button onClick={handleLogout} className="hover:text-blue-600">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="hover:text-blue-600">Login</Link>
                <Link to="/register" className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
```

#### □ `src/components/Footer.jsx`
```jsx
export default function Footer() {
  return (
    <footer className="bg-gray-800 text-white py-8 mt-20">
      <div className="container mx-auto px-4 text-center">
        <p>&copy; 2025 Food Ordering System. All rights reserved.</p>
        <p className="mt-2 text-gray-400">Microservices Architecture Demo</p>
      </div>
    </footer>
  )
}
```

---

### 6. Pages (7 files)

#### □ `src/pages/HomePage.jsx`
- Hero section
- Features showcase
- Call to action

#### □ `src/pages/LoginPage.jsx`
- Login form
- Integration với Auth Service

#### □ `src/pages/RegisterPage.jsx`
- Registration form
- Validation

#### □ `src/pages/MenuPage.jsx`
- Display menu items from Menu Service
- Add to cart functionality

#### □ `src/pages/CartPage.jsx`
- Show cart items
- Update quantities
- Proceed to checkout

#### □ `src/pages/CheckoutPage.jsx`
- Delivery info form
- Create order
- Process payment

#### □ `src/pages/MyOrdersPage.jsx`
- Display user's orders from Order Service
- Order status

---

## 🚀 INSTALLATION STEPS

```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Run dev server
npm run dev
```

**Access:** http://localhost:3000

---

## 📝 NOTES FOR NEW CONVERSATION

**What to say to start:**
"Tôi cần tiếp tục tạo React frontend cho Food Ordering System. Backend đã chạy đầy đủ 6 microservices. Hãy đọc file FRONTEND_TODOLIST.md và tiếp tục tạo các files còn lại."

**Current Status:**
- Backend: 100% (All 6 services running on Docker)
- Frontend: 20% (Setup + configs done, need to create components/pages)
- Token used in previous conversation: ~100k

**Priority Order:**
1. Core files (main.jsx, App.jsx, index.css)
2. Services (API integration)
3. Context (State management)
4. Components (Navbar, Footer)
5. Pages (starting with HomePage, LoginPage, MenuPage)

---

## ✅ DEFINITION OF DONE

- [ ] All 20 files created
- [ ] `npm install` runs successfully
- [ ] `npm run dev` starts app on port 3000
- [ ] Can register user via Auth Service
- [ ] Can login and see JWT token stored
- [ ] Can browse menu from Menu Service
- [ ] Can add items to cart
- [ ] Can create order via Order Service
- [ ] Can view order history
- [ ] Responsive design works

---

## 🔗 Backend APIs Available

- **Auth:** http://localhost:8080/api/auth/*
- **Menu:** http://localhost:8080/api/menu & /categories
- **Orders:** http://localhost:8080/api/orders/*
- **Payments:** http://localhost:8080/api/payments
- **Inventory:** http://localhost:8080/api/inventory/*
- **Notifications:** http://localhost:8080/api/notifications

All proxied through Vite to avoid CORS issues.

---

**START NEW CONVERSATION WITH THIS TODOLIST TO CONTINUE!**
