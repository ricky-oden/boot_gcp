# 不具合切り分け練習

各問は先に「どこまで正常か」を答えてから解説を読む。共通順序は、前提データ → 操作 → Browser Network → Response → DB → Log → Code。

## A. 存在しない申請の承認が404

### 問題

ID 999を承認すると404。障害か仕様か。どの層まで確認するか。

### 解説

1. 前提データ: DBに999がないことをSELECT。
2. 操作: `POST /applications/999/approve`。
3. Network: URL/methodが正しいか。
4. Response: 404と「申請が見つかりません」。
5. DB: 更新・履歴追加なし。
6. Log: application not found。
7. Code: Serviceの`orElseThrow`→Adviceの404。仕様どおり。

## B. 二重承認が409

### 問題

1回目200、2回目409。なぜ400でも500でもないか。

### 解説

前提はAPPROVED。Networkの2回の時系列を確認し、Response 409を確認する。DBはAPPROVEDのまま、履歴は1件。Serviceの状態判定が`AlreadyApprovedException`、AdviceがConflictへ変換する。入力形式ではなく現在状態との競合なので409。

## C. 空titleが400

### 問題

DBやServiceへ到達しているか。

### 解説

Network request bodyが空文字であることを確認。Responseは400。DBに行追加なし。Controller引数の`@Valid`とDTOの`@NotBlank`でService前に止まる。Controller testでService未呼出しも確認できる。

## D. 予期しない500

### 問題

利用者へstack traceを返さず、開発者は原因をどう追うか。

### 解説

Networkのrequest ID・時刻とResponse 500を控える。DBの部分更新有無を調べ、同時刻のserver logでstack traceを検索する。`GlobalExceptionHandler`は利用者へ固定メッセージ、server logへ例外詳細を残す。原因をController/Service/Repository/infraへ絞る。秘密値はResponseにもlogにも出さない。

## E. APIは200だが画面が古い

### 問題

バックエンド、フロント、キャッシュのどこを疑うか。

### 解説

登録/承認POSTが200でも、その後のGETが実行されたかNetworkで確認する。GET Responseが新しければUI state/filter/renderの問題、古ければDB/API側。DBを直接確認し、server logのGET時刻と照合する。本アプリならRedux filterがAPPROVED/PENDINGに偏っていないか、`loadApplications`後にstateが更新されたかを確認する。

## 面談用の型

「まず再現条件と期待仕様を合わせ、Networkでrequest/responseの境界を決めます。次にDBの事実と同時刻のlogを照合し、最後に該当codeへ絞ります。修正後は同じ条件の自動testを追加します。」
