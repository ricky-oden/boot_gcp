import { FormEvent, useState } from 'react'
import { createStockMovement, searchInventory, toDisplayMessage } from '../api/stockOperationApi'
import type { InventoryItem, MovementType } from '../types'

export function StockOperationPage() {
  const [barcode, setBarcode] = useState('ITEM001')
  const [warehouseId, setWarehouseId] = useState('1')
  const [operationQuantity, setOperationQuantity] = useState('1')
  const [inventory, setInventory] = useState<InventoryItem | null>(null)
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const clearMessages = () => {
    setSuccessMessage('')
    setErrorMessage('')
  }

  const handleSearch = async (event: FormEvent) => {
    event.preventDefault()
    clearMessages()
    setLoading(true)
    try {
      const results = await searchInventory(barcode.trim(), Number(warehouseId))
      if (results.length === 0) {
        setInventory(null)
        setErrorMessage('指定した商品・倉庫の在庫が見つかりません。')
        return
      }
      setInventory(results[0])
    } catch (error) {
      setInventory(null)
      setErrorMessage(toDisplayMessage(error))
    } finally {
      setLoading(false)
    }
  }

  const handleMovement = async (movementType: MovementType) => {
    if (!inventory) return
    clearMessages()
    setLoading(true)
    try {
      const result = await createStockMovement({
        itemCode: inventory.itemCode,
        warehouseId: inventory.warehouseId,
        movementType,
        quantity: Number(operationQuantity),
      })
      setInventory({ ...inventory, quantity: result.currentQuantity })
      setSuccessMessage(
        `${movementType === 'IN' ? '入庫' : '出庫'}しました。現在庫: ${result.currentQuantity}`,
      )
    } catch (error) {
      setErrorMessage(toDisplayMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="mobile-shell">
      <header>
        <p className="eyebrow">Day4 / 学習用Smartphone画面</p>
        <h1>在庫入出庫</h1>
        <p>Barcode / QRの代わりに商品Codeを文字入力します。</p>
      </header>

      <form className="card" onSubmit={handleSearch}>
        <label htmlFor="barcode">Barcode / QR相当値</label>
        <input
          id="barcode"
          value={barcode}
          onChange={(event) => setBarcode(event.target.value)}
          required
        />
        <label htmlFor="warehouseId">倉庫ID</label>
        <input
          id="warehouseId"
          type="number"
          min="1"
          value={warehouseId}
          onChange={(event) => setWarehouseId(event.target.value)}
          required
        />
        <button type="submit" disabled={loading}>Search</button>
      </form>

      {inventory && (
        <section className="card" aria-label="在庫情報">
          <h2>{inventory.itemName}</h2>
          <dl>
            <div><dt>商品Code</dt><dd>{inventory.itemCode}</dd></div>
            <div><dt>倉庫</dt><dd>{inventory.warehouseName}</dd></div>
            <div><dt>現在庫数</dt><dd>{inventory.quantity}</dd></div>
          </dl>
          <label htmlFor="operationQuantity">入出庫数量</label>
          <input
            id="operationQuantity"
            type="number"
            min="1"
            value={operationQuantity}
            onChange={(event) => setOperationQuantity(event.target.value)}
          />
          <div className="button-row">
            <button type="button" disabled={loading} onClick={() => handleMovement('IN')}>入庫</button>
            <button type="button" className="secondary" disabled={loading} onClick={() => handleMovement('OUT')}>出庫</button>
          </div>
          <small>出庫Backend処理はDay4 Exerciseです。</small>
        </section>
      )}

      {successMessage && <p role="status" className="message success">{successMessage}</p>}
      {errorMessage && <p role="alert" className="message error">{errorMessage}</p>}
    </main>
  )
}
