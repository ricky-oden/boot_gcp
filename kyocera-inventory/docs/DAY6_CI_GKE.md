# Day6: CI・Code Quality・GKE確認 Practice Guide

## 目的と前提

このDayは新しい業務機能を追加せず、Localで動くCodeをPR時に自動検証し、失敗したJob／Step／Logから原因を切り分ける練習です。Checkstyle、CI、Kubernetes manifestは学習用であり、実案件の設定を再現したものではありません。

既存の`.github/workflows/ci.yml`は申請Workflow Application用です。削除・変更せず、京セラ領域専用に`.github/workflows/kyocera-ci.yml`を追加しています。

## LocalでCI相当処理を実行する

DevContainer内でRepository rootから実行します。

```bash
cd kyocera-inventory

./gradlew :backend-api:openApiValidate
./gradlew :backend-api:checkstyleMain :backend-api:checkstyleTest
./gradlew :backend-api:test :backend-api:jacocoTestReport :backend-api:build

./gradlew :batch:checkstyleMain :batch:checkstyleTest
./gradlew :batch:test :batch:jacocoTestReport :batch:build

cd frontend-pc
npm ci
npm test -- --ci
npm run build

cd ../frontend-mobile
npm ci
npm test -- --ci
npm run build
```

Checkstyle Report:

- `backend-api/build/reports/checkstyle/main.html`
- `batch/build/reports/checkstyle/main.html`

JaCoCo HTML Report:

- `backend-api/build/reports/jacoco/test/html/index.html`
- `batch/build/reports/jacoco/test/html/index.html`

JaCoCoは未Test箇所を見つける観察道具として使います。Coverage率を目的化しないため、最低CoverageによるBuild Failureは設定していません。

OpenAPI生成CodeはYAMLから再生成され、直接修正しません。そのためCheckstyleは手書きの`backend-api/src/main/java`を対象とし、`build/generated/openapi`は対象外です。

## Test層をCodeから読む

| Test層 | 実File例 | Mock | DB | 保証する範囲 |
|---|---|---|---|---|
| Controller Test | `HealthControllerTest`、`InventoryControllerTest`、`StockMovementControllerTest` | ServiceまたはJdbcTemplateを`@MockBean` | 使わない | HTTP Status、Parameter、Request／Response JSON、Exception変換 |
| Service Unit Test | `InventoryServiceTest`、`StockMovementServiceTest` | MapperをMockitoでMock | 使わない | 業務分岐、Validation、Mapper呼出し、Model変換 |
| Mapper Integration Test | `InventoryMapperTest` | なし | H2 | Mapper Interface→MyBatis XML→SQL、JOIN、動的条件 |
| Transaction Test | `StockMovementTransactionTest` | なし | H2 | UPDATEとHistory INSERTのCommit／Rollback |
| Batch Test | `DailyStockSummaryJobTest` | なし | H2＋Batch Metadata | Job Parameter、Chunk結果、FAILED、checkpoint Restart、二重計上防止 |
| Frontend Component／State Test | PCとMobileの`*.test.ts(x)` | API ClientまたはAxios | 使わない | 入力、Dispatch、Loading／Success／Error、画面更新 |

Unit Testは対象Classを狭く速く確認します。Integration Testは複数LayerやFramework設定を含む代わりに、失敗原因の範囲が広くなります。

## Checkstyle Failure Exercise

最終状態へ残さない一時変更です。

1. `backend-api/src/main/java/com/example/kyocera/inventory/health/HealthController.java`へ未使用の`import java.util.List;`を追加する。
2. `./gradlew :backend-api:checkstyleMain`を実行する。
3. `UnusedImports`、File、Lineを読む。
4. HTML Reportも開く。
5. importを削除する。
6. 同じCommandを再実行して成功を確認する。

名前違反を試す場合はLocal Variableを`Bad_Name`のように一時変更できます。コンパイルエラーとCheckstyleエラーを混同しないよう、最初に失敗したTask名を確認します。

## JUnit Failure Exercise

1. `backend-api/src/test/java/com/example/kyocera/inventory/health/HealthControllerTest.java`の期待Statusを一時的に成功値と異なる値へ変える。
2. `./gradlew :backend-api:test --tests '*HealthControllerTest'`を実行する。
3. Failed Test名、期待値、実値、Stack Traceの最初のApplication/Test行を確認する。
4. `backend-api/build/reports/tests/test/index.html`を開く。
5. 期待値を戻し、同じTestを再実行する。

