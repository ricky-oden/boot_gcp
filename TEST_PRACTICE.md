# Test実践ガイド（1〜2時間）

## この練習の目的

「申請承認」の1機能に絞り、Unit Test、CI、Local結合Test、GCP確認を1本の流れで体験します。商用システム向けの網羅的なTest設計は目的にしません。

実案件では、既存のGitHub Actions Workflowを利用し、PR時のTest / Build結果を確認する立場でした。このRepositoryの`.github/workflows/ci.yml`は学習用に作成したもので、実務でPipelineを構築した実績としては扱いません。

GCPも同様です。このフォルダのGCP環境は「Staging相当の学習環境」であり、実案件のStaging環境そのものではありません。実務での立場はGCP Infrastructureの設計・構築主体ではなく、Application側のDeploy、設定確認、Log確認です。

## Branchの使い分け

- `completed`: 練習用Testまで完成した正解版。迷ったら差分を見る。
- `main`: `completed`と同じ完成版を指す。GitHubの既定Branch用。
- `test-practice`: 実際に作業するBranch。BackendとFrontendに各1か所のTODOがある。

学習開始前に確認:

```bash
git branch --show-current
git status
```

`test-practice`でなければ:

```bash
git switch test-practice
```

## 推奨時間配分

1. Backend Unit Test: 20分
2. Frontend Unit Test: 20分
3. Localで両Test実行: 10分
4. Git Commit / Push / GitHub Actions: 15分
5. Docker Compose結合Test: 25分
6. GCP確認: 15分

---

## 1. Backend Unit Test

対象:

`backend/src/test/java/com/example/workflow/service/ApplicationApprovalPracticeTest.java`

確認する関係:

```text
ApplicationService       本物
ApplicationRepository    Mockito Mock
HistoryRepository        Mockito Mock
```

Testの流れ:

1. Arrange: PENDINGのApplicationと`findById`のMock戻り値を準備する。
2. Act: 本物の`applicationService.approve(1L)`を呼ぶ。
3. Assert: StatusがAPPROVEDで、ApplicationとHistoryが保存されたことを確認する。

### 自分で埋める場所

`test-practice` BranchではAssert部の1行がTODOになっています。次の意味になるAssertionを自分で書いてください。

```text
response.status()がApplicationStatus.APPROVEDと等しい
```

迷った場合だけ完成版と比較:

```bash
git diff completed -- backend/src/test/java/com/example/workflow/service/ApplicationApprovalPracticeTest.java
```

Backend Testだけ実行:

```bash
docker build --target build -t workflow-backend-test ./backend
```

Mavenを直接使える環境またはDevContainerの中:

```bash
cd backend
mvn test
```

期待結果: `BUILD SUCCESS`。

### 異常系を1つ読む

`ApplicationServiceTest.approvedApplicationCannotBeApprovedAgain`を読みます。APPROVED済みの申請は`AlreadyApprovedException`となり、ApplicationとHistoryの`save`は呼ばれません。ここで`assertThrows`と`never()`を確認します。

---

## 2. Frontend Unit Test

対象:

`frontend/src/App.practice.test.tsx`

確認する要素:

- `render`: MemoryRouter内にAppを表示する。
- `screen`: ユーザーが見る「承認」buttonをRoleとNameで探す。
- Mock: `axios.get` / `axios.post`をJest Mockに差し替える。
- ユーザー操作: `userEvent.click`でbuttonを押す。
- Assertion: 承認APIが正しいURLで呼ばれたことを確認する。

### 自分で埋める場所

`test-practice` Branchでは最後のAssertionがTODOになっています。次の意味になるAssertionを書いてください。

```text
axiosMock.postが http://localhost:8080/applications/1/approve で呼ばれた
```

迷った場合:

```bash
git diff completed -- frontend/src/App.practice.test.tsx
```

Dependencyを初回だけInstall:

```bash
cd frontend
npm ci
```

Frontend Test実行:

```bash
npm test
```

期待結果: 全7CaseがPASS。

---

## 3. Local TestからGitHub Actionsまで

TODOを埋め、BackendとFrontendがLocalで成功した後に進みます。

```bash
git status
git add backend/src/test/java/com/example/workflow/service/ApplicationApprovalPracticeTest.java \
        frontend/src/App.practice.test.tsx
git commit -m "Complete unit test practice"
git push -u origin test-practice
```

