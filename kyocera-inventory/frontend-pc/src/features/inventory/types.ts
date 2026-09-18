export interface InventoryItem {
  inventoryId: number
  itemCode: string
  itemName: string
  warehouseId: number
  warehouseName: string
  quantity: number
  status: string
}

export interface InventorySearchCriteria {
  itemCode?: string
  warehouseId?: number
}

export interface InventoryState {
  items: InventoryItem[]
  loading: boolean
  error: string | null
  hasSearched: boolean
  lastSearchCondition: InventorySearchCriteria | null
}
