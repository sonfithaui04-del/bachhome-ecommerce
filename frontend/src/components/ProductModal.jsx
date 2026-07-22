import React, { useState, useEffect } from 'react';
import api from '../services/apiClient';
import { X, Star, MessageSquare, Heart } from 'lucide-react';
import { useWishlist } from '../context/WishlistContext';

export default function ProductModal({ item, onClose, addToCart }) {
  const { isWished, toggleWishlist } = useWishlist();
  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(true);
  
  // Review form state
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (item) {
      fetchReviews();
    }
  }, [item]);

  const fetchReviews = async () => {
    try {
      setLoadingReviews(true);
      const res = await api.get(`/api/menu/reviews/item/${item.id}`);
      setReviews(res.data || []);
    } catch (err) {
      console.error("Error fetching reviews", err);
    } finally {
      setLoadingReviews(false);
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      alert("Vui lòng đăng nhập để đánh giá!");
      return;
    }
    const user = JSON.parse(userStr);

    try {
      setSubmitting(true);
      await api.post('/api/menu/reviews', 
        {
          menuItemId: item.id,
          rating,
          comment
        },
        {
          headers: {
            'X-User-Id': user.userId || user.id
          }
        }
      );
      // Reset form & refresh reviews
      setRating(5);
      setComment('');
      fetchReviews();
      alert("Cảm ơn bạn đã đánh giá!");
    } catch (err) {
      console.error(err);
      alert("Có lỗi xảy ra khi gửi đánh giá.");
    } finally {
      setSubmitting(false);
    }
  };

  if (!item) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white rounded-3xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col md:flex-row relative shadow-2xl">
        
        {/* Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 z-10 p-2 bg-gray-100 hover:bg-gray-200 rounded-full transition-colors"
        >
          <X size={24} className="text-gray-600" />
        </button>

        {/* Left Side: Image & Basic Info */}
        <div className="w-full md:w-1/2 bg-gray-50 flex flex-col relative h-64 md:h-auto">
          <img 
            src={item.imageUrl || 'https://images.unsplash.com/photo-1556911220-bff31c812dba'} 
            alt={item.name} 
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
          <div className="absolute bottom-0 left-0 p-6 text-white w-full">
            <h2 className="text-3xl font-bold mb-2">{item.name}</h2>
            <div className="flex items-center gap-4 text-sm font-medium">
              <span className="flex items-center gap-1 bg-white/20 px-3 py-1.5 rounded-full backdrop-blur-md">
                <Star size={16} className="text-yellow-400 fill-yellow-400" />
                {item.averageRating ? item.averageRating.toFixed(1) : 'Chưa có'}
              </span>
              <span className="flex items-center gap-1 bg-white/20 px-3 py-1.5 rounded-full backdrop-blur-md">
                <MessageSquare size={16} />
                {item.totalReviews || 0} đánh giá
              </span>
            </div>
          </div>
        </div>

        {/* Right Side: Details & Reviews */}
        <div className="w-full md:w-1/2 flex flex-col bg-white h-[50vh] md:h-auto overflow-y-auto">
          <div className="p-6 md:p-8 flex-1">
            <div className="flex items-end justify-between mb-4">
              <span className="text-3xl font-bold text-gray-900">
                {item.price?.toLocaleString('vi-VN')} <span className="text-lg text-gray-500 font-normal">đ</span>
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => toggleWishlist(item)}
                  title={isWished(item.id) ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'}
                  className={`p-3 rounded-full border transition-all active:scale-95 ${
                    isWished(item.id)
                      ? 'bg-red-50 border-red-200 text-red-500'
                      : 'bg-white border-gray-200 text-gray-400 hover:text-red-500 hover:border-red-200'
                  }`}
                >
                  <Heart size={20} className={isWished(item.id) ? 'fill-red-500' : ''} />
                </button>
                <button
                  onClick={() => { addToCart(item); onClose(); }}
                  className="bg-emerald-500 hover:bg-emerald-600 text-white px-6 py-3 rounded-full font-bold shadow-lg shadow-emerald-500/30 transition-all active:scale-95"
                >
                  Thêm vào giỏ
                </button>
              </div>
            </div>
            <p className="text-gray-600 leading-relaxed mb-8 border-b pb-8">{item.description}</p>

            {/* Reviews Section */}
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                Đánh giá từ khách hàng
              </h3>
              
              {/* Review Form */}
              <form onSubmit={handleSubmitReview} className="bg-gray-50 p-4 rounded-2xl mb-8 border border-gray-100">
                <div className="flex items-center gap-2 mb-3">
                  <span className="text-sm font-medium text-gray-700">Chấm điểm:</span>
                  <div className="flex gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button 
                        key={star}
                        type="button"
                        onClick={() => setRating(star)}
                        className="focus:outline-none transition-transform hover:scale-110"
                      >
                        <Star 
                          size={24} 
                          className={star <= rating ? "text-yellow-400 fill-yellow-400" : "text-gray-300"} 
                        />
                      </button>
                    ))}
                  </div>
                </div>
                <textarea 
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Chia sẻ cảm nhận của bạn về sản phẩm này..."
                  className="w-full p-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none resize-none h-24 mb-3 text-sm"
                  required
                ></textarea>
                <div className="flex justify-end">
                  <button 
                    type="submit" 
                    disabled={submitting}
                    className="bg-gray-900 hover:bg-black text-white px-5 py-2 rounded-lg text-sm font-medium transition-colors disabled:opacity-50"
                  >
                    {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
                  </button>
                </div>
              </form>

              {/* Review List */}
              <div className="space-y-4">
                {loadingReviews ? (
                  <p className="text-center text-gray-400 py-4">Đang tải đánh giá...</p>
                ) : reviews.length === 0 ? (
                  <p className="text-center text-gray-400 py-4 bg-gray-50 rounded-xl">Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
                ) : (
                  reviews.map(r => (
                    <div key={r.id} className="border-b last:border-0 pb-4 last:pb-0">
                      <div className="flex items-center gap-2 mb-1">
                        <div className="w-8 h-8 bg-gradient-to-tr from-emerald-400 to-pink-500 rounded-full flex items-center justify-center text-white font-bold text-xs">
                          U
                        </div>
                        <div>
                          <p className="text-sm font-bold text-gray-900">User #{r.userId}</p>
                          <div className="flex gap-0.5">
                            {[1, 2, 3, 4, 5].map((star) => (
                              <Star key={star} size={12} className={star <= r.rating ? "text-yellow-400 fill-yellow-400" : "text-gray-300"} />
                            ))}
                          </div>
                        </div>
                        <span className="ml-auto text-xs text-gray-400">
                          {new Date(r.createdAt).toLocaleDateString('vi-VN')}
                        </span>
                      </div>
                      <p className="text-gray-600 text-sm pl-10 leading-relaxed">{r.comment}</p>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