GitHub上で:

1. Repositoryの`Actions`を開く。
2. `Unit tests`を開く。
3. `Backend JUnit`と`Frontend Jest`の両Jobを確認する。
4. BackendのJUnit / Maven buildとFrontendのJest / Vite buildが緑で完了したことを確認する。
5. 失敗時は、最初に赤くなったStepとError messageを読む。

Remote Repositoryが未設定の場合は、自分のPrivate RepositoryをGitHubで作成し、表示される`git remote add origin ...`を実行します。案件資料やGCP Screenshotは`.gitignore`で除外されていることを`git status --ignored`で必ず確認してください。

---

## 4. Local結合Test

### 対象Scenario

```text
申請登録
  -> 一覧表示
  -> 承認
  -> StatusがAPPROVED
  -> application_historiesに履歴が1件追加
```

### 前提Data

- 新規に始める場合、事前Dataは不要。
- 同じtitleがあっても区別できるよう、`Test申請-YYYYMMDD-HHMM`のようなtitleを使う。
- 既存のLocal DB Dataを消す必要はない。完全に初期化する場合だけ`docker compose down -v`を使う。

### 起動

Project Rootで:

```bash
docker compose up --build
```

別TerminalでLogを見る:

```bash
docker compose logs -f backend
```

### Browserでの手順とExpected Result

1. `http://localhost:3000`を開く。
   Expected: 「法人向け申請ワークフロー」と申請一覧が表示される。
2. 一意な申請titleを入力し、「登録」を押す。
   Expected: 一覧にtitle、PENDING、承認buttonが表示される。
3. Browser Developer ToolsのNetworkで`POST /applications`を開く。
   Expected: Request bodyは`{"title":"..."}`、Statusは201、Responseはid / title / PENDING / createdAt。
4. 「承認」を押す。
   Expected: `POST /applications/{id}/approve`は200。一覧のStatusがAPPROVEDとなり、承認buttonが消える。

### APIをcurlで確認する場合

```bash
curl -i http://localhost:8080/applications
curl -i -X POST http://localhost:8080/applications \
  -H 'Content-Type: application/json' \
  -d '{"title":"Local結合Test申請"}'
curl -i -X POST http://localhost:8080/applications/1/approve
```

IDは登録Responseの値に置き換えます。

### PostgreSQLでの確認

```bash
docker compose exec db psql -U workflow -d workflow
```

```sql
SELECT id, title, status, created_at
FROM applications
ORDER BY id DESC;

SELECT id, application_id, action, created_at
FROM application_histories
ORDER BY id DESC;
```

Expected:

- `applications.status` = `APPROVED`
- `application_histories.application_id` = 承認したApplication ID
- `application_histories.action` = `APPROVED`

psqlを終了:

```text
\q
```

### Backend Log

```bash
docker compose logs backend | grep -E 'Application created|Approval started|Approval succeeded'
```

Expected:

- `Application created`
- `Approval started`
- `Approval succeeded`

---

## 5. 意図的な不具合練習

対象: 同じApplicationを2回承認する。データ破壊は行いません。

### 切り分けの順番

1. 前提条件
   1回目の承認が成功し、DB上のStatusがAPPROVEDであること。
2. 操作
   Browserかcurlで同じIDの`POST /applications/{id}/approve`をもう1回実行する。
3. Network Request / Response
   MethodとIDが正しいことを確認。ExpectedはHTTP 409と「既に承認済みの申請です」。
4. DB
   ApplicationはAPPROVEDのまま。同じ操作による新しいHistoryは追加されない。
5. Backend Log
   `Approval failed: already approved`とIDを確認。
6. Code
   `ApplicationService.approve`のStatus判定と`AlreadyApprovedException`、`GlobalExceptionHandler`の409変換を読む。

この順番にする理由は、いきなりコードを変更せず、Data、Request、DB、Logの事実から原因を狭めるためです。

---

## 6. GCPのStaging相当学習環境で確認

新しいGCP Resourceは作りません。既存の次のResourceだけを使います。

- Project: `workflow-gcp-learn-260909-rk82`
- Cloud Run: `workflow-api`
- Cloud SQL: `workflow-db`
- Artifact Registry: `workflow-repo`
- Region: `asia-northeast1`

