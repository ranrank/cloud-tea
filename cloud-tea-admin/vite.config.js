import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 前端统一带 /api 前缀，这里转发到后端并去掉前缀。
      // 这样后端不用配 CORS，拦截器的 /admin/** 、/user/** 路径也保持不变。
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        // Element Plus 和 ECharts 体积大，单独拆包，避免主 chunk 过大
        manualChunks: {
          element: ['element-plus', '@element-plus/icons-vue'],
          echarts: ['echarts'],
          vue: ['vue', 'vue-router', 'pinia']
        }
      }
    }
  }
})
