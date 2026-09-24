# Day5: Spring Batch 日次入出庫実績集計

## 学習用Scenario

`stock_history`を指定日・商品・倉庫単位で集計し、`daily_stock_summary`へ保存します。実案件のJob、Table、運用方式を再現したものではありません。

```text
Job: dailyStockSummaryJob
└─ Step: dailyStockSummaryStep
   └─ Chunk Size 2
      ├─ Reader: stock_historyをSQL集計して読む
      ├─ Processor: businessDate／updatedAtを持つ出力Modelへ変換
      └─ Writer: daily_stock_summaryをUPDATEまたはINSERT
```

入力日付の境界は学習用としてUTCの`00:00:00以上、翌日00:00:00未満`です。実案件では業務Timezoneと締め時刻を確認する必要があります。

## Codeを初めて読む順番

1. `batch/src/main/java/.../job/DailyStockSummaryJobConfiguration.java`でJobとStepの全体像を見る。
2. 同Fileの`stockHistoryAggregateReader`でSQL、並び順、Job Parameterを見る。
3. `DailyStockSummaryProcessor.java`で変換と意図的Failure条件を見る。
4. `DailyStockSummaryWriter.java`でApplication Dataへの書込みとIdempotencyを見る。
5. `domain/StockHistoryAggregate.java`と`DailyStockSummary.java`で入出力を対応付ける。
6. `batch/src/main/resources/application.yml`でDataSource、Job名、Metadata初期化を見る。
7. `batch/scripts/reset-day5.sql`でSeedとReset対象を見る。
8. `DailyStockSummaryJobTest.java`で正常、Rollback、Restartの期待値を見る。

Job Configurationから外側を把握してからReader／Processor／Writerへ降りると、個別Classを先に読むより処理の位置付けを見失いにくくなります。

## Reader／Processor／Writer

### Reader

`JdbcCursorItemReader`が対象期間の`stock_history`を読みます。SQLで`item_code + warehouse_id`単位にGROUP BYし、IN合計、OUT合計、件数を算出します。集約自体をProcessorへ押し込まず、DBが得意な集合演算をSQLへ置いています。

Reader名とExecutionContextへ読取位置が保存されるため、Restart時は最後にCommitされた位置の次から再開できます。安定したRestartのためORDER BYを指定しています。

### Processor

Reader結果を`DailyStockSummary`へ変換します。非識別Job Parameterの`failOnItemCode`と商品Codeが一致した場合だけ、学習用Runtime Exceptionを発生させます。

### Writer

主Keyで既存行をUPDATEし、0件ならINSERTします。集計値を加算せず「再計算した絶対値」で置き換えるため、再処理で数量が倍になりません。

## ChunkとTransaction

Chunk Sizeは`2`です。

```text
2件Read → 2件Process → Writer → Commit
次の2件Read → 途中でError → Chunk全体Rollback
```

Transaction BoundaryはStepの各Chunkです。1件が失敗すると、そのChunk内のWriter更新とReaderのcheckpoint更新がRollbackされます。すでにCommitされた前Chunkは残ります。Restartは保存済みcheckpoint以降を処理します。

Online APIは1 Request内の在庫UPDATE＋History INSERTをTransactionにします。一方Batchは、大量処理を小さなChunkへ分割し、処理量とRollback範囲のバランスを取ります。

## Job ParameterとJobRepository

- `businessDate`: 識別Parameter。Job名と合わせてJobInstanceを識別する。
- `failOnItemCode`: 非識別Parameter。Failure／Restartの動作変更に使うがJobInstanceは変えない。

Spring Batch 4.3のCommand LineではParameter名の`-` prefixが非識別を意味します。

```text
JobInstance = Job名 + 識別Job Parameters
JobExecution = JobInstanceを実際に起動した1回
StepExecution = そのJobExecution内でStepを実行した記録
```

FAILED後に同じ`businessDate`で起動すると、同じJobInstanceに新しいJobExecutionが追加されます。COMPLETED後は同じ識別Parameterでは通常`JobInstanceAlreadyCompleteException`になり、再実行されません。