2026-09-11にChromeのCT用Profileで再確認済みです。Cloud Runは正常、service minimum 0、revision maximum 1、concurrency 20、request-based billingです。Cloud SQLは利用可能なPostgreSQL 16 / Enterprise / 10GB / asia-northeast1-a、Artifact RegistryはDocker Repository `workflow-repo`（128.6MB、scan無効）です。Cloud SQLが存在する間は課金が続くため、実習の直前にも再確認してください。

Cloud Shellで:

```bash
gcloud config set project workflow-gcp-learn-260909-rk82
gcloud config set run/region asia-northeast1
SERVICE_URL=$(gcloud run services describe workflow-api --format='value(status.url)')
ID_TOKEN=$(gcloud auth print-identity-token)
```

### 1. Cloud Run APIを呼ぶ

```bash
curl -i -H "Authorization: Bearer ${ID_TOKEN}" "${SERVICE_URL}/applications"
```

Expected: HTTP 200。認証なしは403になる構成。

### 2. Cloud SQLへの保存を確認

```bash
curl -i -X POST \
  -H "Authorization: Bearer ${ID_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"title":"GCP Test Practice"}' \
  "${SERVICE_URL}/applications"

curl -i -H "Authorization: Bearer ${ID_TOKEN}" "${SERVICE_URL}/applications"
```

Expected: POSTは201。その後の別GET Requestで`GCP Test Practice`が取得できる。Cloud RunのInstance内H2ではなく、最終Revisionが`cloud-sql` ProfileであることもCloud RunのRevision設定で確認する。

### 3. Cloud LoggingでApplication Logを確認

Logs Explorer Query:

```text
resource.type="cloud_run_revision"
resource.labels.service_name="workflow-api"
```

`Application created`、Timestamp、Severity、Revision nameを確認する。

### 4. Errorから原因を追う

存在しないIDを使う安全な練習:

```bash
curl -i -X POST \
  -H "Authorization: Bearer ${ID_TOKEN}" \
  "${SERVICE_URL}/applications/999999/approve"
```

Expected: HTTP 404。Logs Explorerで`Approval failed: application not found`を検索し、Requestの時刻とRevisionを対応付ける。新RevisionやCloud SQL Instanceは作成しない。

参照: `GCP_画面操作_面談対策.xlsx`

---

## 7. DevContainerでTestを実行

1. MacでDocker Desktopを起動する。
2. VS CodeでこのProject Rootを開く。
3. Command Paletteで`Dev Containers: Reopen in Container`を実行。
4. 初回buildと`npm ci`の完了を待つ。
5. Container内Terminalで実行:

```bash
cd backend && mvn test
cd ../frontend && npm test
```

Docker CLIはHostのDocker Engineを使う設定です。必要であればProject Rootで`docker compose up --build`も実行できます。

---

## 8. 面談で30秒で説明する

「申請承認処理に対し、BackendはApplicationServiceを本物、JPA RepositoryをMockitoのMockにし、Status更新と保存呼び出しをJUnitで確認しました。FrontendはAxiosをMockし、React Testing LibraryでPENDING申請の承認buttonを操作し、正しいAPIが呼ばれることを確認しました。その後、Docker Composeで画面からSpring Boot、PostgreSQL、承認履歴まで結合Testしました。GitHub Actionsについては、実務では既存WorkflowのTest / Build結果を確認する利用側でした」

---

## 9. 最終チェックリスト（1〜2時間）

- [ ] Spring Boot Unit Testを1Case自分で完成
- [ ] JUnit Test実行
- [ ] React Unit Testを1Case自分で完成
- [ ] Jest Test実行
- [ ] Commit / Push
- [ ] GitHub ActionsのCI成功確認
- [ ] Docker ComposeでApplication起動
- [ ] Reactから申請を登録
- [ ] 承認操作
- [ ] Browser Network確認
- [ ] PostgreSQL確認
- [ ] Backend Log確認
- [ ] 意図的なErrorを発生
- [ ] 原因を切り分け
- [ ] GCP Cloud Run API確認
- [ ] Cloud SQL確認
- [ ] Cloud Logging確認
- [ ] VS Code DevContainerで開く
- [ ] Container内でTest実行

最初に開くfileは `TEST_PRACTICE.md`、最初のcommandは `git switch test-practice` です。
