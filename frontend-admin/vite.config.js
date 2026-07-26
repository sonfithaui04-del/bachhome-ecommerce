import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  // Đọc cả .env.local để override cổng gateway trên từng máy local
  const env = loadEnv(mode, process.cwd(), '')
  const gatewayTarget = env.VITE_GATEWAY_TARGET || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: 3002,
      proxy: {
        '/api': {
          target: gatewayTarget,
          changeOrigin: true
        },
        '/ws': {
          target: gatewayTarget,
          changeOrigin: true,
          ws: true
        }
      }
    },
    define: {
      global: 'window',
    }
  }
})
