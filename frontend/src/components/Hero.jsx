import { Link } from 'react-router-dom'
import { ArrowRight, Star, Clock, ShieldCheck } from 'lucide-react'

export default function Hero() {
  return (
    <section className="relative pt-28 pb-12 lg:pt-32 lg:pb-16 overflow-hidden">
      {/* Background Shapes */}
      <div className="absolute top-0 right-0 -z-10 w-1/2 h-full bg-gradient-to-bl from-emerald-50 to-transparent rounded-bl-[100px] opacity-50"></div>
      <div className="absolute bottom-0 left-0 -z-10 w-64 h-64 bg-yellow-50 rounded-full blur-3xl opacity-60"></div>

      <div className="container mx-auto px-4">
        <div className="flex flex-col lg:flex-row items-center gap-12 lg:gap-20">
          
          {/* Text Content */}
          <div className="flex-1 text-center lg:text-left space-y-8 animate-in slide-in-from-left-10 duration-700">
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-100 text-emerald-600 rounded-full font-semibold text-sm">
              <span className="relative flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
              </span>
              Đồ gia dụng chính hãng số 1
            </div>

            <h1 className="text-5xl lg:text-7xl font-bold text-gray-900 leading-[1.1]">
              Nâng tầm <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-500 to-teal-600">không gian sống</span> <br/>
              Cho ngôi nhà của bạn
            </h1>

            <p className="text-lg text-gray-600 max-w-xl mx-auto lg:mx-0 leading-relaxed">
              Khám phá các sản phẩm gia dụng thông minh cho nhà bếp, điện gia dụng và dọn dẹp.
              Sản phẩm chính hãng, bảo hành uy tín và giao đến tận cửa nhà bạn.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4">
              <Link 
                to="/menu" 
                className="w-full sm:w-auto px-8 py-4 bg-gradient-to-r from-emerald-500 to-teal-600 text-white rounded-full font-bold text-lg shadow-lg shadow-emerald-500/30 hover:shadow-emerald-500/50 transform hover:-translate-y-1 transition-all duration-300 flex items-center justify-center gap-2"
              >
                Mua ngay <ArrowRight size={20} />
              </Link>
              <Link 
                to="/menu" 
                className="w-full sm:w-auto px-8 py-4 bg-white text-gray-700 border border-gray-200 rounded-full font-bold text-lg hover:bg-gray-50 hover:border-emerald-200 transition-all duration-300 flex items-center justify-center gap-2"
              >
                Xem sản phẩm
              </Link>
            </div>

            <div className="flex items-center justify-center lg:justify-start gap-8 pt-4">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-green-100 text-green-600 rounded-full">
                  <Clock size={20} />
                </div>
                <div className="text-left">
                  <p className="font-bold text-gray-900">30 Phút</p>
                  <p className="text-xs text-gray-500">Giao nhanh</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <div className="p-2 bg-yellow-100 text-yellow-600 rounded-full">
                  <Star size={20} />
                </div>
                <div className="text-left">
                  <p className="font-bold text-gray-900">4.9/5</p>
                  <p className="text-xs text-gray-500">Đánh giá</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <div className="p-2 bg-blue-100 text-blue-600 rounded-full">
                  <ShieldCheck size={20} />
                </div>
                <div className="text-left">
                  <p className="font-bold text-gray-900">100%</p>
                  <p className="text-xs text-gray-500">An toàn</p>
                </div>
              </div>
            </div>
          </div>

          {/* Image Content */}
          <div className="flex-1 relative animate-in slide-in-from-right-10 duration-700 delay-200">
            <div className="relative z-10 w-full max-w-lg mx-auto">
              <div className="absolute inset-0 bg-gradient-to-tr from-emerald-500 to-teal-600 rounded-full blur-3xl opacity-20"></div>
              <img
                src="https://images.unsplash.com/photo-1556911220-bff31c812dba?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                alt="Đồ gia dụng nhà bếp"
                className="relative w-full aspect-square object-cover rounded-full shadow-2xl border-8 border-white"
              />
              
              {/* Floating Cards */}
              <div className="absolute -bottom-10 -left-10 bg-white p-4 rounded-2xl shadow-xl">
                <div className="flex items-center gap-3">
                  <img
                    src="https://images.unsplash.com/photo-1583778176476-4a8b02a64c01?ixlib=rb-4.0.3&auto=format&fit=crop&w=100&q=80"
                    alt="Sản phẩm gia dụng"
                    className="w-12 h-12 rounded-full object-cover"
                  />
                  <div>
                    <p className="font-bold text-gray-800">Chính hãng 100%</p>
                    <div className="flex text-yellow-400 text-xs">★★★★★</div>
                  </div>
                </div>
              </div>

              <div className="absolute top-10 -right-5 bg-white p-3 rounded-2xl shadow-xl">
                <div className="flex items-center gap-2">
                  <span className="text-2xl">🛡️</span>
                  <span className="font-bold text-emerald-500">Bảo hành</span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>
  )
}
