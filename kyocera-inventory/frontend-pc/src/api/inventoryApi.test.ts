import { searchInventories } from './inventoryApi'
import type { InventoryItem } from '../features/inventory/types'

jest.mock('axios', () => {

    const mockGet = jest.fn()

    return {
        __esModule: true,
        default: {
            create: jest.fn(() => ({
                get: mockGet,
            })),
        },
        mockGet,
    }
})

const { mockGet } = jest.requireMock('axios')

const item: InventoryItem = {
    inventoryId: 1001,
    itemCode: 'ITEM001',
    itemName: '六角ボルト',
    warehouseId: 1,
    warehouseName: '東京倉庫',
    quantity: 120,
    status: 'AVAILABLE',
}

describe('searchInventories', () => {
    test('returns a list of inventory items', async () => {
        mockGet.mockResolvedValue({ data: [item] })
        const result = await searchInventories({ itemCode: 'ITEM001', warehouseId: 1 })

        expect(mockGet).toHaveBeenCalledWith('/api/inventories', {
            params: { itemCode: 'ITEM001', warehouseId: 1 },
        })
        expect(result).toEqual([item])
    })
})