# Transaction / Rollback 学習

## 30秒回答

`@Transactional`で申請更新と履歴追加を同じトランザクションに入れます。正常終了ならcommitされ、途中で実行時例外が出れば両方rollbackされます。管理中Entityはsetter後に明示的なsaveを呼ばなくてもdirty checkingでcommit時にUPDATEされます。ただしトランザクション外やdetached Entityでは同じ前提を置けません。

## 実験コード

- `ApprovalTransactionLabService#approveUsingDirtyChecking`: `application.approve()`後にRepositoryのsaveを呼ばない。
- `TransactionRollbackIntegrationTest`: 履歴RepositoryだけをSpyにし、学習用例外を投げる。
- 例外後にDBを再読込して、申請がPENDING、履歴が0件であることを確認する。

```text
BEGIN
  SELECT application
  Entity status: PENDING -> APPROVED  (dirty)
  INSERT history                  <- ここで失敗
ROLLBACK
結果: application=PENDING / history=0
```

本番APIに障害注入用フラグは追加していない。失敗注入はテストのSpyだけなので安全に繰り返せる。

```bash
cd backend
mvn -Dtest=TransactionRollbackIntegrationTest test
```