Mockitoの失敗を試す場合は`InventoryServiceTest`の`verify`条件を一時的に変え、WantedとActual invocationの違いを読みます。

## Frontend Failure Exercise

1. `frontend-mobile/src/pages/StockOperationPage.test.tsx`の表示期待値を一時的に存在しない文言へ変える。
2. `npm test -- --ci`を実行する。
3. 失敗したTest名、Testing Libraryが表示するDOM、最初のAssertionを確認する。
4. 変更を戻してTestを再実行する。

Build FailureとTest Failureは別です。TypeScriptの型を一時的に崩した場合は`npm test`が通っても`npm run build`で失敗する可能性があります。

## GitHub Actions構成

`Kyocera inventory CI`には4 Jobあります。

| Job | 主なStep |
|---|---|
| backend | Checkout、Java 17、Gradle、OpenAPI Validate、Checkstyle、Test／JaCoCo、Build、Artifact upload |
| batch | Checkout、Java 17、Gradle、Checkstyle、Test／JaCoCo、Build、Artifact upload |
| frontend-pc | Checkout、Node 20、npm ci、Jest、Vite Build |
| frontend-mobile | Checkout、Node 20、npm ci、Jest、Vite Build |

`kyocera-inventory/**`またはこのWorkflow自体が変わったpush／pull requestだけを対象にします。Jobを分けることでBackend、Batch、PC、Mobileのどこが壊れたかを一覧で判断できます。

用語：

- Workflow: YAMLで定義する自動処理全体。
- Job: 同じRunner上で順に動くStepの集合。Job同士は原則独立・並列。
- Step: CommandまたはAction 1つ分の処理。
- Action: CheckoutやJava Setupなど再利用可能な処理。
- GitHub-hosted Runner: GitHubがJobごとに用意する一時VM。
- Exit Code: `0`は成功、非0は失敗。最初の非0 Stepから調査する。
- Cache: 依存関係を再利用して時間を短縮するが、成果物そのものではない。
- Artifact: ReportやJARなど、そのWorkflow RunからDownloadできる保存物。
- Environment Variable: 実行中の設定値。機密情報には使い方の注意が必要。
- Secret: GitHubに暗号化保存し、Workflowへ必要時だけ渡す機密値。Logへ出さない。
- CI: 変更を継続的にBuild／Testして統合可能か検証する。
- CD: 検証済み成果物を配布・Deployする段階。今回のWorkflowには含めない。

## GitHub UIで失敗を調べる

通常のRun：

```text
Repository → Actions → Kyocera inventory CI → Workflow Run
→ 赤いJob → 最初に失敗したStep → Error Log
```

PR：

```text
Pull Request → Checks → Failed Check → Details
→ Job → 最初に失敗したStep
```

見る順番は、Job名、Step名、実行Command、最初のError、対象File／Line、後続の二次Errorです。大量のStack Trace末尾だけを読むのではなく、最初の具体的な失敗へ戻ります。

実際のGitHub Actions RunにはRemote pushが必要です。対象は`kyocera-catchup`、Remoteは`origin`です。壊した状態のpushやPR作成は自動では行わず、実施前に確認します。

## CI Failure Exercise A／B／C

- A: 上記の未使用importでBackend Checkstyle Jobを失敗させる。
- B: JUnitの期待値を変え、BackendのTest Stepを失敗させる。
- C: Frontend Testの期待文言を変え、Mobile Jobを失敗させる。

Localで原因を理解した後、学習者がExercise用Commitをpushし、Actions画面でFailed Stepを特定します。修正Commitをpushして全Job Greenを確認します。壊れた変更を最終Branchへ残しません。

## PR TemplateとReview Practice

Repository rootの`.github/pull_request_template.md`を使います。Review指摘の練習例：

1. 在庫更新APIに負数／在庫不足の境界値Testが足りない、と指摘された場合にService TestとHTTP Testのどちらへ追加するか判断する。
2. MyBatis XMLで`warehouseId`未指定時にも条件が残っている、と指摘された場合に生成SQLとMapper Integration Testを確認する。
3. Controllerが業務ValidationやDB処理まで担当している、と指摘された場合にController／Service／Mapperの責務へ分けて説明する。

悪い実装は実Codeへ残さず、PR Conversationで「事実、影響、修正方針、確認結果」の順に回答します。

## Docker Image／Artifact Registry／GKE Flow

```text
Source
  → Checkstyle / Test / Build
  → JAR（CI Artifactとして確認可能）
  → DockerfileでContainer Image化
  → Artifact RegistryへTag付きPush
  → GKE DeploymentがImageを指定
  → Replica数に応じてPodが起動
  → ServiceがPodへ通信を転送
```

