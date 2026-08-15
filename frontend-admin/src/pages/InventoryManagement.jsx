import { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'
import { 
  Plus, Search, Edit, Trash2, 
  Package, AlertTriangle, Calendar, DollarSign,
  CheckCircle, XCircle, Loader2, Scale 
} from 'lucide-react'

export default function InventoryManagement() {
  const { getToken } = useAuth()
  const [inventoryItems, setInventoryItems] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingInventoryItem, setEditingInventoryItem] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')
  
  const [formData, setFormData] = useState({
    name: '',
    productId: '',
    unit: '',
    quantity: '',
    minQuantity: '',
    costPerUnit: '',
    expiryDate: '',
    description: '',
    active: true
  })

  useEffect(() => {
    loadInventoryItems()
    loadProducts()
  }, [])

  // Danh sách sản phẩm đang bán, dùng để gắn mặt hàng kho với sản phẩm
  const loadProducts = async () => {
    try {
      const token = getToken()
      const response = await axios.get('/api/products', {
        headers: { Authorization: `Bearer ${token}` }
      })
      const data = response.data
      setProducts(Array.isArray(data) ? data : (data?.content || []))
    } catch (error) {
      console.error('Failed to load products:', error)
      setProducts([])
    }
  }

  const loadInventoryItems = async () => {
    try {
      setLoading(true)
      const token = getToken()
      const response = await axios.get('/api/inventory-items', {
        headers: { Authorization: `Bearer ${token}` }
      })
      setInventoryItems(response.data || [])
    } catch (error) {
      console.error('Failed to load inventoryItems:', error)
      // Mock data if API fails
      setInventoryItems([]) 
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const token = getToken()
      const payload = {
        ...formData,
        productId: formData.productId ? parseInt(formData.productId, 10) : null,
        quantity: parseFloat(formData.quantity),
        minQuantity: formData.minQuantity ? parseFloat(formData.minQuantity) : null,
        costPerUnit: formData.costPerUnit ? parseFloat(formData.costPerUnit) : null,
        expiryDate: formData.expiryDate || null
      }

      if (editingInventoryItem) {
        await axios.put(`/api/inventory-items/${editingInventoryItem.id}`, payload, {
          headers: { Authorization: `Bearer ${token}` }
        })
      } else {
        await axios.post('/api/inventory-items', payload, {
          headers: { Authorization: `Bearer ${token}` }
        })
      }

      handleCloseModal()
      loadInventoryItems()
    } catch (error) {
      console.error('Failed to save inventoryItem:', error)
      alert(error.response?.data?.message || 'Không thể lưu mặt hàng')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa mặt hàng này?')) return
    
    try {
      const token = getToken()
      await axios.delete(`/api/inventory-items/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      loadInventoryItems()
    } catch (error) {
      console.error('Failed to delete inventoryItem:', error)
      alert('Xóa mặt hàng thất bại')
    }
  }

  const handleEdit = (inventoryItem) => {
    setEditingInventoryItem(inventoryItem)
    setFormData({
      name: inventoryItem.name,
      productId: inventoryItem.productId ?? '',
      unit: inventoryItem.unit,
      quantity: inventoryItem.quantity,
      minQuantity: inventoryItem.minQuantity || '',
      costPerUnit: inventoryItem.costPerUnit || '',
      expiryDate: inventoryItem.expiryDate || '',
      description: inventoryItem.description || '',
      active: inventoryItem.active
    })
    setShowModal(true)
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setEditingInventoryItem(null)
    setFormData({
      name: '',
      productId: '',
      unit: '',
      quantity: '',
      minQuantity: '',
      costPerUnit: '',
      expiryDate: '',
      description: '',
      active: true
    })
  }

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const filteredInventoryItems = inventoryItems.filter(item => 
    item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    item.description?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader2 className="w-12 h-12 text-blue-500 animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý tồn kho</h1>
          <p className="text-gray-500">Theo dõi kho, chi phí và mức tồn kho</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
        >
          <Plus size={20} />
          Thêm mặt hàng
        </button>
      </div>

      {/* Search */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm mặt hàng..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên hàng</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sản phẩm gắn kèm</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tồn kho</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đơn giá</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao tác</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredInventoryItems.map((inventoryItem) => (
                <tr key={inventoryItem.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mr-3">
                        <Package size={16} />
                      </div>
                      <div>
                        <div className="text-sm font-medium text-gray-900">{inventoryItem.name}</div>
                        <div className="text-xs text-gray-500">{inventoryItem.unit}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {inventoryItem.productId ? (
                      <span className="px-2 py-1 inline-flex text-xs font-medium rounded-full bg-blue-50 text-blue-700 border border-blue-200">
                        {products.find(p => p.id === inventoryItem.productId)?.name || `#${inventoryItem.productId}`}
                      </span>
                    ) : (
                      <span className="text-xs text-gray-400">Vật tư kho</span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-col">
                      <span className={`text-sm font-semibold ${
                        inventoryItem.isLowStock ? 'text-red-600' : 'text-gray-900'
                      }`}>
                        {inventoryItem.quantity} {inventoryItem.unit}
                      </span>
                      {inventoryItem.minQuantity && (
                        <span className="text-xs text-gray-500">Min: {inventoryItem.minQuantity}</span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {inventoryItem.costPerUnit ? `${inventoryItem.costPerUnit.toLocaleString('vi-VN')}đ` : '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      inventoryItem.active 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-gray-100 text-gray-800'
                    }`}>
                      {inventoryItem.active ? 'Hoạt động' : 'Tạm ngưng'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      onClick={() => handleEdit(inventoryItem)}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                    >
                      <Edit size={18} />
                    </button>
                    <button
                      onClick={() => handleDelete(inventoryItem.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        {filteredInventoryItems.length === 0 && (
          <div className="text-center py-12">
            <Package className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">Không tìm thấy mặt hàng</h3>
            <p className="mt-1 text-sm text-gray-500">Bắt đầu bằng cách thêm mặt hàng mới.</p>
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
              <h2 className="text-xl font-bold text-gray-900">
                {editingInventoryItem ? 'Sửa mặt hàng' : 'Thêm mặt hàng mới'}
              </h2>
              <button onClick={handleCloseModal} className="text-gray-400 hover:text-gray-600">
                <XCircle size={24} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Tên mặt hàng *</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                    placeholder="Ví dụ: Nồi cơm điện Cuckoo 1.8L"
                  />
                </div>

                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Sản phẩm tương ứng</label>
                  <select
                    name="productId"
                    value={formData.productId}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 bg-white"
                  >
                    <option value="">— Không gắn (vật tư kho) —</option>
                    {products.map(p => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                  <p className="mt-1 text-xs text-gray-500">
                    Gắn với sản phẩm để hệ thống tự trừ kho khi xác nhận đơn và hoàn kho khi hủy đơn.
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Đơn vị *</label>
                  <div className="relative">
                    <Scale className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input
                      type="text"
                      name="unit"
                      value={formData.unit}
                      onChange={handleChange}
                      required
                      className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                      placeholder="cái, chiếc, bộ..."
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Tồn kho hiện tại *</label>
                  <input
                    type="number"
                    step="0.01"
                    name="quantity"
                    value={formData.quantity}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Mức cảnh báo tối thiểu</label>
                  <div className="relative">
                    <AlertTriangle className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input
                      type="number"
                      step="0.01"
                      name="minQuantity"
                      value={formData.minQuantity}
                      onChange={handleChange}
                      className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                      placeholder="Ngưỡng cảnh báo"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Đơn giá</label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input
                      type="number"
                      step="0.01"
                      name="costPerUnit"
                      value={formData.costPerUnit}
                      onChange={handleChange}
                      className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                      placeholder="0.00"
                    />
                  </div>
                </div>

                <div className="col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows="3"
                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 resize-none"
                    placeholder="Thông tin thêm..."
                  />
                </div>
              </div>

              <div className="flex items-center p-3 bg-gray-50 rounded-lg border border-gray-100">
                <input
                  type="checkbox"
                  name="active"
                  id="active"
                  checked={formData.active}
                  onChange={handleChange}
                  className="w-5 h-5 text-blue-600 rounded focus:ring-blue-500 border-gray-300"
                />
                <label htmlFor="active" className="ml-2 block text-sm font-medium text-gray-700 cursor-pointer select-none">
                  Kích hoạt
                </label>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium shadow-sm"
                >
                  {editingInventoryItem ? 'Cập nhật' : 'Thêm mới'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
