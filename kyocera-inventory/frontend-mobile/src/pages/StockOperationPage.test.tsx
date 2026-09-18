import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { StockOperationPage } from './StockOperationPage'
import { createStockMovement, searchInventory } from '../api/stockOperationApi'

jest.mock('../api/stockOperationApi', () => ({
  searchInventory: jest.fn(),
  createStockMovement: jest.fn(),
  toDisplayMessage: (error: unknown) => error instanceof Error ? error.message : 'API Error',
}))

const mockedSearch = jest.mocked(searchInventory)
const mockedMovement = jest.mocked(createStockMovement)

const inventory = {
  inventoryId: 1001,
  itemCode: 'ITEM001',
  itemName: '六角ボルト',
  warehouseId: 1,
  warehouseName: '東京倉庫',
  quantity: 120,
  status: 'AVAILABLE',
}

async function search(user: ReturnType<typeof userEvent.setup>) {
  mockedSearch.mockResolvedValue([inventory])
  await user.clear(screen.getByLabelText('Barcode / QR相当値'))
  await user.type(screen.getByLabelText('Barcode / QR相当値'), 'ITEM001')
  await user.click(screen.getByRole('button', { name: 'Search' }))
  await screen.findByText('六角ボルト')
}

beforeEach(() => {
  jest.clearAllMocks()
})

test('Barcode相当値で在庫を取得して表示する', async () => {
  const user = userEvent.setup()
  render(<StockOperationPage />)
  await search(user)

  expect(mockedSearch).toHaveBeenCalledWith('ITEM001', 1)
  expect(screen.getByText('120')).toBeInTheDocument()
})

test('入庫後に現在庫とSuccess Messageを更新する', async () => {
  const user = userEvent.setup()
  render(<StockOperationPage />)
  await search(user)
  mockedMovement.mockResolvedValue({
    itemCode: 'ITEM001', warehouseId: 1, movementType: 'IN', quantity: 5,
    previousQuantity: 120, currentQuantity: 125, processedAt: '2026-09-18T01:00:00Z',
  })

  await user.clear(screen.getByLabelText('入出庫数量'))
  await user.type(screen.getByLabelText('入出庫数量'), '5')
  await user.click(screen.getByRole('button', { name: '入庫' }))

  expect(await screen.findByRole('status')).toHaveTextContent('入庫しました。現在庫: 125')
  expect(mockedMovement).toHaveBeenCalledWith(expect.objectContaining({ movementType: 'IN', quantity: 5 }))
})

test('出庫ButtonからOUT Requestを送れる', async () => {
  const user = userEvent.setup()
  render(<StockOperationPage />)
  await search(user)
  mockedMovement.mockResolvedValue({
    itemCode: 'ITEM001', warehouseId: 1, movementType: 'OUT', quantity: 5,
    previousQuantity: 120, currentQuantity: 115, processedAt: '2026-09-18T01:00:00Z',
  })

  await user.clear(screen.getByLabelText('入出庫数量'))
  await user.type(screen.getByLabelText('入出庫数量'), '5')
  await user.click(screen.getByRole('button', { name: '出庫' }))

  expect(await screen.findByRole('status')).toHaveTextContent('出庫しました。現在庫: 115')
  expect(mockedMovement).toHaveBeenCalledWith(expect.objectContaining({ movementType: 'OUT', quantity: 5 }))
})

test('API Errorを画面に表示する', async () => {
  const user = userEvent.setup()
  render(<StockOperationPage />)
  await search(user)
  mockedMovement.mockRejectedValue(new Error('在庫不足です。'))

  await user.click(screen.getByRole('button', { name: '出庫' }))

  expect(await screen.findByRole('alert')).toHaveTextContent('在庫不足です。')
})
