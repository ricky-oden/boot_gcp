import axios from 'axios'
import type {
  ApiError,
  InventoryItem,
  StockMovementRequest,
  StockMovementResponse,
} from '../types'

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 5000,
})

export async function searchInventory(itemCode: string, warehouseId: number): Promise<InventoryItem[]> {
  const response = await httpClient.get<InventoryItem[]>('/api/inventories', {
    params: { itemCode, warehouseId },
  })
  return response.data
}

export async function createStockMovement(
  request: StockMovementRequest,
): Promise<StockMovementResponse> {
  const response = await httpClient.post<StockMovementResponse>('/api/stock-movements', request)
  return response.data
}

export function toDisplayMessage(error: unknown): string {
  if (axios.isAxiosError<ApiError>(error)) {
    return error.response?.data?.message || 'API通信に失敗しました。'
  }
  return error instanceof Error ? error.message : '予期しないErrorが発生しました。'
}
