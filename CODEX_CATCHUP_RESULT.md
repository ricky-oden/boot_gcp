# 2/2面談向け Catch-up Lab 実装結果

実施日: 2026-09-12  
Branch: `catchup-labs`  
方針: Local Docker / Spring Boot / React / PostgreSQLのみ。GCP resource、課金設定、Secret/Credentialは変更していない。

## 結果

- Backend: Docker build内の`mvn package`成功。JUnit 21件成功。
- Frontend: Jest 10件成功（3 suites）。`npm run build`成功。
- Compose: `db` healthy、`backend`/`frontend`起動中。
- 既存JPA API: GET 200、POSTで登録、approve 200、二重approve 409、空title 400。
- PostgreSQL: 回帰確認申請がAPPROVED、承認履歴1件をSQLで確認。
- MyBatis: local-labs endpointからPostgreSQLへINSERT/SELECT/条件付きUPDATE成功。
- OpenAPI: `/v3/api-docs` 200、指定3 endpoint/schema/statusを確認。Frontend `http://localhost:3000`も200。
- `git diff --check`: 問題なし。

## 追加・変更File

### Backend実装

- `backend/pom.xml`: Spring Batch、MyBatis starter、springdoc、batch-test。
- `backend/src/main/java/com/example/workflow/controller/ApplicationController.java`: OpenAPI annotation。
- `backend/src/main/java/com/example/workflow/controller/GlobalExceptionHandler.java`: 安全な500 responseとserver-side stack trace。
- `backend/src/main/java/com/example/workflow/config/OpenApiConfig.java`: API metadata。
- `backend/src/main/java/com/example/workflow/service/ApprovalTransactionLabService.java`: dirty checking/rollback専用経路。
- `backend/src/main/java/com/example/workflow/repository/*Repository.java`: status/history query method。
- `backend/src/main/java/com/example/workflow/mybatis/`: row model、Mapper、Service、local-only Controller。
- `backend/src/main/resources/mappers/ApplicationMapper.xml`: SELECT/INSERT/UPDATE/WHERE/ResultMap/binding。
- `backend/src/main/java/com/example/workflow/batch/`: Job/Step/Reader/Processor/Writer、1回失敗tracker。
- `backend/src/main/resources/application.yml`: Batch自動実行停止、MyBatis、Swagger UI設定。
- `backend/src/main/resources/application-local-labs.yml`: Local Batch metadata schema初期化。
- `backend/src/test/resources/application-test.yml`: H2 PostgreSQL modeとBatch test設定。
- `docker-compose.yml`: Backendだけ`local-labs` profileを有効化。

### Backend test

- `ApplicationControllerTest`: MockMvc + mocked Service、200/201/400/404/409/500とJSON。
- `ApplicationRepositoryTest`: `@DataJpaTest`、save/find/status query/ManyToOne mapping。
- `ApplicationIntegrationTest`: Controller→DB、登録→承認→履歴、二重承認。
- `TransactionRollbackIntegrationTest`: commit/dirty checking/履歴失敗rollback。
- `MyBatisApplicationMapperIntegrationTest`: XML CRUD mapping。
- `BatchLabIntegrationTest`: success/failure/restart/二重起動/idempotency。
- `OpenApiIntegrationTest`: generated API contract。

既存の`ApplicationServiceTest`と`ApplicationApprovalPracticeTest`も削除・変更せず成功している。

### Frontend

- `frontend/package.json`, `package-lock.json`: Redux Toolkit / React Redux。
- `frontend/src/store/`: store、slice、initial state、reducer/action、typed hooks、slice test。
- `frontend/src/App.tsx`: 状態filterのselector/dispatch。API data/loading/errorはuseState、formはReact Hook Formのまま。
- `frontend/src/main.tsx`: Redux Provider。
- `frontend/src/App.test.tsx`, `App.practice.test.tsx`: test用storeとfilter test。
- `frontend/src/style.css`: filter表示。

### 学習Document

