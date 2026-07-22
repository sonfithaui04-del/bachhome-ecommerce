import { useState, useEffect } from 'react'
import axios from 'axios'
import { Link } from 'react-router-dom'
import Hero from '../components/Hero'
import { ArrowRight, Star, ShoppingBag, Heart, Clock } from 'lucide-react'
import { useCart } from '../context/CartContext'

export default function HomePage() {
  const [featuredItems, setFeaturedItems] = useState([])
  const [loading, setLoading] = useState(true)
  const { addToCart } = useCart()

  useEffect(() => {
    const fetchFeatured = async () => {
      try {
        const res = await axios.get('/api/menu?availableOnly=true')
        // Get top 4 items
        setFeaturedItems(res.data.slice(0, 4))
      } catch (error) {
        console.error('Failed to fetch menu:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchFeatured()
  }, [])

  const categories = [
    { id: 1, name: 'Đồ dùng nhà bếp', image: 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=300&q=80', count: '12 Sản phẩm' },
    { id: 2, name: 'Điện gia dụng', image: 'https://images.unsplash.com/photo-1585659722983-3a675dabf23d?auto=format&fit=crop&w=300&q=80', count: '8 Sản phẩm' },
    { id: 3, name: 'Dụng cụ dọn dẹp', image: 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=300&q=80', count: '15 Sản phẩm' },
    { id: 4, name: 'Đồ dùng phòng tắm', image: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=300&q=80', count: '10 Sản phẩm' },
  ]

  return (
    <div className="min-h-screen bg-gray-50">
      <Hero />

      {/* Categories Section */}
      <section className="py-12 container mx-auto px-4">
        <div className="text-center mb-12">
          <span className="text-emerald-500 font-bold tracking-wider uppercase text-sm">Danh mục</span>
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mt-2">Khám phá sản phẩm</h2>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {categories.map((cat) => (
            <Link 
              key={cat.id} 
              to="/menu"
              className="group relative overflow-hidden rounded-2xl shadow-lg aspect-[4/5] hover:-translate-y-2 transition-all duration-300"
            >
              <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent z-10"></div>
              <img 
                src={cat.image} 
                alt={cat.name} 
                className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
              />
              <div className="absolute bottom-0 left-0 p-6 z-20 text-white">
                <h3 className="text-xl font-bold mb-1">{cat.name}</h3>
                <p className="text-sm text-gray-300 group-hover:text-emerald-400 transition-colors">{cat.count}</p>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Items Section */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-end mb-12">
            <div>
              <span className="text-emerald-500 font-bold tracking-wider uppercase text-sm">Phổ biến</span>
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mt-2">Sản phẩm được yêu thích</h2>
            </div>
            <Link to="/menu" className="hidden md:flex items-center gap-2 text-emerald-500 font-bold hover:text-emerald-600 transition-colors">
              Xem tất cả <ArrowRight size={20} />
            </Link>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              {[1,2,3,4].map(i => (
                <div key={i} className="bg-gray-100 rounded-2xl h-80 animate-pulse"></div>
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              {featuredItems.map((item) => (
                <div key={item.id} className="bg-white rounded-2xl shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden group">
                  <div className="relative h-48 overflow-hidden">
                    <img 
                      src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=500&q=80'} 
                      alt={item.name} 
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=500&q=80';
                      }}
                    />
                    <button className="absolute top-3 right-3 p-2 bg-white/80 backdrop-blur-sm rounded-full text-gray-400 hover:text-red-500 hover:bg-white transition-colors">
                      <Heart size={18} />
                    </button>
                    <div className="absolute bottom-3 left-3 px-2 py-1 bg-white/90 backdrop-blur-sm rounded-lg text-xs font-bold text-gray-800 flex items-center gap-1">
                      <Clock size={12} /> Giao nhanh
                    </div>
                  </div>
                  
                  <div className="p-5">
                    <div className="flex justify-between items-start mb-2">
                      <h3 className="font-bold text-gray-900 text-lg line-clamp-1">{item.name}</h3>
                      <div className="flex items-center gap-1 text-yellow-400 text-sm font-bold">
                        <Star size={14} fill="currentColor" /> {item.averageRating ? item.averageRating.toFixed(1) : 'Mới'}
                      </div>
                    </div>
                    
                    <p className="text-gray-500 text-sm mb-4 line-clamp-2 h-10">{item.description}</p>
                    
                    <div className="flex items-center justify-between mt-4">
                      <span className="text-xl font-bold text-gray-900">
                        {(item.price || 0).toLocaleString('vi-VN')}đ
                      </span>
                      <button 
                        onClick={() => addToCart(item)}
                        className="p-3 bg-emerald-100 text-emerald-600 rounded-xl hover:bg-emerald-500 hover:text-white transition-all duration-300"
                      >
                        <ShoppingBag size={20} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
          
          <div className="mt-12 text-center md:hidden">
            <Link to="/menu" className="inline-flex items-center gap-2 px-6 py-3 bg-emerald-500 text-white rounded-full font-bold shadow-lg shadow-emerald-500/30">
              Xem tất cả <ArrowRight size={20} />
            </Link>
          </div>
        </div>
      </section>

      {/* Why Choose Us */}
      <section className="py-20 bg-gray-900 text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-emerald-500 rounded-full blur-3xl opacity-20"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-teal-500 rounded-full blur-3xl opacity-20"></div>

        <div className="container mx-auto px-4 relative z-10">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">Tại sao chọn chúng tôi?</h2>
            <p className="text-gray-400 max-w-2xl mx-auto">Chúng tôi không chỉ bán đồ gia dụng, chúng tôi mang đến sự tiện nghi cho ngôi nhà bạn. Đây là lý do hàng ngàn khách hàng tin tưởng.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              { title: 'Giao hàng nhanh', desc: 'Giao hàng tận nơi nhanh chóng trên toàn quốc.', icon: '🚀' },
              { title: 'Bảo hành chính hãng', desc: 'Sản phẩm chính hãng, bảo hành uy tín dài hạn.', icon: '🛡️' },
              { title: 'Đổi trả dễ dàng', desc: 'Chính sách đổi trả linh hoạt trong 30 ngày.', icon: '🔄' }
            ].map((item, idx) => (
              <div key={idx} className="bg-gray-800/50 backdrop-blur-sm p-8 rounded-2xl border border-gray-700 hover:border-emerald-500/50 transition-colors duration-300">
                <div className="text-5xl mb-6">{item.icon}</div>
                <h3 className="text-xl font-bold mb-3">{item.title}</h3>
                <p className="text-gray-400">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