Day6では実Artifact RegistryへPushしません。既存の`GCP_画面操作_面談対策.xlsx`の`04_ArtifactRegistry`も再利用し、Backend ImageはLocalで確認します。

```bash
docker build -f kyocera-inventory/backend-api/Dockerfile \
  -t kyocera-backend-api:day6 kyocera-inventory
docker image inspect kyocera-backend-api:day6
```

RegistryではRepositoryがImageを保持し、Tagは人が扱いやすい参照、Digestは内容に結び付く不変参照です。Productionでは`latest`依存を避け、Commit SHAやDigestを使う理由を説明できるようにします。

## GKE／kubectl Practice

学習用manifestは`k8s/backend-api.yaml`です。関係とCommandは`k8s/README.md`を参照します。実GKE Clusterは作成せず、適用もしません。

Podが起動しない場合：

```text
kubectl get pods
→ READY / STATUS / RESTARTSを確認
kubectl describe pod <name>
→ Events、Image Pull、Probe、Environmentを確認
kubectl logs <name> -c backend-api
→ Application起動LogとStack Traceを確認
kubectl logs <name> -c backend-api --previous
→ 再起動前ContainerのLogを確認
```

`CrashLoopBackOff`は原因名ではなく、Containerが繰り返し終了して待機時間が増えている状態です。

## Cloud Logging調査Flow

既存の`GCP_画面操作_面談対策.xlsx`、特に`02_Logging`と既存Cloud Run Error教材を再利用します。新規GCP Resourceは作りません。

```text
画面Error
→ Browser NetworkでStatus / Response / Request情報
→ 発生時刻、Service、Revision、可能ならRequest IDを記録
→ Logs ExplorerでCloud Run RevisionとService名を絞る
→ Request LogとApplication Logを同時刻で照合
→ Stack Traceの最初のApplication Class
→ Service / Mapper / SQL
→ DB Data・接続状態
```

秘密値、Token、Passwordを検索条件・Screenshot・Issueへ貼らないようにします。

## GCSの最低限理解

Day6ではGCS ClientやEmulatorを業務Flowへ追加しません。

```text
Application → GCS Client → Bucket → Object
```

- Upload: Bucket名とObject名を指定してByte列／Streamを保存する。
- Download: Objectを取得し、存在しない場合や権限不足を区別する。
- Object確認: Consoleまたは`gcloud storage ls gs://...`で名前、Size、更新時刻を確認する。
- Error調査: Application LogでStatus、例外型、Bucket／Object名を確認する。Credential内容は出さない。

既存Bucketが安全に用意されている場合だけ既存教材の範囲で確認し、新しいBucketやIAMは作成しません。

## 面談用Question

1. GitHub Actionsとは何か。CIとCDは何が違うか。
2. Workflow、Job、Step、Action、Runnerの関係は何か。
3. CheckstyleはCompilerやJUnitと何が違うか。
4. Test FailureとBuild FailureをLog上でどう区別するか。
5. JaCoCo Reportから何を判断し、何を判断しないか。
6. CacheとArtifactは何が違うか。
7. Docker Imageはどの段階で作られ、Artifact Registryは何を保持するか。
8. GKEのDeployment、Pod、Container、Service、Replicaの関係は何か。
9. Podが起動しない場合、なぜ`get → describe → logs`の順で見るか。
10. 画面ErrorからCloud Logging、Code、DBまでどう追うか。

## Daily Meeting Template

```text
昨日／今回: 京セラ用CIの【Job名】を確認しました。
CI状況: 【Green / Failed】、失敗Stepは【Step名】です。
Log確認: 【File:Line / Test名 / Error】まで切り分けました。
原因: 【Style / Test期待値 / Build / 環境】です。
対応: 【修正済み / 調査中】、再実行結果は【結果】です。
Deployment確認: 【Image / Pod / Probe / Environment / Log】を確認します。
Blocker・不明点: 【内容。なければなし】です。
```

## Day6完了後に学習者が行うこと

1. Checkstyle違反をLocalで作り、Reportから修正する。
2. JUnit失敗をLocalで作り、Test Reportから修正する。
3. Remote push前に対象Branchと差分を再確認する。
4. 承認後にGitHub Actions上で1種類ずつ失敗とGreen復旧を確認する。
5. PR Templateを埋め、ChecksとReview Conversationを練習する。

Day7のTicket→設計→実装→Test Evidence→報告の通し演習は、Day6では開始しません。
