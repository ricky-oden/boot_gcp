# Spring Batch ミニハンズオン

## 構成

```text
Job: pendingApplicationApprovalJob
  Step: approvePendingApplications
    Reader: PENDING申請を読む
    Processor: APPROVEDへ変更（指定titleで1回だけ意図的失敗可）
    Writer: application更新 + history追加
    chunk size: 2
```

Chunkの境界ごとにtransactionが張られる。たとえば3件目で失敗した場合、先にcommit済みのchunkは残り、失敗chunkはrollbackされる。再起動は同じJob ParametersでFAILED instanceを再開し、PENDINGだけを再読込する。

## テストする場面

1. 正常完了: 2件がAPPROVED、履歴2件。
2. 意図した失敗: `failOnTitle`でFAILED。
3. restart: 同じparametersでCOMPLETED。
4. 完了済み二重起動: `JobInstanceAlreadyCompleteException`。
5. 新しいinstance: PENDINGがないため履歴は増えずidempotent。

```bash
cd backend
mvn -Dtest=BatchLabIntegrationTest test
```

このReaderは教材を小さくするため、step開始時点のPENDING一覧をメモリに載せる。大量データの本番ではpaging/cursor reader、skip/retry方針、監視、並列性を別途設計する。
