import { Navigate, Route, Routes } from 'react-router-dom'
import { InventoryPage } from '../pages/InventoryPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate replace to="/inventory" />} />
      <Route path="/inventory" element={<InventoryPage />} />
      <Route path="*" element={<Navigate replace to="/inventory" />} />
    </Routes>
  )
}