JobRepositoryはこれらの状態、Parameter、Count、ExecutionContextをBatch Metadata Tableへ保存します。

## Idempotencyは別の防御

Spring Batchの「COMPLETED JobInstance再実行拒否」はFrameworkの実行管理です。`daily_stock_summary`の主KeyとWriterの上書き方式はApplication Data側の二重計上防止です。

運用操作、Metadata削除、別Parameter、障害復旧などでFramework側の防御を越えて処理される可能性があるため、両方を分けて設計します。

## 0. Buildと起動準備

Repository rootで実行します。

```bash
cd kyocera-inventory
./gradlew :batch:test :batch:build
cd ..

docker compose -f kyocera-inventory/docker-compose.yml up -d --build kyocera-db backend-api
docker compose -f kyocera-inventory/docker-compose.yml --profile batch build batch
```

`batch` serviceはCompose profileへ分離しているため、通常のOnline環境起動だけではBatchが自動実行されません。

## 1. DB Reset

次のSQLはBatch Metadata、`daily_stock_summary`、`stock_history`を削除し、在庫数量と学習Historyを既知状態へ戻します。`item_master`、`warehouse`、`inventory` Table自体は削除しません。

```bash
docker compose -f kyocera-inventory/docker-compose.yml exec -T kyocera-db \
  psql -v ON_ERROR_STOP=1 -U kyocera -d kyocera_inventory \
  < kyocera-inventory/batch/scripts/reset-day5.sql
```

期待値は`stock_history=6`、`daily_stock_summary=0`です。別日のHistory 1件も含まれます。

## 2. 入力確認

```sql
SELECT sh.history_id, i.item_code, i.warehouse_id,
       sh.movement_type, sh.quantity, sh.processed_at
FROM stock_history sh
JOIN inventory i ON i.inventory_id = sh.inventory_id
ORDER BY sh.processed_at;
```

## 3. 正常実行

```bash
docker compose -f kyocera-inventory/docker-compose.yml --profile batch run --rm batch \
  businessDate=2026-09-18
```

Logの最終Statusが`COMPLETED`、Process exit codeが0であることを確認します。

```sql
SELECT *
FROM daily_stock_summary
ORDER BY item_code, warehouse_id;
```

期待値:

| item | warehouse | IN | OUT | count |
|---|---:|---:|---:|---:|
| ITEM001 | 1 | 5 | 2 | 2 |
| ITEM001 | 2 | 7 | 0 | 1 |
| ITEM002 | 1 | 10 | 4 | 2 |

別日IN 99は含まれません。

## 4. Metadata確認SQL

```sql
SELECT job_instance_id, job_name, job_key
FROM batch_job_instance
ORDER BY job_instance_id;

SELECT job_execution_id, job_instance_id, status, exit_code,
       start_time, end_time
FROM batch_job_execution
ORDER BY job_execution_id;

SELECT job_execution_id, key_name, type_cd, string_val, identifying
FROM batch_job_execution_params
ORDER BY job_execution_id, key_name;

SELECT step_execution_id, job_execution_id, step_name, status,
       read_count, write_count, commit_count, rollback_count,
       exit_code, start_time, end_time
FROM batch_step_execution
ORDER BY step_execution_id;
```

ExecutionContextの存在確認:

```sql
SELECT step_execution_id, short_context
FROM batch_step_execution_context
ORDER BY step_execution_id;

SELECT job_execution_id, short_context
FROM batch_job_execution_context
ORDER BY job_execution_id;
```

## 5. Failure → Restart

まずDB Resetを再実行します。次にITEM002で意図的に失敗させます。

```bash
docker compose -f kyocera-inventory/docker-compose.yml --profile batch run --rm batch \
  businessDate=2026-09-18 \
  -failOnItemCode=ITEM002
```

これは期待したFailureで、Process exit codeは5です。MetadataではFAILED、最初のChunkだけが残り、目安は次の値です。

```text
READ_COUNT=3
WRITE_COUNT=2
COMMIT_COUNT=1
ROLLBACK_COUNT=1
summary rows=2
```

同じ識別ParameterでFailure指定だけ解除します。

