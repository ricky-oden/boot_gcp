import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import { searchInventories } from '../api/inventoryApi'
import { createAppStore } from '../app/store'
import type { InventoryItem } from '../features/inventory/types'
import { InventoryPage } from './InventoryPage'

jest.mock('../api/inventoryApi', () => ({
  searchInventories: jest.fn(),
}))

const mockedSearchInventories = jest.mocked(searchInventories)

const item: InventoryItem = {
  inventoryId: 1001,
  itemCode: 'ITEM001',
  itemName: '六角ボルト',
  warehouseId: 1,
  warehouseName: '東京倉庫',
  quantity: 120,
  status: 'AVAILABLE',
}

function renderPage() {
  const store = createAppStore()
  return render(
    <Provider store={store}>
      <MemoryRouter
        future={{ v7_relativeSplatPath: true, v7_startTransition: true }}
        initialEntries={['/inventory']}
      >
        <InventoryPage />
      </MemoryRouter>
    </Provider>,
  )
}

describe('InventoryPage', () => {
  afterEach(() => {
    jest.clearAllMocks()
  })

  test('shows the search form and initial state', () => {
    renderPage()

    expect(screen.getByRole('heading', { name: 'Inventory Search' })).toBeInTheDocument()
    expect(screen.getByLabelText('Item Code')).toBeInTheDocument()
    expect(screen.getByLabelText('Item Name')).toBeInTheDocument()
    expect(screen.getByLabelText('Warehouse ID')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Search' })).toBeInTheDocument()
    expect(screen.getByText('Searchを押してください。検索条件は任意です。')).toBeInTheDocument()
  })

  test('submits all conditions and shows the result', async () => {
    const user = userEvent.setup()
    mockedSearchInventories.mockResolvedValue([item])
    renderPage()

    await user.type(screen.getByLabelText('Item Code'), 'ITEM001')
    await user.type(screen.getByLabelText('Item Name'), 'ボルト')
    await user.type(screen.getByLabelText('Warehouse ID'), '1')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('六角ボルト')).toBeInTheDocument()
    expect(screen.getByText('東京倉庫')).toBeInTheDocument()
    expect(mockedSearchInventories).toHaveBeenCalledWith({ itemCode: 'ITEM001', itemName: 'ボルト', warehouseId: 1 })
  })

    test('submits warehouseId and shows the result', async () => {
    const user = userEvent.setup()
    mockedSearchInventories.mockResolvedValue([item])
    renderPage()

    await user.type(screen.getByLabelText('Warehouse ID'), '1')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('六角ボルト')).toBeInTheDocument()
    expect(screen.getByText('東京倉庫')).toBeInTheDocument()
    expect(mockedSearchInventories).toHaveBeenCalledWith({  warehouseId: 1 })
  })

  test('submit itemName and shows the result', async () => {
    const user = userEvent.setup()
    mockedSearchInventories.mockResolvedValue([item])
    renderPage()

    await user.type(screen.getByLabelText('Item Name'), 'ボルト')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByText('六角ボルト')).toBeInTheDocument()
    expect(screen.getByText('東京倉庫')).toBeInTheDocument()
    expect(mockedSearchInventories).toHaveBeenCalledWith({ itemName: 'ボルト' })
  })

  test('shows loading while the API request is pending', async () => {
    const user = userEvent.setup()
    let resolveRequest!: (items: InventoryItem[]) => void
    mockedSearchInventories.mockReturnValue(
      new Promise((resolve) => {
        resolveRequest = resolve
      }),
    )
    renderPage()

    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(screen.getByRole('status')).toHaveTextContent('Loading...')
    await act(async () => resolveRequest([]))
  })

  test('shows the empty state when the API returns no rows', async () => {
    const user = userEvent.setup()
    mockedSearchInventories.mockResolvedValue([])
    renderPage()

    await user.type(screen.getByLabelText('Item Code'), 'NOT_FOUND')
    await user.type(screen.getByLabelText('Item Name'), 'NOT_FOUND')
    await user.type(screen.getByLabelText('Warehouse ID'), '999')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(
      await screen.findByText('検索条件に一致する在庫はありません。'),
    ).toBeInTheDocument()
  })

  test('shows an error when the API request fails', async () => {
    const user = userEvent.setup()
    mockedSearchInventories.mockRejectedValue(new Error('network error'))
    renderPage()

    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(
      await screen.findByText('在庫検索中に予期しないエラーが発生しました。'),
    ).toBeInTheDocument()
  })

    test('shows an error message when submit wrong warehouseId', async () => {
    const user = userEvent.setup()
    mockedSearchInventories.mockResolvedValue([item])
    renderPage()

    await user.type(screen.getByLabelText('Warehouse ID'), 'abc')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    expect(
      await screen.findByText('Warehouse IDは半角数字で入力してください。')
    ).toBeInTheDocument()
  })
})