- `NEXT_PRACTICE_GUIDE.md`: Phase 1〜8の入口。
- `SPRING_TEST_LAYERS.md`: 本物/Mockの層図。
- `TRANSACTION_ROLLBACK_LEARNING.md`
- `DEFECT_TRIAGE_PRACTICE.md`
- `MYBATIS_XML_LEARNING.md`
- `SPRING_BATCH_LEARNING.md`
- `REDUX_LEARNING.md`
- `OPENAPI_LEARNING.md`
- `CI_TROUBLESHOOTING.md`
- `DOCKER_NETWORK_LEARNING.md`
- `TEST_DATA_PRACTICE.md`

## 実行した確認

```text
docker compose build backend       -> SUCCESS（mvn package + 21 tests）
npm test                           -> 10 passed / 3 suites
npm run build                      -> SUCCESS / 104 modules
docker compose up -d --build       -> SUCCESS
GET  /applications                 -> 200
POST /applications                 -> 201
POST /applications/6/approve       -> 200、その後409
POST /applications (blank title)   -> 400
GET  /v3/api-docs                  -> 200
GET  frontend :3000                -> 200
MyBatis POST id=7                  -> PENDING
MyBatis approve id=7               -> APPROVED
PostgreSQL SQL確認                  -> status APPROVED / history 1
```

Mac HostにはMaven CLIがないため、Backendの最終検証はRepositoryのDockerfileに固定されたMaven 3.9.9 + Temurin 17で実行した。DevContainer内では資料記載どおり`mvn test`を直接実行できる。

## 各Labの目的

| Lab | 説明できるようになること |
|---|---|
| Test layers | slice/unit/integrationの境界とtrade-off |
| Transaction | commit/rollback/dirty checkingと整合性 |
| Defect triage | Network→Response→DB→Log→Codeでの切り分け |
| MyBatis XML | Java methodからXML SQL/parameter/result mappingを追う |
| Spring Batch | chunk transaction、途中失敗、restart、idempotency |
| Redux Toolkit | store/slice/action/dispatch/selectorとlocal/form stateとの差 |
| OpenAPI | Code/API契約/実通信の照合 |
| CI/Network | Runner差とContainerごとのlocalhost |
| Test data | 状態・Role・関連Data・既存履歴による期待値変化 |

## 既知の制約

- Batch Readerは教材を小さく保つためPENDING一覧をmemoryへ載せる。大量データ向けpaging/cursor、skip/retry、並列化は未実装。
- User/Role、添付等の関連domainは元アプリにないため、`TEST_DATA_PRACTICE.md`では本番規模の観点として明示した。
- `/labs/mybatis/**`はComposeの`local-labs` profile限定かつOpenAPI非表示。Cloud profileではControllerは作られない。
- Automated MyBatis testはH2、加えてCompose上のPostgreSQL 16で手動相当の実通信を検証済み。
- Jest成功時にReact Router v7 future flag warningが出るが、test failureではない。
- Batch failure testでは意図した例外のstack traceがlogへ出る。最終test statusは成功。
- `npm install`は4件（moderate 3/high 1）のaudit警告を報告した。破壊的な`npm audit fix --force`は適用していない。
- 変更は未commit。既存branch/historyと`test-practice`/`completed`は触っていない。

## 最初に行うPhase

`NEXT_PRACTICE_GUIDE.md`のPhase 1から始める。Controller TestでService Mockを追い、その直後にRepository Testを見ると「HTTP境界」と「DB mapping」の責務差が最も理解しやすい。

## 手動で確認する部分

1. `http://localhost:8080/swagger-ui.html`を開き、3 APIをTry it outする。
2. Browser DevToolsのNetworkでrequest/responseと`DEFECT_TRIAGE_PRACTICE.md`を照合する。
3. Debugger breakpointをService→Mapper Interface→XMLの順に置いてMyBatisを追う。
4. Batch testを1 methodずつ実行し、FAILED/COMPLETEDとchunk境界のlogを見る。
5. 練習後、停止だけなら`docker compose down`。DB volumeも消す場合だけ`docker compose down -v`。

現在は復習をすぐ始められるよう、Local Composeの3 containerを起動したままにしている。
