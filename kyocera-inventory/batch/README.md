# Daily Stock Summary Batch (Day5)

`stock_history`を日付・商品・倉庫単位で集計し、`daily_stock_summary`へ保存する学習用Spring Batch moduleです。実案件のBatch仕様ではありません。

詳細な正常実行、失敗、Metadata確認、Restart、Reset手順は`../docs/DAY5_SPRING_BATCH.md`を参照してください。

```bash
cd kyocera-inventory
./gradlew :batch:test :batch:build

docker compose --profile batch run --rm batch \
  businessDate=2026-09-18
```

意図的失敗の`failOnItemCode`はJobInstanceを変えない非識別Parameterとして渡します。

```bash
docker compose --profile batch run --rm batch \
  businessDate=2026-09-18 \
  -failOnItemCode=ITEM002
```

Spring Batch 4.3のCLI規則では、Parameter名の`-` prefixが「非識別」を表します。

同じJobInstanceをRestart:

```bash
docker compose --profile batch run --rm batch \
  businessDate=2026-09-18 \
  -failOnItemCode=NONE
```

Reset SQLは`batch/scripts/reset-day5.sql`です。削除対象を確認してから、Guide記載のCommandで実行してください。
