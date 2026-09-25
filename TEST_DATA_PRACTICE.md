# テスト前提データ練習

テスト失敗を読む前に、Arrangeでどの状態を作ったかを言葉にする。

| ケース | 前提条件 | 操作 | 期待結果 | DB確認 | Evidence |
|---|---|---|---|---|---|
| 一覧 | PENDING 1件 | GET | 200、配列1件 | applications 1件 | Response JSON |
| 登録 | 申請なし、一般User | 有効titleでPOST | 201、PENDING | application追加 | Network + SQL |
| Validation | 一般User、関連Data不要 | 空titleでPOST | 400 | 行追加なし | Error JSON + count |
| 正常承認 | PENDING 1件、承認Role、履歴0件 | approve | 200、APPROVED、履歴1 | status/history | Response + Log + SQL |
| 権限不足の想定 | PENDING 1件、一般User | approve | 将来は403 | 現実装にUser/Role認証なし | 制約として記録 |
| 関連Data不足の想定 | PENDINGだが必須添付なし | approve | 業務仕様次第で422等 | 現実装に添付Entityなし | 制約として記録 |
| 404 | 対象IDなし | approve | 404、履歴0 | 対象なし | Response + Log |
| 409 | APPROVED 1件、既存履歴1件 | approve | 409、履歴増えない | 履歴1のまま | Response + count |
| rollback | PENDING 1件、履歴saveを失敗化 | lab approve | 例外、PENDING、履歴0 | 両table再読込 | JUnit assertion |
| Batch restart | PENDING 3件、3件目で一度失敗 | 同一parameters再実行 | 最終的に全件APPROVED、履歴は各1 | status/history count | JobExecution + SQL |

Repository/統合テストは履歴→申請の順に削除する。外部キーがあるため親を先に消さない。テストごとにデータを初期化し、実行順へ依存させない。

User Roleや添付等の関連Dataは現行の小型domainには未実装。存在しない仕様をtestしたふりにせず、本番規模ではFixtureに含めるべき観点として明示している。
