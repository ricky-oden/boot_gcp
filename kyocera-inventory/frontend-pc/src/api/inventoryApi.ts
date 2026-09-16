import axios from 'axios'
import type { InventoryItem, InventorySearchCriteria } from '../features/inventory/types'

const inventoryHttpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 5000,
})

export async function searchInventories(
  criteria: InventorySearchCriteria,
): Promise<InventoryItem[]> {
  const response = await inventoryHttpClient.get<InventoryItem[]>('/api/inventories', {
    params: criteria.itemCode ? { itemCode: criteria.itemCode } : undefined,
  })
  return response.data
}
