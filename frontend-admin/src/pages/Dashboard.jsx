import { useState, useEffect, useMemo } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area, PieChart, Pie, Cell } from 'recharts'
import { ShoppingBag, DollarSign, Package, Clock, TrendingUp, TrendingDown, AlertTriangle, Trophy, Hourglass, Receipt } from 'lucide-react'

const ACCENT = '#10B981'   // emerald
const ACCENT2 = '#14B8A6'  // teal

const STATUS_META = {
  PENDING:    { label: 'Chờ xác nhận', color: '#F59E0B' },
  CONFIRMED:  { label: 'Đã xác nhận', color: '#3B82F6' },
  PREPARING:  { label: 'Đang chuẩn bị', color: '#8B5CF6' },
  DELIVERING: { label: 'Đang giao', color: '#14B8A6' },
  COMPLETED:  { label: 'Hoàn thành', color: '#22C55E' },
  DELIVERED:  { label: 'Đã giao', color: '#22C55E' },
  CANCELLED:  { label: 'Đã huỷ', color: '#EF4444' },
}
const REVENUE_STATUSES = ['COMPLETED', 'DELIVERED']
const PENDING_STATUSES = ['PENDING', 'CONFIRMED', 'PREPARING', 'DELIVERING']

const fmtMoney = (v) => `${(v || 0).toLocaleString('vi-VN')}đ`

