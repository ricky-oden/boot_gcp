import inventoryReducer, {
  fetchInventories,
  initialInventoryState,
} from './inventorySlice'
import type { InventoryItem } from './types'

jest.mock('../../api/inventoryApi', () => ({
  searchInventories: jest.fn(),
}))

const item: InventoryItem = {
  inventoryId: 1001,
  itemCode: 'ITEM001',
  itemName: '六角ボルト',
  warehouseId: 1,
  warehouseName: '東京倉庫',
  quantity: 120,
  status: 'AVAILABLE',
}

describe('inventoryReducer', () => {
  test('returns the initial state', () => {
    expect(inventoryReducer(undefined, { type: 'unknown' })).toEqual(initialInventoryState)
  })

  test('handles pending', () => {
    const state = inventoryReducer(
      initialInventoryState,
      fetchInventories.pending('request-id', { itemCode: 'ITEM001' }),
    )

    expect(state.loading).toBe(true)
    expect(state.error).toBeNull()
    expect(state.lastSearchCondition).toEqual({ itemCode: 'ITEM001' })
  })

  test('handles fulfilled', () => {
    const loadingState = { ...initialInventoryState, loading: true }
    const state = inventoryReducer(
      loadingState,
      fetchInventories.fulfilled([item], 'request-id', { itemCode: 'ITEM001' }),
    )

    expect(state.loading).toBe(false)
    expect(state.hasSearched).toBe(true)
    expect(state.items).toEqual([item])
  })

  test('handles rejected', () => {
    const loadingState = { ...initialInventoryState, loading: true, items: [item] }
    const state = inventoryReducer(
      loadingState,
      fetchInventories.rejected(
        new Error('network error'),
        'request-id',
        { itemCode: 'ITEM001' },
        'API error',
      ),
    )

    expect(state.loading).toBe(false)
    expect(state.hasSearched).toBe(true)
    expect(state.items).toEqual([])
    expect(state.error).toBe('API error')
  })
})
