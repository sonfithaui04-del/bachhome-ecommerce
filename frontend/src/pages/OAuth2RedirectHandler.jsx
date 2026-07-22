import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authService } from '../services/authService';
import toast from 'react-hot-toast';

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');

        if (token) {
            try {
                // Decode JWT to get user info (Simple base64 decode of payload)
                const payload = JSON.parse(atob(token.split('.')[1]));
                
                const userData = {
                    token: token,
                    tokenType: 'Bearer',
                    userId: payload.userId,
                    email: payload.sub,
                    role: payload.role,
                    fullName: payload.sub.split('@')[0] // Fallback if name not in token
                };

                localStorage.setItem('token', token);
                localStorage.setItem('user', JSON.stringify(userData));

                toast.success('Đăng nhập Google thành công! 🎉');
                
                // Reload home/menu to refresh context
                window.location.href = '/menu';
            } catch (error) {
                console.error('Failed to parse OAuth2 token', error);
                toast.error('Lỗi xác thực Google');
                navigate('/login');
            }
        } else {
            navigate('/login');
        }
    }, [location, navigate]);

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="text-center">
                <div className="w-16 h-16 border-4 border-emerald-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                <p className="text-gray-600 font-medium">Đang xác thực bảo mật...</p>
            </div>
        </div>
    );
};

export default OAuth2RedirectHandler;