export default function Dashboard() {
  const { getToken } = useAuth()
  const [orders, setOrders] = useState([])
  const [recentOrders, setRecentOrders] = useState([])
  const [lowStock, setLowStock] = useState([])
  const [counts, setCounts] = useState({ users: 0, menu: 0 })
  const [menuItems, setMenuItems] = useState([])
  const [ingredients, setIngredients] = useState([])
  const [loading, setLoading] = useState(true)
  const [timeFilter, setTimeFilter] = useState('day')

  useEffect(() => {
    loadDashboardData()
    const handler = () => loadDashboardData()
    window.addEventListener('notification_received', handler)
    return () => window.removeEventListener('notification_received', handler)
  }, [])

  const loadDashboardData = async () => {
    try {
      const headers = { Authorization: `Bearer ${getToken()}` }
      const [menuRes, usersRes, ordersRes, ingRes] = await Promise.allSettled([
        axios.get('/api/menu?availableOnly=false', { headers }),
        axios.get('/api/users', { headers }),
        axios.get('/api/orders', { headers }),
        axios.get('/api/ingredients', { headers }),
      ])

      const allOrders = ordersRes.status === 'fulfilled' ? (ordersRes.value.data || []) : []
      const ingredients = ingRes.status === 'fulfilled' ? (ingRes.value.data || []) : []

      setOrders(allOrders)
      setMenuItems(menuRes.status === 'fulfilled' ? (menuRes.value.data || []) : [])
      setIngredients(ingredients)
      setRecentOrders([...allOrders].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 5))
      setLowStock(ingredients.filter(i => i.isLowStock || (i.minQuantity != null && Number(i.quantity) <= Number(i.minQuantity))))
      setCounts({
        users: usersRes.status === 'fulfilled' ? (usersRes.value.data?.length || 0) : 0,
        menu: menuRes.status === 'fulfilled' ? (menuRes.value.data?.length || 0) : 0,
      })
    } catch (e) {
      console.error('Dashboard load error', e)
    } finally {
      setLoading(false)
    }
  }

  // ===== Derived business metrics =====
  const metrics = useMemo(() => {
    const isRevenue = (o) => REVENUE_STATUSES.includes(o.status)
    const totalRevenue = orders.filter(isRevenue).reduce((s, o) => s + (o.totalAmount || 0), 0)
    const paidCount = orders.filter(isRevenue).length
    const aov = paidCount ? totalRevenue / paidCount : 0
    const pending = orders.filter(o => PENDING_STATUSES.includes(o.status)).length

    // growth: last 30 days vs previous 30 days
    const now = Date.now(), DAY = 86400000
    const inRange = (o, from, to) => {
      const t = new Date(o.createdAt).getTime()
      return t >= from && t < to
    }
    const revIn = (from, to) => orders.filter(o => isRevenue(o) && inRange(o, from, to)).reduce((s, o) => s + (o.totalAmount || 0), 0)
    const ordIn = (from, to) => orders.filter(o => inRange(o, from, to)).length
    const revCur = revIn(now - 30 * DAY, now + DAY), revPrev = revIn(now - 60 * DAY, now - 30 * DAY)
    const ordCur = ordIn(now - 30 * DAY, now + DAY), ordPrev = ordIn(now - 60 * DAY, now - 30 * DAY)
    const growth = (cur, prev) => prev > 0 ? ((cur - prev) / prev) * 100 : (cur > 0 ? 100 : 0)

    // status breakdown
    const sc = {}
    orders.forEach(o => { sc[o.status] = (sc[o.status] || 0) + 1 })
    const statusData = Object.entries(sc).map(([k, v]) => ({ name: STATUS_META[k]?.label || k, value: v, color: STATUS_META[k]?.color || '#9CA3AF' }))

    // top products from order items
    const counter = {}
    orders.forEach(o => (o.items || []).forEach(it => {
      const n = it.menuItemName || `#${it.menuItemId}`
      counter[n] = (counter[n] || 0) + (it.quantity || 0)
    }))
    const topProducts = Object.entries(counter).map(([name, qty]) => ({ name, qty })).sort((a, b) => b.qty - a.qty).slice(0, 5)
    const maxQty = topProducts[0]?.qty || 1

    // Lợi nhuận ước tính (giá vốn lấy từ kho theo tên; nếu không có thì ước tính 75% giá bán)
    const costByName = {}
    ingredients.forEach(g => { if (g.costPerUnit != null) costByName[(g.name || '').trim().toLowerCase()] = Number(g.costPerUnit) })
    let estCost = 0
    orders.filter(isRevenue).forEach(o => (o.items || []).forEach(it => {
      const c = costByName[(it.menuItemName || '').trim().toLowerCase()]
      const unitCost = c != null ? c : (it.price || 0) * 0.75
      estCost += unitCost * (it.quantity || 0)
    }))
    const estProfit = totalRevenue - estCost
    const estMargin = totalRevenue > 0 ? (estProfit / totalRevenue) * 100 : 0

    // Doanh thu theo danh mục
    const catOf = {}
    menuItems.forEach(m => { catOf[m.id] = m.categoryName || 'Khác' })
    const catRev = {}
    orders.filter(isRevenue).forEach(o => (o.items || []).forEach(it => {
      const cat = catOf[it.menuItemId] || 'Khác'
      catRev[cat] = (catRev[cat] || 0) + (it.subtotal || (it.price || 0) * (it.quantity || 0))
    }))
    const revenueByCategory = Object.entries(catRev).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
    const maxCatRev = revenueByCategory[0]?.value || 1

    // Doanh thu theo phương thức thanh toán
    const payLabels = { COD: 'Tiền mặt (COD)', SEPAY: 'Chuyển khoản QR' }
    const payRev = {}
    orders.filter(isRevenue).forEach(o => { const k = o.paymentMethod || 'KHÁC'; payRev[k] = (payRev[k] || 0) + (o.totalAmount || 0) })
    const revenueByPayment = Object.entries(payRev).map(([k, value]) => ({ name: payLabels[k] || k, value }))

    // Tỷ lệ đơn
    const total = orders.length
    const completedRate = total ? (orders.filter(isRevenue).length / total) * 100 : 0
    const cancelRate = total ? (orders.filter(o => o.status === 'CANCELLED').length / total) * 100 : 0

    return { totalRevenue, aov, pending, revGrowth: growth(revCur, revPrev), ordGrowth: growth(ordCur, ordPrev), statusData, topProducts, maxQty,
      estProfit, estMargin, revenueByCategory, maxCatRev, revenueByPayment, completedRate, cancelRate }
  }, [orders, menuItems, ingredients])

  // ===== Time-series chart data =====
  const chartData = useMemo(() => {
    const now = new Date(), currentYear = now.getFullYear()
    const isRev = (o) => REVENUE_STATUSES.includes(o.status)
    const build = (keys, keyOf) => {
      const map = new Map(keys.map(k => [k.key, { name: k.name, orders: 0, revenue: 0 }]))
      orders.forEach(o => {
        const k = keyOf(new Date(o.createdAt), o)
        if (map.has(k)) { const e = map.get(k); e.orders += 1; e.revenue += isRev(o) ? (o.totalAmount || 0) : 0 }
      })
      return Array.from(map.values())
    }
    if (timeFilter === 'day') {
      const keys = []
      for (let i = 6; i >= 0; i--) { const d = new Date(now); d.setDate(now.getDate() - i); keys.push({ key: d.toISOString().split('T')[0], name: d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) }) }
      return build(keys, (d) => d.toISOString().split('T')[0])
    }
    if (timeFilter === 'month') {
      const keys = Array.from({ length: 12 }, (_, i) => ({ key: i, name: `T${i + 1}` }))
      return build(keys, (d) => d.getFullYear() === currentYear ? d.getMonth() : -1)
    }
    if (timeFilter === 'quarter') {
      const keys = [1, 2, 3, 4].map(i => ({ key: i, name: `Quý ${i}` }))
      return build(keys, (d) => d.getFullYear() === currentYear ? Math.floor(d.getMonth() / 3) + 1 : -1)
    }
    const keys = []
    for (let i = 4; i >= 0; i--) { const y = currentYear - i; keys.push({ key: y, name: `${y}` }) }
    return build(keys, (d) => d.getFullYear())
  }, [orders, timeFilter])

  const StatCard = ({ title, value, icon: Icon, color, growth, highlight, subtitle }) => (
    <div className={`rounded-2xl p-6 shadow-sm border transition-shadow hover:shadow-md ${highlight ? 'bg-amber-50 border-amber-200' : 'bg-white border-gray-100'}`}>
      <div className="flex justify-between items-start mb-4">
        <div className={`p-3 rounded-xl ${color} bg-opacity-10`}>
          <Icon size={24} className={color.replace('bg-', 'text-')} />
        </div>
        {typeof growth === 'number' && (
          <div className={`flex items-center gap-1 text-xs font-bold px-2 py-1 rounded-full ${growth >= 0 ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'}`}>
            {growth >= 0 ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
            {Math.abs(growth).toFixed(0)}%
          </div>
        )}
      </div>
      <p className="text-gray-500 text-sm font-medium mb-1">{title}</p>
      <h3 className="text-2xl font-bold text-gray-900">{value}</h3>
      {subtitle && <p className="text-xs text-gray-400 mt-1">{subtitle}</p>}
    </div>
  )

  if (loading) {
    return <div className="flex items-center justify-center h-96"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600"></div></div>
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Tổng quan Dashboard</h1>
          <p className="text-gray-500">Số liệu kinh doanh cập nhật mới nhất.</p>
        </div>
        <div className="flex items-center bg-white rounded-lg p-1 border border-gray-200 shadow-sm">
          {[['day', 'Ngày'], ['month', 'Tháng'], ['quarter', 'Quý'], ['year', 'Năm']].map(([k, label]) => (
            <button key={k} onClick={() => setTimeFilter(k)}
              className={`px-4 py-2 text-sm font-medium rounded-md transition-all ${timeFilter === k ? 'bg-emerald-50 text-emerald-600 shadow-sm' : 'text-gray-600 hover:bg-gray-50'}`}>
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Doanh thu (đã bán)" value={fmtMoney(metrics.totalRevenue)} icon={DollarSign} color="bg-green-500" growth={metrics.revGrowth} />
        <StatCard title="Tổng đơn hàng" value={orders.length} icon={ShoppingBag} color="bg-blue-500" growth={metrics.ordGrowth} />
        <StatCard title="Đơn cần xử lý" value={metrics.pending} icon={Hourglass} color="bg-amber-500" highlight />
        <StatCard title="Giá trị đơn TB" value={fmtMoney(Math.round(metrics.aov))} icon={Receipt} color="bg-emerald-500" />
      </div>

      {/* Secondary stats: profit + rates */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard title="Lợi nhuận (ước tính)" value={fmtMoney(Math.round(metrics.estProfit))} icon={TrendingUp} color="bg-green-500" subtitle={`Biên lợi nhuận ~${metrics.estMargin.toFixed(0)}%`} />
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <p className="text-gray-500 text-sm font-medium mb-1">Tỷ lệ hoàn tất đơn</p>
          <h3 className="text-2xl font-bold text-green-600">{metrics.completedRate.toFixed(0)}%</h3>
          <div className="h-2 bg-gray-100 rounded-full mt-3 overflow-hidden"><div className="h-full bg-green-500 rounded-full" style={{ width: `${metrics.completedRate}%` }}></div></div>
        </div>
        <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <p className="text-gray-500 text-sm font-medium mb-1">Tỷ lệ huỷ đơn</p>
          <h3 className="text-2xl font-bold text-red-500">{metrics.cancelRate.toFixed(0)}%</h3>
          <div className="h-2 bg-gray-100 rounded-full mt-3 overflow-hidden"><div className="h-full bg-red-500 rounded-full" style={{ width: `${metrics.cancelRate}%` }}></div></div>
        </div>
      </div>

      {/* Revenue + status */}
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="font-bold text-gray-900">Biểu đồ doanh thu</h3>
              <p className="text-xs text-gray-500 mt-1">{timeFilter === 'day' ? '7 ngày gần nhất' : timeFilter === 'month' ? `Năm ${new Date().getFullYear()}` : timeFilter === 'quarter' ? `Theo quý ${new Date().getFullYear()}` : '5 năm gần nhất'}</p>
            </div>
            <div className="p-2 bg-green-50 rounded-lg text-green-600"><DollarSign size={20} /></div>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs><linearGradient id="cRev" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={ACCENT} stopOpacity={0.25} /><stop offset="95%" stopColor={ACCENT} stopOpacity={0} /></linearGradient></defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} tickFormatter={(v) => v >= 1000000 ? `${v / 1000000}M` : v >= 1000 ? `${v / 1000}K` : v} />
                <Tooltip contentStyle={{ borderRadius: 12, border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} formatter={(v) => [fmtMoney(v), 'Doanh thu']} />
                <Area type="monotone" dataKey="revenue" stroke={ACCENT} strokeWidth={3} fill="url(#cRev)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-gray-900 mb-1">Phân bổ trạng thái đơn</h3>
          <p className="text-xs text-gray-500 mb-4">Tỉ lệ theo trạng thái</p>
          {metrics.statusData.length === 0 ? (
            <div className="h-56 flex items-center justify-center text-gray-400 text-sm">Chưa có đơn hàng</div>
          ) : (
            <>
              <div className="h-44">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={metrics.statusData} dataKey="value" nameKey="name" innerRadius={45} outerRadius={70} paddingAngle={2}>
                      {metrics.statusData.map((e, i) => <Cell key={i} fill={e.color} />)}
                    </Pie>
                    <Tooltip formatter={(v, n) => [v, n]} contentStyle={{ borderRadius: 12, border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="space-y-1.5 mt-2">
                {metrics.statusData.map((s, i) => (
                  <div key={i} className="flex items-center justify-between text-sm">
                    <span className="flex items-center gap-2 text-gray-600"><span className="w-2.5 h-2.5 rounded-full" style={{ background: s.color }}></span>{s.name}</span>
                    <span className="font-semibold text-gray-800">{s.value}</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>

      {/* Orders bar + top products */}
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="font-bold text-gray-900">Biểu đồ đơn hàng</h3>
              <p className="text-xs text-gray-500 mt-1">Số đơn theo {timeFilter === 'day' ? 'ngày' : timeFilter === 'month' ? 'tháng' : timeFilter === 'quarter' ? 'quý' : 'năm'}</p>
            </div>
            <div className="p-2 bg-blue-50 rounded-lg text-blue-600"><ShoppingBag size={20} /></div>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} allowDecimals={false} />
                <Tooltip cursor={{ fill: '#F3F4F6' }} contentStyle={{ borderRadius: 12, border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} formatter={(v) => [v, 'Đơn hàng']} />
                <Bar dataKey="orders" fill={ACCENT2} radius={[6, 6, 0, 0]} barSize={40} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center gap-2 mb-5">
            <Trophy size={20} className="text-amber-500" />
            <h3 className="font-bold text-gray-900">Top sản phẩm bán chạy</h3>
          </div>
          {metrics.topProducts.length === 0 ? (
            <div className="h-56 flex items-center justify-center text-gray-400 text-sm text-center">Chưa có dữ liệu bán hàng</div>
          ) : (
            <div className="space-y-4">
              {metrics.topProducts.map((p, i) => (
                <div key={i}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="font-medium text-gray-700 line-clamp-1">{i + 1}. {p.name}</span>
                    <span className="font-bold text-gray-900 shrink-0 ml-2">{p.qty}</span>
                  </div>
                  <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                    <div className="h-full rounded-full" style={{ width: `${(p.qty / metrics.maxQty) * 100}%`, background: ACCENT }}></div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Revenue by category + payment method */}
      <div className="grid lg:grid-cols-2 gap-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-gray-900 mb-1">Doanh thu theo danh mục</h3>
          <p className="text-xs text-gray-500 mb-5">Danh mục nào đang bán tốt nhất</p>
          {metrics.revenueByCategory.length === 0 ? (
            <div className="py-10 text-center text-gray-400 text-sm">Chưa có dữ liệu</div>
          ) : (
            <div className="space-y-4">
              {metrics.revenueByCategory.map((c, i) => (
                <div key={i}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="font-medium text-gray-700">{c.name}</span>
                    <span className="font-bold text-gray-900">{fmtMoney(c.value)}</span>
                  </div>
                  <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden">
                    <div className="h-full rounded-full" style={{ width: `${(c.value / metrics.maxCatRev) * 100}%`, background: ACCENT }}></div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-gray-900 mb-1">Doanh thu theo phương thức</h3>
          <p className="text-xs text-gray-500 mb-5">Tiền mặt vs chuyển khoản</p>
          {metrics.revenueByPayment.length === 0 ? (
            <div className="py-10 text-center text-gray-400 text-sm">Chưa có dữ liệu</div>
          ) : (
            <>
              <div className="h-52">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={metrics.revenueByPayment} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={3}>
                      {metrics.revenueByPayment.map((e, i) => <Cell key={i} fill={[ACCENT, ACCENT2, '#F59E0B'][i % 3]} />)}
                    </Pie>
                    <Tooltip formatter={(v, n) => [fmtMoney(v), n]} contentStyle={{ borderRadius: 12, border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="space-y-1.5 mt-2">
                {metrics.revenueByPayment.map((p, i) => (
                  <div key={i} className="flex items-center justify-between text-sm">
                    <span className="flex items-center gap-2 text-gray-600"><span className="w-2.5 h-2.5 rounded-full" style={{ background: [ACCENT, ACCENT2, '#F59E0B'][i % 3] }}></span>{p.name}</span>
                    <span className="font-semibold text-gray-800">{fmtMoney(p.value)}</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>

      {/* Recent orders + low stock */}
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="p-6 border-b border-gray-100"><h3 className="font-bold text-gray-900">Đơn hàng mới nhất</h3></div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50/50">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Mã đơn</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Khách hàng</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Tổng tiền</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {recentOrders.length === 0 ? (
                  <tr><td colSpan="4" className="px-6 py-8 text-center text-gray-500">Chưa có đơn hàng nào</td></tr>
                ) : recentOrders.map(o => (
                  <tr key={o.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-6 py-4 font-medium text-gray-900">#{o.id}</td>
                    <td className="px-6 py-4 text-gray-600">{o.phoneNumber || o.email || 'Khách vãng lai'}</td>
                    <td className="px-6 py-4 font-bold text-gray-900">{fmtMoney(o.totalAmount)}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold text-white" style={{ background: STATUS_META[o.status]?.color || '#9CA3AF' }}>
                        {STATUS_META[o.status]?.label || o.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex items-center gap-2 mb-5">
            <AlertTriangle size={20} className="text-red-500" />
            <h3 className="font-bold text-gray-900">Cảnh báo sắp hết hàng</h3>
          </div>
          {lowStock.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-10 text-center">
              <Package size={36} className="text-gray-300 mb-2" />
              <p className="text-sm text-gray-500">Không có mặt hàng nào dưới mức tồn tối thiểu.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {lowStock.slice(0, 6).map((it) => (
                <div key={it.id} className="flex items-center justify-between p-3 bg-red-50 border border-red-100 rounded-xl">
                  <div>
                    <p className="text-sm font-semibold text-gray-800">{it.name}</p>
                    <p className="text-xs text-gray-500">Tối thiểu: {it.minQuantity ?? '-'} {it.unit}</p>
                  </div>
                  <span className="text-sm font-bold text-red-600">{it.quantity} {it.unit}</span>
                </div>
              ))}
            </div>
          )}
          <div className="mt-5 pt-4 border-t border-gray-100 grid grid-cols-2 gap-4 text-center">
            <div><p className="text-2xl font-bold text-gray-900">{counts.menu}</p><p className="text-xs text-gray-500">Sản phẩm</p></div>
            <div><p className="text-2xl font-bold text-gray-900">{counts.users}</p><p className="text-xs text-gray-500">Người dùng</p></div>
          </div>
        </div>
      </div>
    </div>
  )
}
