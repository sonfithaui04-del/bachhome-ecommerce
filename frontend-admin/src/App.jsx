import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import Dashboard from './pages/Dashboard'
import ProductManagement from './pages/ProductManagement'
import CategoryManagement from './pages/CategoryManagement'
import OrderManagement from './pages/OrderManagement'
import UserManagement from './pages/UserManagement'
import InventoryManagement from './pages/InventoryManagement'

function ProtectedRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" />
}

function App() {
  const { user } = useAuth()

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    )
  }

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/products" element={<ProtectedRoute><ProductManagement /></ProtectedRoute>} />
        <Route path="/categories" element={<ProtectedRoute><CategoryManagement /></ProtectedRoute>} />
        <Route path="/orders" element={<ProtectedRoute><OrderManagement /></ProtectedRoute>} />
        <Route path="/users" element={<ProtectedRoute><UserManagement /></ProtectedRoute>} />
        <Route path="/inventoryItems" element={<ProtectedRoute><InventoryManagement /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Layout>
  )
}

export default App
