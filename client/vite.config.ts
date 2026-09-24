import { defineConfig } from 'vite'

export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://10.1.1.10:9957', changeOrigin: true },
    },
  },
})
