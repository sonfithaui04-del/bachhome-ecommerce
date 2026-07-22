# 🎛️ Admin Panel - Food Ordering System

## 📋 Overview

Premium admin dashboard for managing the Food Ordering System. Built with React, Vite, Tailwind CSS, and Recharts.

---

## ✨ Features

### 📊 **Dashboard**
- Real-time statistics (orders, revenue, users, menu items)
- Interactive charts (orders & revenue trends)
- Recent orders overview
- Visual analytics with Recharts

### 🍔 **Menu Management**
- ➕ Add new menu items
- ✏️ Edit existing items
- 🗑️ Delete items
- 📂 Assign categories
- 💰 Set prices
- ✅ Toggle availability

### 📂 **Category Management**
- Create food categories
- Edit category details
- Delete categories
- Activate/deactivate categories

### 📦 **Order Management**
- View all orders
- Filter by status
- Update order status
- View order details
- Track customer information

### 👥 **User Management**
- View all users
- See user roles (Admin/Staff/Customer)
- User information overview

---

## 🚀 Getting Started

### Installation

```bash
cd frontend-admin
npm install
```

### Run Development Server

```bash
npm run dev
```

Admin panel will run on: **http://localhost:3002**

### Build for Production

```bash
npm run build
```

---

## 🔐 Authentication

Admin panel requires **ADMIN role** to access.

**Test Credentials:**
- Email: `admin@foodorder.com`
- Password: `admin123` (or create an admin user in backend)
- Role: **ADMIN** (required)

---

## 📁 Project Structure

```
frontend-admin/
├── src/
│   ├── components/
│   │   └── Layout.jsx         # Admin layout with sidebar
│   ├── context/
│   │   └── AuthContext.jsx    # Admin authentication
│   ├── pages/
│   │   ├── Dashboard.jsx      # Stats & charts
│   │   ├── MenuManagement.jsx # CRUD menu items
│   │   ├── CategoryManagement.jsx
│   │   ├── OrderManagement.jsx
│   │   ├── UserManagement.jsx
│   │   └── LoginPage.jsx      # Admin login
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
├── package.json
├── vite.config.js
└── tailwind.config.js
```

---

## 🎨 Design Features

- **Indigo/Purple theme** - Premium admin aesthetic
- **Gradient backgrounds** - Modern visual appeal
- **Responsive tables** - Works on all devices
- **Interactive charts** - Data visualization
- **Modal dialogs** - Clean editing interface
- **Status badges** - Clear visual indicators
- **Sidebar navigation** - Easy access to all features

---

## 🔌 API Integration

Connects to backend via API Gateway:

### Menu Endpoints
- `GET /api/menu` - Get all menu items
- `POST /api/menu` - Create menu item
- `PUT /api/menu/:id` - Update menu item
- `DELETE /api/menu/:id` - Delete menu item

### Category Endpoints
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create category
- `PUT /api/categories/:id` - Update category
- `DELETE /api/categories/:id` - Delete category

### Order Endpoints
- `GET /api/orders` - Get all orders
- `PATCH /api/orders/:id/status` - Update order status

### Auth Endpoint
- `POST /api/auth/login` - Admin login (requires ADMIN role)

---

## 🛡️ Security

- JWT token-based authentication
- Admin role verification
- Protected routes
- Token stored in localStorage
- Automatic logout on token expiry

---

## 📊 Technologies

- **React 18** - UI framework
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **Recharts** - Charts & graphs
- **Axios** - HTTP client
- **React Router** - Navigation

---

## 🎯 Key Pages

### Dashboard (`/`)
- Statistics cards
- Weekly order chart (bar chart)
- Weekly revenue chart (line chart)
- Recent orders table

### Menu Management (`/menu`)
- Grid view of menu items
- Add/Edit modal form
- Price & availability controls
- Category assignment

### Category Management (`/categories`)
- Card grid view
- Quick add/edit
- Active status toggle

### Order Management (`/orders`)
- Filterable orders table
- Status update workflow
- Order details modal
- Customer information

### User Management (`/users`)
- User list with roles
- User information display

---

## 🎨 Color Scheme

```javascript
{
  primary: '#6366F1',    // Indigo
  secondary: '#10B981',  // Green
  danger: '#EF4444',     // Red
  warning: '#F59E0B'     // Amber
}
```

---

## 📝 Usage Tips

1. **Login** - Use admin credentials
2. **Dashboard** - Monitor system overview
3. **Add Categories** - Create categories before menu items
4. **Add Menu Items** - Assign to categories, set prices
5. **Manage Orders** - Update status as orders progress
6. **View Analytics** - Check charts for trends

---

## 🔄 Development Workflow

1. Start backend services: `docker-compose up -d`
2. Verify API Gateway is running: http://localhost:8080
3. Start admin panel: `npm run dev`
4. Access admin: http://localhost:3002
5. Login with admin credentials

---

## 🚀 Deployment

For production deployment:

```bash
npm run build
# Deploy 'dist' folder to your hosting
```

Update `vite.config.js` API proxy for production backend URL.

---

## ⚡ Performance

- Vite for fast development
- Code splitting enabled
- Optimized production builds
- Lazy loading ready
- Minimal bundle size

---

## 📈 Future Enhancements

- [ ] Image upload for menu items
- [ ] Bulk operations
- [ ] Export data (CSV/PDF)
- [ ] Advanced analytics
- [ ] Real-time notifications
- [ ] Multi-language support
- [ ] Dark mode
- [ ] Advanced filtering

---

## 🎉 Complete Feature List

✅ Admin authentication with role check  
✅ Dashboard with statistics & charts  
✅ Full CRUD for menu items  
✅ Full CRUD for categories  
✅ Order status management  
✅ User list viewing  
✅ Responsive design  
✅ Premium UI/UX  
✅ Modal-based editing  
✅ Status badges  
✅ Data visualization  

---

**Built with ❤️ for efficient restaurant management**

**Access:** http://localhost:3002