```bash
docker compose -f kyocera-inventory/docker-compose.yml --profile batch run --rm batch \
  businessDate=2026-09-18 \
  -failOnItemCode=NONE
```

同じJobInstance IDに新しいJobExecutionが作られ、残り1件を処理してCOMPLETEDになります。Summaryは3行で、先にCommitされた数量は倍増しません。

もう一度同じCommandを実行すると、COMPLETED済みなので`JobInstanceAlreadyCompleteException`となります。これは想定動作です。

## Manual Practice Checklist

1. Reset SQLを実行する。
2. `stock_history`の対象日／別日Dataを確認する。
3. 正常Jobを起動する。
4. Summary 3行と集計値を確認する。
5. 4種類のMetadata Tableを確認する。
6. Reset SQLを再実行する。
7. `-failOnItemCode=ITEM002`で起動する。
8. Process exit code 5とFAILEDを確認する。
9. Summary 2行、Count、ExecutionContextを確認する。
10. `-failOnItemCode=NONE`でRestartする。
11. 同じJobInstance内の新ExecutionがCOMPLETEDであることを確認する。
12. Summaryが3行になったことを確認する。
13. ITEM001の値が倍増していないことを確認する。

## 完了済みExercise: warehouseId Parameter

学習者が`warehouseId`を追加し、対象倉庫だけを集計できる状態まで実装済みです。以下はCode Review時の確認観点として残します。

変更候補:

- `DailyStockSummaryJobConfiguration.stockHistoryAggregateReader`: Parameter取得、SQL条件、PreparedStatementの順番。
- `DailyStockSummaryJobConfiguration.jobParametersValidator`: warehouseIdを必須にするか任意にするかを決める。
- `DailyStockSummaryJobTest.parameters`: 識別Parameterの組立て。
- 正常Test: 他倉庫が含まれない期待値。
- README／このGuide: Command例。

考える点:

- warehouseIdはJobInstanceを分ける識別Parameterにするか。
- 未指定時に全倉庫とするか、必須にするか。
- SQLへ文字列連結せずBind Parameterとして渡せているか。
- Restart時に同じwarehouseIdを指定しているか。

## 理解確認Question

1. Jobとは何か。
2. Stepとは何か。今回のJobには何Stepあるか。
3. JobとStepはどのような関係か。
4. Reader／Processor／Writerの責務は何か。
5. Chunkとは何か。
6. Chunk Size 2はCommit回数にどう影響するか。
7. Commitはいつ行われるか。
8. Error時にどの更新とcheckpointがRollbackされるか。
9. Job Parameterとは何か。
10. JobInstance／JobExecution／StepExecutionの違いは何か。
11. FAILEDとCOMPLETEDではRestart可否がどう違うか。
12. Restartとは何か。
13. RetryとRestartの時間軸と実行単位はどう違うか。
14. 同じ識別Job Parameterで再実行するとどうなるか。
15. Frameworkの再実行防止とApplication DataのIdempotencyは何が違うか。
16. Online TransactionとChunk TransactionのBoundaryはどう違うか。
17. JobRepositoryは何を保存しているか。
18. ReaderのORDER BYがRestartで重要なのはなぜか。

## Daily Meeting練習Template

```text
確認しているBatch:
- 日次入出庫実績集計Batchです。実案件仕様ではなく学習用Scenarioです。

Input / Output:
- stock_historyを読み、daily_stock_summaryへ日付・商品・倉庫単位の集計を保存します。

Job Parameter:
- businessDateが識別Parameter、failOnItemCodeは障害練習用の非識別Parameterです。

現在確認したStep:
- dailyStockSummaryStepのReader／Processor／WriterとChunk Size 2を確認しました。

Error時:
- 最初の2件はCommit済みで、ITEM002を含む次ChunkがRollbackされました。

Restart後:
- 同じJobInstanceの新しいJobExecutionで残り1件を処理し、COMPLETEDになりました。

DB確認:
- Summary、JobInstance、JobExecution、StepExecution、Count、ExecutionContextを確認しました。

不明点:
- （自分で未確認のSQL、Count、Code箇所を書く）
```
