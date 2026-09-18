import { useForm } from 'react-hook-form'
import { useAppDispatch, useAppSelector } from '../app/hooks'
import { Alert } from '../components/design-system/Alert'
import { Button } from '../components/design-system/Button'
import { Table } from '../components/design-system/Table'
import type { TableColumn } from '../components/design-system/Table'
import { TextField } from '../components/design-system/TextField'
import { fetchInventories, resetInventoryState } from '../features/inventory/inventorySlice'
import {
  selectInventoryError,
  selectInventoryHasSearched,
  selectInventoryItems,
  selectInventoryLoading,
  selectLastSearchCondition,
} from '../features/inventory/selectors'
import type { InventoryItem, InventorySearchCriteria } from '../features/inventory/types'

interface InventorySearchForm {
  itemCode: string
  warehouseId: string
}

const columns: TableColumn<InventoryItem>[] = [
  { key: 'inventoryId', header: 'Inventory ID', render: (row) => row.inventoryId },
  { key: 'itemCode', header: 'Item Code', render: (row) => row.itemCode },
  { key: 'itemName', header: 'Item Name', render: (row) => row.itemName },
  { key: 'warehouseId', header: 'Warehouse ID', render: (row) => row.warehouseId },
  { key: 'warehouseName', header: 'Warehouse Name', render: (row) => row.warehouseName },
  { key: 'quantity', header: 'Quantity', render: (row) => row.quantity },
  { key: 'status', header: 'Status', render: (row) => row.status },
]

export function InventoryPage() {
  const dispatch = useAppDispatch()
  const items = useAppSelector(selectInventoryItems)
  const loading = useAppSelector(selectInventoryLoading)
  const error = useAppSelector(selectInventoryError)
  const hasSearched = useAppSelector(selectInventoryHasSearched)
  const lastSearchCondition = useAppSelector(selectLastSearchCondition)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<InventorySearchForm>({ defaultValues: { itemCode: '', warehouseId: '' } })

  const onSubmit = ({ itemCode, warehouseId }: InventorySearchForm) => {
    const normalizedItemCode = itemCode ? itemCode.trim() : undefined
    const normalizedWarehouseId = warehouseId ? Number(warehouseId) : undefined
    const criteria: InventorySearchCriteria =
    {
      itemCode: normalizedItemCode,
      warehouseId: normalizedWarehouseId
    }
    dispatch(fetchInventories(criteria))
  }

  const handleReset = () => {
    dispatch(resetInventoryState())
    reset()
  }

  return (
    <main className="page-shell">
      <header className="page-header">
        <p className="eyebrow">KYOCERA CATCH-UP / DAY 3</p>
        <h1>Inventory Search</h1>
        <p>OpenAPI + MyBatis BackendをRedux Toolkit経由で検索する学習画面です。</p>
      </header>

      <section className="panel" aria-labelledby="search-heading">
        <h2 id="search-heading">Search Conditions</h2>
        <form className="search-form" onSubmit={handleSubmit(onSubmit)}>
          <TextField
            label="Item Code"
            placeholder="ITEM001"
            {...register('itemCode', {
              maxLength: { value: 30, message: 'Item Codeは30文字以内で入力してください。' },
              pattern: {
                value: /^[A-Za-z0-9_-]*$/,
                message: '半角英数字、ハイフン、アンダースコアを使用してください。',
              },
            })}
            error={errors.itemCode?.message}
          />
          <TextField
            label="Warehouse ID"
            placeholder="1"
            {...register('warehouseId', {
              pattern: {
                value: /^[0-9]*$/,
                message: 'Warehouse IDは半角数字で入力してください。',
              },
            })}
            error={errors.warehouseId?.message}
          />
          <Button disabled={loading} type="submit">
            Search
          </Button>
          <Button disabled={loading} type="button" onClick={handleReset}>
            Reset
          </Button>
        </form>
      </section>

      <section className="panel" aria-labelledby="results-heading">
        <div className="results-heading">
          <h2 id="results-heading">Search Results</h2>
          {lastSearchCondition && (
            <span className="last-condition">
              Item Code: {lastSearchCondition.itemCode || 'All'}
              , Warehouse ID: {lastSearchCondition.warehouseId || 'All'}
            </span>
          )}
        </div>

        {!hasSearched && !loading && !error && (
          <p className="state-message">Item CodeやWarehouse IDを入力してSearchを押してください。</p>
        )}
        {loading && <p className="state-message" role="status">Loading...</p>}
        {error && <Alert tone="error">{error}</Alert>}
        {hasSearched && !loading && !error && items.length === 0 && (
          <Alert>検索条件に一致する在庫はありません。</Alert>
        )}
        {!loading && !error && items.length > 0 && (
          <Table
            caption="Inventory search results"
            columns={columns}
            rows={items}
            rowKey={(row) => row.inventoryId}
          />
        )}
      </section>
    </main>
  )
}
