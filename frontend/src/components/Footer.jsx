import { Link } from 'react-router-dom'
import { Facebook, Instagram, Twitter, MapPin, Phone, Mail, Home } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-emerald-50 text-gray-600">
      <div className="container mx-auto px-4 py-14">
        {/* Centered brand header */}
        <div className="text-center max-w-2xl mx-auto mb-12">
          <Link to="/" className="inline-flex items-center gap-2 mb-4">
            <div className="bg-emerald-500 p-2.5 rounded-2xl text-white shadow-lg shadow-emerald-500/30">
              <Home size={26} />
            </div>
            <span className="text-3xl font-extrabold text-gray-800">
              Bach<span className="text-emerald-500">Home</span>
            </span>
          </Link>
          <p className="text-gray-500 leading-relaxed">
            Đồ gia dụng thông minh cho ngôi nhà của bạn — chính hãng, chất lượng cao,
            bảo hành uy tín và giao hàng tận nơi nhanh chóng.
          </p>
          <div className="flex justify-center gap-3 pt-5">
            <a href="#" className="w-11 h-11 rounded-full bg-white shadow-sm flex items-center justify-center text-gray-500 hover:bg-emerald-500 hover:text-white transition-all"><Facebook size={20} /></a>
            <a href="#" className="w-11 h-11 rounded-full bg-white shadow-sm flex items-center justify-center text-gray-500 hover:bg-emerald-500 hover:text-white transition-all"><Instagram size={20} /></a>
            <a href="#" className="w-11 h-11 rounded-full bg-white shadow-sm flex items-center justify-center text-gray-500 hover:bg-emerald-500 hover:text-white transition-all"><Twitter size={20} /></a>
          </div>
        </div>

        {/* Columns on soft cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
          <div className="bg-white rounded-3xl p-6 shadow-sm">
            <h3 className="text-gray-800 text-lg font-bold mb-4">Liên kết nhanh</h3>
            <ul className="space-y-3">
              <li><Link to="/" className="hover:text-emerald-600 transition-colors">Trang chủ</Link></li>
              <li><Link to="/menu" className="hover:text-emerald-600 transition-colors">Sản phẩm</Link></li>
              <li><Link to="/wishlist" className="hover:text-emerald-600 transition-colors">Yêu thích</Link></li>
              <li><Link to="/" className="hover:text-emerald-600 transition-colors">Về chúng tôi</Link></li>
            </ul>
          </div>

          <div className="bg-white rounded-3xl p-6 shadow-sm">
            <h3 className="text-gray-800 text-lg font-bold mb-4">Danh mục</h3>
            <ul className="space-y-3">
              <li><Link to="/menu" className="hover:text-emerald-600 transition-colors">Đồ dùng nhà bếp</Link></li>
              <li><Link to="/menu" className="hover:text-emerald-600 transition-colors">Điện gia dụng</Link></li>
              <li><Link to="/menu" className="hover:text-emerald-600 transition-colors">Dụng cụ dọn dẹp</Link></li>
              <li><Link to="/menu" className="hover:text-emerald-600 transition-colors">Đồ dùng phòng tắm</Link></li>
            </ul>
          </div>

          <div className="bg-white rounded-3xl p-6 shadow-sm">
            <h3 className="text-gray-800 text-lg font-bold mb-4">Liên hệ</h3>
            <ul className="space-y-3">
              <li className="flex items-start gap-3"><MapPin className="text-emerald-500 mt-0.5 shrink-0" size={18} /><span>123 Đường Gia Dụng, Quận 1, TP.HCM</span></li>
              <li className="flex items-center gap-3"><Phone className="text-emerald-500 shrink-0" size={18} /><span>+84 123 456 789</span></li>
              <li className="flex items-center gap-3"><Mail className="text-emerald-500 shrink-0" size={18} /><span>contact@bachhome.com</span></li>
            </ul>
          </div>
        </div>

        <div className="mt-12 pt-8 border-t border-emerald-200/60 text-center text-gray-500 text-sm">
          <p>&copy; {new Date().getFullYear()} BachHome. Đã đăng ký bản quyền.</p>
        </div>
      </div>
    </footer>
  )
}
