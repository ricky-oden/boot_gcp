import { Navigate, Route, Routes } from 'react-router-dom'
import { StockOperationPage } from './pages/StockOperationPage'

export default function App() {
  return (
    <Routes>
      <Route path="/stock-operation" element={<StockOperationPage />} />
      <Route path="*" element={<Navigate to="/stock-operation" replace />} />
    </Routes>
  )
}
