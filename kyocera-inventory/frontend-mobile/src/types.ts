export type MovementType = 'IN' | 'OUT'

export interface InventoryItem {
  inventoryId: number
  itemCode: string
  itemName: string
  warehouseId: number
  warehouseName: string
  quantity: number
  status: string
}

export interface StockMovementRequest {
  itemCode: string
  warehouseId: number
  movementType: MovementType
  quantity: number
}

export interface StockMovementResponse extends StockMovementRequest {
  previousQuantity: number
  currentQuantity: number
  processedAt: string
}

export interface ApiError {
  code?: string
  message?: string
}
