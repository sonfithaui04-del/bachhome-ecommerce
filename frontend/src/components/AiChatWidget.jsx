import { useState, useRef, useEffect } from 'react'
import axios from 'axios'

const GREETING = {
  role: 'assistant',
  text: 'Xin chào 👋 Mình là trợ lý của BachHome. Bạn cần tư vấn đồ gia dụng gì để mình gợi ý nhé?',
  suggestions: [],
}

const priceFmt = new Intl.NumberFormat('vi-VN')

function formatPrice(p) {
  if (p === null || p === undefined) return ''
  return priceFmt.format(Number(p)) + '₫'
}

// Render **đậm** trong một dòng
function renderInline(text) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g)
  return parts.map((part, i) => {
    if (/^\*\*[^*]+\*\*$/.test(part)) {
      return <strong key={i}>{part.slice(2, -2)}</strong>
    }
    return <span key={i}>{part}</span>
  })
}

// Render markdown đơn giản: đậm, gạch đầu dòng, xuống dòng
function renderRich(text) {
  const lines = text.split('\n')
  const blocks = []
  let listItems = []
  const flushList = (key) => {
    if (listItems.length) {
      blocks.push(
        <ul key={'ul' + key} className="list-disc pl-5 space-y-0.5 my-1">
          {listItems}
        </ul>
      )
      listItems = []
    }
  }
  lines.forEach((line, i) => {
    const trimmed = line.trim()
    const bullet = trimmed.match(/^[*-]\s+(.*)$/)
    if (bullet) {
      listItems.push(<li key={i}>{renderInline(bullet[1])}</li>)
    } else {
      flushList(i)
      if (trimmed === '') {
        blocks.push(<div key={i} className="h-1.5" />)
      } else {
        blocks.push(
          <p key={i} className="my-0.5">
            {renderInline(line)}
          </p>
        )
      }
    }
  })
  flushList('end')
  return blocks
}

export default function AiChatWidget() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([GREETING])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const endRef = useRef(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, open])

  const sendMessage = async () => {
    const text = input.trim()
    if (!text || loading) return

    const newMessages = [...messages, { role: 'user', text }]
    setMessages(newMessages)
    setInput('')
    setLoading(true)

    try {
      const history = messages
        .filter((m) => m !== GREETING)
        .map((m) => ({ role: m.role === 'assistant' ? 'model' : 'user', content: m.text }))

      const res = await axios.post('/api/ai/chat', { message: text, history })
      const reply = res.data?.reply || 'Xin lỗi, mình chưa trả lời được câu này.'
      const suggestions = res.data?.suggestions || []
      setMessages((prev) => [...prev, { role: 'assistant', text: reply, suggestions }])
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', text: 'Xin lỗi, trợ lý đang bận. Bạn thử lại sau ít phút nhé.', suggestions: [] },
      ])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  return (
    <>
      {!open && (
        <button
          onClick={() => setOpen(true)}
          aria-label="Mở trợ lý AI"
          className="fixed bottom-6 right-6 w-14 h-14 bg-emerald-600 hover:bg-emerald-700 text-white rounded-full shadow-2xl flex items-center justify-center hover:scale-110 transition-transform z-50"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" viewBox="0 0 24 24"
               fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        </button>
      )}

      {open && (
        <div className="fixed bottom-6 right-6 w-80 md:w-96 h-[520px] bg-white rounded-3xl shadow-2xl border border-gray-100 flex flex-col overflow-hidden z-50">
          {/* Header */}
          <div className="bg-emerald-600 text-white px-4 py-3 flex items-center justify-between shrink-0">
            <div className="flex items-center gap-2">
              <span className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">🛍️</span>
              <div>
                <p className="font-semibold leading-tight">Trợ lý BachHome</p>
                <p className="text-xs text-emerald-100">Tư vấn đồ gia dụng bằng AI</p>
              </div>
            </div>
            <button onClick={() => setOpen(false)} aria-label="Đóng" className="text-white/90 hover:text-white text-xl leading-none">
              ×
            </button>
          </div>

          {/* Tin nhắn */}
          <div className="flex-1 overflow-y-auto p-3 space-y-3 bg-gray-50">
            {messages.map((m, i) => (
              <div key={i} className={`flex flex-col ${m.role === 'user' ? 'items-end' : 'items-start'}`}>
                <div
                  className={`max-w-[85%] px-3 py-2 rounded-2xl text-sm ${
                    m.role === 'user'
                      ? 'bg-emerald-600 text-white rounded-br-sm whitespace-pre-wrap'
                      : 'bg-white text-gray-800 border border-gray-200 rounded-bl-sm leading-relaxed'
                  }`}
                >
                  {m.role === 'user' ? m.text : renderRich(m.text)}
                </div>

                {/* Thẻ sản phẩm gợi ý (kèm ảnh) */}
                {m.role === 'assistant' && m.suggestions && m.suggestions.length > 0 && (
                  <div className="mt-2 grid grid-cols-2 gap-2 w-[85%]">
                    {m.suggestions.map((s) => (
                      <div key={s.id ?? s.name} className="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
                        <div className="w-full h-20 bg-gray-100">
                          {s.imageUrl && (
                            <img
                              src={s.imageUrl}
                              alt={s.name}
                              className="w-full h-full object-cover"
                              onError={(e) => { e.currentTarget.style.display = 'none' }}
                            />
                          )}
                        </div>
                        <div className="p-2">
                          <p className="text-xs font-medium text-gray-800 line-clamp-2">{s.name}</p>
                          {s.price != null && (
                            <p className="text-xs text-emerald-600 font-semibold mt-0.5">{formatPrice(s.price)}</p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-white border border-gray-200 text-gray-500 px-3 py-2 rounded-2xl rounded-bl-sm text-sm">
                  Đang soạn trả lời…
                </div>
              </div>
            )}
            <div ref={endRef} />
          </div>

          {/* Ô nhập */}
          <div className="p-3 border-t border-gray-100 flex items-end gap-2 bg-white shrink-0">
            <textarea
              rows={1}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Nhập câu hỏi…"
              className="flex-1 resize-none border border-gray-200 rounded-2xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 max-h-24"
            />
            <button
              onClick={sendMessage}
              disabled={loading || !input.trim()}
              className="bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-full w-10 h-10 flex items-center justify-center shrink-0"
              aria-label="Gửi"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
            </button>
          </div>
        </div>
      )}
    </>
  )
}
