import React, { useState, useEffect, useRef } from 'react'
import { Send, User, MessageCircle, X, Minus } from 'lucide-react'
import { connectSocket } from '../services/socketService'
import api from '../services/apiClient'
import toast from 'react-hot-toast'

export default function ChatBox({ orderId, currentUser, senderName, onClose }) {
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [isOpen, setIsOpen] = useState(!!orderId)
  const [isMinimized, setIsMinimized] = useState(false)
  const messagesEndRef = useRef(null)

  useEffect(() => {
    if (orderId) {
      setIsOpen(true)
      setIsMinimized(false)
    }
  }, [orderId])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    if (isOpen) {
      fetchHistory()
      const stompClient = connectSocket((event) => {
        if (event.type === 'CHAT_MESSAGE' && event.orderId === orderId) {
          setMessages(prev => [...prev, event])
          if (event.senderId !== currentUser.userId) {
             // Play sound or show small toast if minimized
          }
        }
      }, currentUser.userId || currentUser.id)

      return () => {
        // cleanup if needed
      }
    }
  }, [isOpen, orderId])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const fetchHistory = async () => {
    try {
      const res = await api.get(`/api/chat/history/${orderId}`)
      setMessages(res.data)
    } catch (error) {
      console.error('Failed to fetch chat history', error)
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim()) return

    const messageData = {
      orderId: orderId,
      senderId: currentUser.userId || currentUser.id,
      senderName: senderName || currentUser.fullName || currentUser.email,
      message: newMessage.trim(),
      type: 'CHAT_MESSAGE'
    }

    try {
      await api.post('/api/chat/send', messageData)
      setNewMessage('')
    } catch (error) {
      toast.error('Gửi tin nhắn thất bại')
    }
  }

  if (!isOpen) {
    return (
      <button 
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 w-14 h-14 bg-emerald-500 text-white rounded-full shadow-2xl flex items-center justify-center hover:scale-110 transition-transform z-50 animate-bounce"
        title="Chat với hỗ trợ/shipper"
      >
        <MessageCircle size={28} />
      </button>
    )
  }

  return (
    <div className={`fixed bottom-6 right-6 w-80 md:w-96 bg-white rounded-3xl shadow-2xl border border-gray-100 flex flex-col overflow-hidden z-50 transition-all duration-300 ${isMinimized ? 'h-16' : 'h-[500px]'}`}>
      {/* Header */}
      <div className="bg-gradient-to-r from-emerald-500 to-teal-600 p-4 text-white flex justify-between items-center cursor-pointer" onClick={() => setIsMinimized(!isMinimized)}>
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
            <User size={18} />
          </div>
          <div>
            <h4 className="font-bold text-sm">Hỗ trợ đơn hàng #{orderId}</h4>
            <span className="text-[10px] opacity-80">Trực tuyến</span>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={(e) => { e.stopPropagation(); setIsMinimized(!isMinimized); }} className="hover:bg-white/20 p-1 rounded-lg transition-colors">
            <Minus size={20} />
          </button>
          <button onClick={(e) => { e.stopPropagation(); setIsOpen(false); if(onClose) onClose(); }} className="hover:bg-white/20 p-1 rounded-lg transition-colors">
            <X size={20} />
          </button>
        </div>
      </div>

      {!isMinimized && (
        <>
          {/* Message List */}
          <div className="flex-grow p-4 overflow-y-auto bg-gray-50 flex flex-col gap-3 custom-scrollbar">
            {messages.length === 0 ? (
              <div className="text-center py-10">
                <p className="text-gray-400 text-sm italic">Hãy gửi lời chào đầu tiên...</p>
              </div>
            ) : (
              messages.map((msg, idx) => {
                const isMe = msg.senderId === (currentUser.userId || currentUser.id)
                return (
                  <div key={idx} className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
                    <span className="text-[10px] text-gray-400 mb-1 px-2">{msg.senderName}</span>
                    <div className={`max-w-[80%] p-3 rounded-2xl text-sm shadow-sm ${
                      isMe 
                        ? 'bg-emerald-500 text-white rounded-tr-none' 
                        : 'bg-white text-gray-800 border border-gray-100 rounded-tl-none'
                    }`}>
                      {msg.message}
                    </div>
                  </div>
                )
              })
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Area */}
          <form onSubmit={handleSendMessage} className="p-4 bg-white border-t border-gray-100 flex gap-2">
            <input 
              type="text" 
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Nhập tin nhắn..."
              className="flex-grow px-4 py-2.5 bg-gray-100 border-none rounded-2xl text-sm focus:ring-2 focus:ring-emerald-500 outline-none transition-all"
            />
            <button 
              type="submit"
              disabled={!newMessage.trim()}
              className="w-10 h-10 bg-emerald-500 text-white rounded-xl flex items-center justify-center hover:bg-emerald-600 disabled:opacity-50 transition-colors"
            >
              <Send size={18} />
            </button>
          </form>
        </>
      )}
    </div>
  )
}
