import { Link } from 'react-router-dom'
import { useWishlist } from '../context/WishlistContext'
import { useCart } from '../context/CartContext'
import { Heart, ShoppingBag, Trash2, Star, ArrowRight } from 'lucide-react'

export default function WishlistPage() {
  const { wishlist, removeWishlist, clearWishlist } = useWishlist()
  const { addToCart } = useCart()

  return (
    <div className="min-h-screen bg-gray-50 pt-32 pb-20">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between mb-10">
          <div>
            <span className="text-emerald-500 font-bold tracking-wider uppercase text-sm">BachHome</span>
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mt-1 flex items-center gap-3">
              <Heart className="text-emerald-500 fill-emerald-500" size={32} />
              Sản phẩm yêu thích
            </h1>
            <p className="text-gray-500 mt-2">{wishlist.length} sản phẩm trong danh sách của bạn</p>
          </div>
          {wishlist.length > 0 && (
            <button
              onClick={clearWishlist}
              className="text-sm text-gray-500 hover:text-red-500 font-medium flex items-center gap-2 transition-colors"
            >
              <Trash2 size={16} /> Xóa tất cả
            </button>
          )}
        </div>

        {wishlist.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-3xl shadow-sm border border-gray-100">
            <div className="bg-emerald-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
              <Heart size={40} className="text-emerald-500" />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-2">Chưa có sản phẩm yêu thích</h3>
            <p className="text-gray-500 mb-6">Hãy khám phá và lưu lại những sản phẩm bạn thích nhé!</p>
            <Link
              to="/menu"
              className="inline-flex items-center gap-2 px-6 py-3 bg-emerald-500 text-white rounded-full font-bold shadow-lg shadow-emerald-500/30 hover:bg-emerald-600 transition-colors"
            >
              Khám phá sản phẩm <ArrowRight size={20} />
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
            {wishlist.map((item) => (
              <div key={item.id} className="bg-white rounded-3xl shadow-lg hover:shadow-2xl transition-all duration-300 group overflow-hidden border border-gray-100">
                <div className="relative h-56 overflow-hidden">
                  <img
                    src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=500&q=80'}
                    alt={item.name}
                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                    onError={(e) => { e.target.onerror = null; e.target.src = 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=500&q=80' }}
                  />
                  <button
                    onClick={() => removeWishlist(item.id)}
                    title="Bỏ khỏi yêu thích"
                    className="absolute top-4 right-4 z-10 p-2.5 bg-white/90 backdrop-blur-sm rounded-full text-red-500 hover:bg-white transition-all shadow-sm"
                  >
                    <Heart size={20} className="fill-red-500" />
                  </button>
                </div>

                <div className="p-6">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="text-xl font-bold text-gray-900 line-clamp-1">{item.name}</h3>
                    <div className="flex items-center gap-1 bg-yellow-50 px-2 py-1 rounded-lg">
                      <Star size={14} className="text-yellow-400 fill-yellow-400" />
                      <span className="text-sm font-bold text-gray-700">{item.averageRating ? item.averageRating.toFixed(1) : 'Mới'}</span>
                    </div>
                  </div>
                  <p className="text-gray-500 text-sm mb-6 line-clamp-2 h-10">
                    {item.description || 'Sản phẩm gia dụng chính hãng, chất lượng cao.'}
                  </p>
                  <div className="flex items-center justify-between pt-4 border-t border-gray-100">
                    <div className="text-2xl font-bold text-gray-900">
                      {item.price?.toLocaleString('vi-VN')}<span className="text-sm text-gray-500 font-normal align-top">đ</span>
                    </div>
                    <button
                      onClick={() => addToCart(item)}
                      className="flex items-center gap-2 bg-gray-900 text-white px-5 py-3 rounded-xl hover:bg-emerald-500 transition-colors duration-300 font-medium shadow-lg shadow-gray-900/20 hover:shadow-emerald-500/30 transform active:scale-95"
                    >
                      <ShoppingBag size={18} /> Thêm
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
