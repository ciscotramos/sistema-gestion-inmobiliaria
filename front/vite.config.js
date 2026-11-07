import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    // Separar vendors de código de app para mejor caché
    rollupOptions: {
      output: {
        manualChunks: {
          // React core en su propio chunk
          'react-vendor': ['react', 'react-dom'],
          // Router en chunk separado
          'router-vendor': ['react-router-dom'],
        },
      },
    },
    // Tamaño de chunk para warning (aumentamos un poco para evitar ruido)
    chunkSizeWarningLimit: 600,
    // CSS por ruta (code splitting de CSS)
    cssCodeSplit: true,
    // Minificación agresiva
    minify: 'esbuild',
    // Target moderno: reduce polyfills
    target: 'esnext',
  },
})
