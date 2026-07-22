import { useState, useEffect } from 'react'
import axios from 'axios'
import { useCart } from '../context/CartContext'
import { useWishlist } from '../context/WishlistContext'
import { Search, ShoppingBag, Star, Heart, Home, ArrowUpDown } from 'lucide-react'
import ProductModal from '../components/ProductModal'

const PRICE_RANGES = [
  { key: 'ALL', label: 'Mọi mức giá', min: 0, max: Infinity },
  { key: 'U500', label: 'Dưới 500k', min: 0, max: 500000 },
  { key: '500-1M', label: '500k - 1 triệu', min: 500000, max: 1000000 },
  { key: '1M-2M', label: '1 - 2 triệu', min: 1000000, max: 2000000 },
  { key: 'O2M', label: 'Trên 2 triệu', min: 2000000, max: Infinity },
]

const PAGE_SIZE = 15

function getPageNumbers(current, total) {
  const pages = []
  if (total <= 7) { for (let i = 1; i <= total; i++) pages.push(i); return pages }
  pages.push(1)
  if (current > 3) pages.push('...')
  for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) pages.push(i)
  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
}

export default function MenuPage() {
  const [menuItems, setMenuItems] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedCategory, setSelectedCategory] = useState('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [priceRange, setPriceRange] = useState('ALL')
  const [sortBy, setSortBy] = useState('default')
  const [selectedItemForModal, setSelectedItemForModal] = useState(null)
  const [page, setPage] = useState(1)
  const { addToCart } = useCart()
  const { isWished, toggleWishlist } = useWishlist()

  // Reset về trang 1 khi đổi bộ lọc / tìm kiếm
  useEffect(() => { setPage(1) }, [selectedCategory, searchTerm, priceRange, sortBy])

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [menuRes, catRes] = await Promise.all([
          axios.get('/api/menu?availableOnly=true'),
          axios.get('/api/categories?activeOnly=true')
        ])
        setMenuItems(menuRes.data || [])
        setCategories(catRes.data || [])
      } catch (error) {
        console.error('Failed to load data:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const activeRange = PRICE_RANGES.find(r => r.key === priceRange) || PRICE_RANGES[0]

  const filteredItems = menuItems
    .filter(item => {
      const matchesCategory = selectedCategory === 'ALL' || item.categoryId === selectedCategory
      const matchesSearch = item.name.toLowerCase().includes(searchTerm.toLowerCase())
      const price = parseFloat(item.price) || 0
      const matchesPrice = price >= activeRange.min && price < activeRange.max
      return matchesCategory && matchesSearch && matchesPrice
    })
    .sort((a, b) => {
      if (sortBy === 'price-asc') return (a.price || 0) - (b.price || 0)
      if (sortBy === 'price-desc') return (b.price || 0) - (a.price || 0)
      if (sortBy === 'rating') return (b.averageRating || 0) - (a.averageRating || 0)
      return 0
    })

  const totalPages = Math.max(1, Math.ceil(filteredItems.length / PAGE_SIZE))
  const currentPage = Math.min(page, totalPages)
  const displayed = filteredItems.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

  const goToPage = (p) => {
    if (p < 1 || p > totalPages) return
    setPage(p)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Header Section */}
      <div className="bg-gray-900 text-white pt-32 pb-12 rounded-b-[50px] mb-12 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-full h-full bg-[url('https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1920&q=80')] bg-cover bg-center opacity-20"></div>
        <div className="absolute inset-0 bg-gradient-to-b from-transparent to-gray-900/90"></div>

        <div className="container mx-auto px-4 relative z-10 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Sản Phẩm Nổi Bật</h1>
          <p className="text-gray-300 max-w-2xl mx-auto text-lg">
            Khám phá các sản phẩm gia dụng đa dạng, chính hãng. Từ đồ dùng nhà bếp đến điện gia dụng hiện đại.
          </p>

          {/* Search Bar */}
          <div className="mt-8 max-w-xl mx-auto relative">
            <input
              type="text"
              placeholder="Tìm kiếm sản phẩm..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-12 pr-4 py-4 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 transition-all"
            />
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 pb-20">
        {/* Category Filter */}
        <div className="flex flex-wrap justify-center gap-3 mb-6">
          <button
            onClick={() => setSelectedCategory('ALL')}
            className={`px-6 py-2.5 rounded-full font-medium transition-all duration-300 ${
              selectedCategory === 'ALL'
                ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/30 scale-105'
                : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'
            }`}
          >
            Tất cả
          </button>
          {categories.map(cat => (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`px-6 py-2.5 rounded-full font-medium transition-all duration-300 ${
                selectedCategory === cat.id
                  ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/30 scale-105'
                  : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              {cat.name}
            </button>
          ))}
        </div>

        {/* Filter & Sort Bar */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 mb-10 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-sm font-semibold text-gray-500 mr-1">Khoảng giá:</span>
            {PRICE_RANGES.map(r => (
              <button
                key={r.key}
                onClick={() => setPriceRange(r.key)}
                className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
                  priceRange === r.key
                    ? 'bg-emerald-100 text-emerald-700 ring-1 ring-emerald-300'
                    : 'bg-gray-50 text-gray-500 hover:bg-gray-100'
                }`}
              >
                {r.label}
              </button>
            ))}
          </div>
          <div className="flex items-center gap-2">
            <ArrowUpDown size={16} className="text-gray-400" />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="border border-gray-200 rounded-full px-4 py-2 text-sm font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-emerald-500 bg-white cursor-pointer"
            >
              <option value="default">Sắp xếp: Mặc định</option>
              <option value="price-asc">Giá: Thấp đến cao</option>
              <option value="price-desc">Giá: Cao đến thấp</option>
              <option value="rating">Đánh giá cao nhất</option>
            </select>
          </div>
        </div>

        {/* Result count */}
        {!loading && (
          <p className="text-sm text-gray-500 mb-6">{filteredItems.length} sản phẩm · Trang <span className="font-bold text-gray-800">{currentPage}</span>/{totalPages}</p>
        )}

        {/* Menu Grid */}
        {loading ? (
          <div className="flex justify-center py-20">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-emerald-500"></div>
          </div>
        ) : filteredItems.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-3xl shadow-sm border border-gray-100">
            <div className="bg-emerald-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
              <Home size={40} className="text-emerald-500" />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-2">Không tìm thấy sản phẩm nào</h3>
            <p className="text-gray-500">Vui lòng thử tìm kiếm hoặc chọn bộ lọc khác</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
            {displayed.map((item) => (
              <div key={item.id} className="bg-white rounded-3xl shadow-lg hover:shadow-2xl transition-all duration-300 group overflow-hidden border border-gray-100">
                <div className="relative h-56 overflow-hidden cursor-pointer" onClick={() => setSelectedItemForModal(item)}>
                  <img
                    src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=500&q=80'}
                    alt={item.name}
                    loading="lazy"
                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=500&q=80';
                    }}
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                     <span className="text-white font-bold tracking-wider opacity-0 group-hover:opacity-100 transform translate-y-4 group-hover:translate-y-0 transition-all duration-300">Xem chi tiết</span>
                  </div>

                  <button
                    onClick={(e) => { e.stopPropagation(); toggleWishlist(item); }}
                    title={isWished(item.id) ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'}
                    className={`absolute top-4 right-4 z-10 p-2.5 backdrop-blur-sm rounded-full transition-all shadow-sm ${
                      isWished(item.id) ? 'bg-white text-red-500' : 'bg-white/90 text-gray-400 hover:text-red-500 hover:bg-white'
                    }`}
                  >
                    <Heart size={20} className={isWished(item.id) ? 'fill-red-500' : ''} />
                  </button>
                </div>

                <div className="p-6">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="text-xl font-bold text-gray-900 line-clamp-1 group-hover:text-emerald-500 transition-colors cursor-pointer" onClick={() => setSelectedItemForModal(item)}>
                      {item.name}
                    </h3>
                    <div className="flex items-center gap-1 bg-yellow-50 px-2 py-1 rounded-lg">
                      <Star size={14} className="text-yellow-400 fill-yellow-400" />
                      <span className="text-sm font-bold text-gray-700">{item.averageRating ? item.averageRating.toFixed(1) : 'Mới'}</span>
                    </div>
                  </div>

                  <p className="text-gray-500 text-sm mb-6 line-clamp-2 h-10">
                    {item.description || 'Sản phẩm gia dụng chính hãng, chất lượng cao.'}
                  </p>

                  <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                    <div>
                      <span className="text-xs text-gray-400 font-medium uppercase tracking-wider">Giá</span>
                      <div className="text-2xl font-bold text-gray-900">
                        {item.price?.toLocaleString('vi-VN')}
                        <span className="text-sm text-gray-500 font-normal align-top">đ</span>
                      </div>
                    </div>

                    <button
                      onClick={() => addToCart(item)}
                      className="flex items-center gap-2 bg-gray-900 text-white px-5 py-3 rounded-xl hover:bg-emerald-500 transition-colors duration-300 font-medium shadow-lg shadow-gray-900/20 hover:shadow-emerald-500/30 transform active:scale-95"
                    >
                      <ShoppingBag size={18} />
                      Thêm
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {!loading && totalPages > 1 && (
          <div className="flex flex-wrap justify-center items-center gap-2 mt-12">
            <button
              onClick={() => goToPage(currentPage - 1)}
              disabled={currentPage === 1}
              className="px-4 h-10 rounded-xl border border-gray-200 bg-white text-gray-600 font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
            >
              Trước
            </button>
            {getPageNumbers(currentPage, totalPages).map((p, idx) => (
              p === '...' ? (
                <span key={`e${idx}`} className="px-2 text-gray-400">…</span>
              ) : (
                <button
                  key={p}
                  onClick={() => goToPage(p)}
                  className={`w-10 h-10 rounded-xl font-medium transition-all ${
                    p === currentPage
                      ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/30'
                      : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  {p}
                </button>
              )
            ))}
            <button
              onClick={() => goToPage(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="px-4 h-10 rounded-xl border border-gray-200 bg-white text-gray-600 font-medium disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
            >
              Sau
            </button>
          </div>
        )}
      </div>

      {/* Product Modal */}
      {selectedItemForModal && (
        <ProductModal
          item={selectedItemForModal}
          onClose={() => setSelectedItemForModal(null)}
          addToCart={addToCart}
        />
      )}
    </div>
  )
}
