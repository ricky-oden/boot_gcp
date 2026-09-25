# Day4: Mobile在庫更新・Transaction・履歴

## 情報の区分

- 学習用仮定: `ITEM001`をBarcode / QRから読み取った文字列とみなす。実Camera・Scannerは使わない。
- 学習用仮定: `POST /api/stock-movements`、HTTP 400／409／500の使い分け、`stock_history` Schemaは実案件仕様ではない。
- 完成範囲: 入庫（IN）と出庫（OUT）がFrontendからDBまで完成し、在庫不足Validationも実装済み。

## 処理FlowとCodeを追う順番

```text
frontend-mobile/StockOperationPage
  → stockOperationApi.searchInventory（GET）
  → 在庫表示
  → stockOperationApi.createStockMovement（POST）
  → generated StockMovementsApi / StockMovementRequest
  → StockMovementController
  → StockMovementService @Transactional
  → StockMovementMapper.selectForUpdate
  → 業務Validation
  → StockMovementMapper.updateQuantity
  → StockMovementMapper.insertHistory
  → Commit
  → StockMovementResponse
  → 画面の現在庫を再描画
```

OpenAPI YAMLがAPI契約の正です。`backend-api/build/generated/openapi/`のInterface／Modelは直接編集しません。

## Transaction Boundary

`StockMovementService.process`全体を`@Transactional`で囲みます。在庫行のLock取得、現在庫計算、`inventory` UPDATE、`stock_history` INSERTが1つの業務操作だからです。

UPDATEだけCommitされてHistory INSERTが失敗すると、現在庫は変わったのに「誰が何個動かしたか」に相当する履歴が欠けます。今回のRuntime ExceptionではSpringがTransactionをRollbackし、先に実行したUPDATEも元へ戻します。Checked Exceptionはdefaultでは自動Rollback対象でないため、例外設計と`rollbackFor`要否を確認します。

## Error設計

| HTTP | 区分 | 例 |
|---:|---|---|
| 400 | Request Validation | quantityが0、必須値なし、JSON不正 |
| 409 | 業務Error | 在庫なし、出庫時の在庫不足 |
| 500 | 予期しないServer Error | DB障害、想定外のRuntime Exception |

現在庫未満なら出庫に成功し、現在庫を超える場合は`INSUFFICIENT_STOCK`の409を返します。

## 起動と自動Test

```bash
cd kyocera-inventory
./gradlew :backend-api:openApiValidate :backend-api:test :backend-api:build
cd frontend-mobile
npm ci
npm test
npm run build
cd ../..
docker compose -f kyocera-inventory/docker-compose.yml up -d --build
```

Mobile画面は`http://localhost:5175/stock-operation`、PC画面は`http://localhost:5174/inventory`です。

## API確認

検索:

```bash
curl "http://localhost:8081/api/inventories?itemCode=ITEM001&warehouseId=1"
```

入庫:

```bash
curl -i -X POST http://localhost:8081/api/stock-movements \
  -H 'Content-Type: application/json' \
  -d '{"itemCode":"ITEM001","warehouseId":1,"movementType":"IN","quantity":5}'
```

Request Validation Error:

```bash
curl -i -X POST http://localhost:8081/api/stock-movements \
  -H 'Content-Type: application/json' \
  -d '{"itemCode":"ITEM001","warehouseId":1,"movementType":"IN","quantity":0}'
```

在庫不足Error（409）:

```bash
curl -i -X POST http://localhost:8081/api/stock-movements \
  -H 'Content-Type: application/json' \
  -d '{"itemCode":"ITEM001","warehouseId":1,"movementType":"OUT","quantity":999}'
```

## Browser Manual Practice

1. `http://localhost:5175/stock-operation`を開く。
2. DevToolsのNetwork Tabを開き、Fetch/XHRへ絞る。
3. Barcode相当値へ`ITEM001`、倉庫IDへ`1`を入力する。
4. Searchを押し、GET `/api/inventories`のQuery、HTTP 200、Responseを確認する。
5. 画面の現在庫数を記録する。
6. 入出庫数量へ`5`を入力し、入庫を押す。
7. POST `/api/stock-movements`のRequest PayloadとResponseを確認する。
8. `previousQuantity`と`currentQuantity`を画面表示と比較する。
9. psqlで`inventory`と`stock_history`を確認する。
10. 出庫を押し、在庫数が減ることを確認する。
11. 現在庫より大きい出庫数量で409 `INSUFFICIENT_STOCK`を確認する。

## psqlで操作前後を比較

```bash
docker compose -f kyocera-inventory/docker-compose.yml exec kyocera-db \
  psql -U kyocera -d kyocera_inventory
```

```sql
SELECT inventory_id, item_code, warehouse_id, quantity, updated_at
FROM inventory
WHERE item_code = 'ITEM001' AND warehouse_id = 1;

SELECT history_id, inventory_id, movement_type, quantity,
       before_quantity, after_quantity, processed_at
FROM stock_history
ORDER BY history_id DESC;
```

入庫前の数量をメモし、画面操作後に同じSQLを再実行します。`after_quantity - before_quantity = quantity`になっていることを確認します。

## Rollback Testの読み方

`StockMovementTransactionTest.rollsBackInventoryWhenHistoryInsertFails`は、Test専用Schemaで`movement_type`を1文字に制限します。Serviceは先にinventoryをUPDATEし、その後`IN`のHistory INSERTで意図的に失敗します。Serviceから例外が戻った後、inventoryが120のまま、Historyが0件であることを検証します。

## 完了済みExercise: OUT

INを手本に次を実装し、完了済みです。

1. `StockMovementService`: OUT分岐を見る。
2. 現在庫とRequest数量を比較し、不足時は業務例外を返す。
3. 足りる場合は減算した数量で、同じUPDATE／INSERT経路を再利用する。
4. `StockMovementServiceTest`: 出庫成功、不足、quantity不正の期待値を追加する。
5. `StockMovementTransactionTest`: OUT成功時のinventory／Historyを追加する。
6. Browserで現在庫より大きい数量を出庫し、409を確認する。

Transaction Boundaryは増やさず、INと同じUPDATE／INSERT経路を再利用しています。

## 説明練習Question

1. `@Transactional`は何をしているか。
2. Transaction Boundaryとは何か。
3. UPDATEとINSERTの片方だけ成功すると何が問題か。
4. Rollbackはいつ発生するか。
5. Checked ExceptionとRuntime ExceptionでdefaultのRollback条件はどう違うか。
6. Serviceに業務Validationを置く理由は何か。
7. 400／409／500はどう使い分けるか。
8. MyBatis UPDATEはMapper InterfaceからXMLのどこへ解決されるか。
9. Historyを残す理由は何か。
10. Barcode入力からDB更新まで、どのFileを順に辿るか。
11. `SELECT ... FOR UPDATE`は同時更新にどう関係するか。

## Daily Meeting練習Template

```text
実装したこと:
- Smartphone画面から商品を検索し、入庫APIで在庫更新と履歴登録を行う経路を作りました。

DB更新箇所:
- inventory.quantity／updated_atを更新し、stock_historyへ操作前後の数量を登録します。

Transaction:
- Serviceのprocess全体をBoundaryにし、SELECT FOR UPDATE、UPDATE、INSERTを1 Transactionにしました。

Testした正常系／異常系:
- 入庫成功、出庫成功、quantity不正、在庫不足409、History INSERT失敗時Rollbackを確認しました。

現在の問題:
- Day4のOUT Exerciseまで完了済みです。

切り分け済み:
- Frontend、Backend Service、MyBatis、DB更新まで切り分け済みです。
```
