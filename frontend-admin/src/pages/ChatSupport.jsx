import { useState, useEffect, useRef } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'
import { connectSocket, subscribeOrderChat, unsubscribeOrderChat } from '../services/socketService'
import { MessageSquare, Send, RefreshCw, Package } from 'lucide-react'

/**
 * Trang hỗ trợ khách hàng: cột trái là danh sách hội thoại theo đơn hàng,
 * cột phải là khung chat của đơn đang chọn. Tin nhắn mới của khách hiện ngay
 * nhờ kênh WebSocket /topic/orders/{orderId}/chat.
 */
export default function ChatSupport() {
  const { getToken, user } = useAuth()
  const [conversations, setConversations] = useState([])
  const [activeOrderId, setActiveOrderId] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const endRef = useRef(null)

  const authHeader = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

  useEffect(() => {
    loadConversations()
    connectSocket()
  }, [])

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Mở hội thoại nào thì lắng nghe đúng kênh của đơn đó
  useEffect(() => {
    if (!activeOrderId) return

    loadHistory(activeOrderId)

    subscribeOrderChat(activeOrderId, (event) => {
      setMessages(prev =>
        prev.some(m => m.id && m.id === event.id) ? prev : [...prev, event]
      )
      loadConversations()
    })

    return () => unsubscribeOrderChat(activeOrderId)
  }, [activeOrderId])

  const loadConversations = async () => {
    try {
      const res = await axios.get('/api/chat/conversations', authHeader())
      setConversations(res.data || [])
    } catch (error) {
      console.error('Không tải được danh sách hội thoại', error)
      setConversations([])
    } finally {
      setLoading(false)
    }
  }

  const loadHistory = async (orderId) => {
    try {
      const res = await axios.get(`/api/chat/history/${orderId}`, authHeader())
      setMessages(res.data || [])
    } catch (error) {
      console.error('Không tải được lịch sử chat', error)
      setMessages([])
    }
  }

  const handleSend = async (e) => {
    e.preventDefault()
    const text = newMessage.trim()
    if (!text || !activeOrderId || sending) return

    setSending(true)
    try {
      await axios.post('/api/chat/send', {
        orderId: Number(activeOrderId),
        senderId: user?.userId || user?.id,
        senderName: user?.fullName || 'Quản trị viên',
        message: text,
        type: 'CHAT_MESSAGE'
      }, authHeader())
      setNewMessage('')
      // Tin của chính mình cũng quay về qua WebSocket nên không cần tự thêm
    } catch (error) {
      console.error('Gửi tin nhắn thất bại', error)
    } finally {
      setSending(false)
    }
  }

  const formatTime = (ts) => {
    if (!ts) return ''
    const d = new Date(ts)
    return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })
  }

  const isMine = (m) => {
    const myId = user?.userId || user?.id
    return myId != null && String(m.senderId) === String(myId)
  }

  return (
    <div className="h-[calc(100vh-8rem)] flex flex-col">
      <div className="mb-4">
        <h1 className="text-2xl font-bold text-gray-800">Hỗ trợ khách hàng</h1>
        <p className="text-sm text-gray-500">Trả lời tin nhắn của khách theo từng đơn hàng</p>
      </div>

      <div className="flex-1 flex gap-4 min-h-0">
        {/* Danh sách hội thoại */}
        <div className="w-72 shrink-0 bg-white rounded-2xl border border-gray-100 shadow-sm flex flex-col overflow-hidden">
          <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
            <span className="font-semibold text-gray-700">Hội thoại</span>
            <button
              onClick={loadConversations}
              className="text-gray-400 hover:text-emerald-600"
              title="Tải lại"
            >
              <RefreshCw size={16} />
            </button>
          </div>

          <div className="flex-1 overflow-y-auto">
            {loading && (
              <p className="p-4 text-sm text-gray-400">Đang tải…</p>
            )}
            {!loading && conversations.length === 0 && (
              <p className="p-4 text-sm text-gray-400">Chưa có tin nhắn nào từ khách.</p>
            )}
            {conversations.map((c) => (
              <button
                key={c.orderId}
                onClick={() => setActiveOrderId(c.orderId)}
                className={`w-full text-left px-4 py-3 border-b border-gray-50 hover:bg-gray-50 transition-colors ${
                  String(activeOrderId) === String(c.orderId) ? 'bg-emerald-50 border-l-4 border-l-emerald-500' : ''
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-gray-800 text-sm">Đơn #{c.orderId}</span>
                  <span className="text-[11px] text-gray-400">{formatTime(c.lastTimestamp)}</span>
                </div>
                <p className="text-xs text-gray-500 truncate mt-0.5">
                  <span className="font-medium">{c.lastSenderName}:</span> {c.lastMessage}
                </p>
                <span className="inline-block mt-1 text-[11px] text-emerald-600">{c.total} tin nhắn</span>
              </button>
            ))}
          </div>
        </div>

        {/* Khung chat */}
        <div className="flex-1 bg-white rounded-2xl border border-gray-100 shadow-sm flex flex-col overflow-hidden">
          {!activeOrderId ? (
            <div className="flex-1 flex flex-col items-center justify-center text-gray-400">
              <MessageSquare size={40} className="mb-3" />
              <p className="text-sm">Chọn một hội thoại bên trái để bắt đầu trả lời</p>
            </div>
          ) : (
            <>
              <div className="px-4 py-3 border-b border-gray-100 flex items-center gap-2">
                <span className="w-8 h-8 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center">
                  <Package size={16} />
                </span>
                <div>
                  <p className="font-semibold text-gray-800 leading-tight">Đơn hàng #{activeOrderId}</p>
                  <p className="text-xs text-gray-400">{messages.length} tin nhắn</p>
                </div>
              </div>

              <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50">
                {messages.map((m, i) => (
                  <div key={m.id ?? i} className={`flex flex-col ${isMine(m) ? 'items-end' : 'items-start'}`}>
                    <span className="text-[11px] text-gray-400 mb-0.5">
                      {m.senderName} · {formatTime(m.timestamp)}
                    </span>
                    <div
                      className={`max-w-[70%] px-3 py-2 rounded-2xl text-sm whitespace-pre-wrap ${
                        isMine(m)
                          ? 'bg-emerald-600 text-white rounded-br-sm'
                          : 'bg-white text-gray-800 border border-gray-200 rounded-bl-sm'
                      }`}
                    >
                      {m.message}
                    </div>
                  </div>
                ))}
                <div ref={endRef} />
              </div>

              <form onSubmit={handleSend} className="p-3 border-t border-gray-100 flex items-center gap-2">
                <input
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="Nhập câu trả lời…"
                  className="flex-1 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
                <button
                  type="submit"
                  disabled={sending || !newMessage.trim()}
                  className="bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-full w-10 h-10 flex items-center justify-center shrink-0"
                  aria-label="Gửi"
                >
                  <Send size={18} />
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
