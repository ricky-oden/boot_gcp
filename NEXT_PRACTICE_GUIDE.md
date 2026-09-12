# 2/2面談向け 次のハンズオン手順

前提: DevContainerをRebuildし、Repository rootを開く。各Phaseは1〜2時間以内で独立して再実行できる。

## Phase 1 — Controller / Repository Test（45分）

- 触るFile: `ApplicationControllerTest`, `ApplicationRepositoryTest`, `SPRING_TEST_LAYERS.md`
- 実行Command: `cd backend && mvn -Dtest=ApplicationControllerTest,ApplicationRepositoryTest test`
- 自分で考える問題: Mockにした層はどこか。どちらがSQL mappingを保証するか。
- 確認ポイント: 200/201/400/404/409、save/find/status query、ManyToOne。
- 解答/解説: `SPRING_TEST_LAYERS.md`。続けて`ApplicationIntegrationTest`で全層との差を見る。

## Phase 2 — Transaction / Rollback（30分）

- 触るFile: `ApprovalTransactionLabService`, `TransactionRollbackIntegrationTest`。
- 実行Command: `cd backend && mvn -Dtest=TransactionRollbackIntegrationTest test`
- 自分で考える問題: 明示saveがないのになぜUPDATEされるか。履歴失敗時のstatusは。
- 確認ポイント: 正常時APPROVED+履歴1、失敗時PENDING+履歴0。
- 解答/解説: `TRANSACTION_ROLLBACK_LEARNING.md`。

## Phase 3 — Defect Triage（35分）

- 触るFile: `DEFECT_TRIAGE_PRACTICE.md`, `GlobalExceptionHandler`, Browser DevTools。
- 実行Command: `docker compose up -d --build`、`docker compose logs backend`。
- 自分で考える問題: 404/409/400/500/古い画面を、どの証拠から切り分けるか。
- 確認ポイント: 前提Data→Network→Response→DB→Log→Codeの順で口頭再現。
- 解答/解説: 各ケースの「問題」を答えてから同じ節の「解説」を開く。

## Phase 4 — MyBatis XML（40分）

- 触るFile: `ApplicationMapper.java`, `ApplicationMapper.xml`, `MyBatisApplicationService`。
- 実行Command: `cd backend && mvn -Dtest=MyBatisApplicationMapperIntegrationTest test`。Compose起動後は`MYBATIS_XML_LEARNING.md`のcurlも実行。
- 自分で考える問題: namespace/id/`@Param`/ResultMapはどう結び付くか。
- 確認ポイント: INSERT採番、SELECT mapping、WHERE付きUPDATE、PostgreSQL実通信。
- 解答/解説: `MYBATIS_XML_LEARNING.md`。

## Phase 5 — Spring Batch（45分）

- 触るFile: `BatchLabConfiguration`, `BatchLabIntegrationTest`。
- 実行Command: `cd backend && mvn -Dtest=BatchLabIntegrationTest test`
- 自分で考える問題: Job/Step/Reader/Processor/Writer/Chunkの責務は。restartと新規実行の違いは。
- 確認ポイント: success、FAILED、same-parameter restart、完了済み二重起動、idempotency。
- 解答/解説: `SPRING_BATCH_LEARNING.md`。

## Phase 6 — Redux Toolkit（30分）

- 触るFile: `frontend/src/store/*`, `App.tsx`。
- 実行Command: `cd frontend && npm test -- applicationFilterSlice.test.ts App.test.tsx`
- 自分で考える問題: store/slice/action/reducer/dispatch/selectorを画面動作へ対応付ける。
- 確認ポイント: 初期ALL、APPROVED filter、既存登録・承認test。
- 解答/解説: `REDUX_LEARNING.md`。

## Phase 7 — OpenAPI（20分）

- 触るFile: `ApplicationController`, `OpenApiConfig`, `OpenApiIntegrationTest`。
- 実行Command: `cd backend && mvn -Dtest=OpenApiIntegrationTest test`
- 手動確認: Compose起動後に`http://localhost:8080/swagger-ui.html`とBrowser Networkを開く。
- 自分で考える問題: OpenAPIとSwagger UIの違いは。Codeと実Responseの不一致をどう探すか。
- 確認ポイント: 3 endpoint、request/response schema、200/201/400/404/409。
- 解答/解説: `OPENAPI_LEARNING.md`。

## Phase 8 — CI / Docker Network（45分）

- 触るFile: `.github/workflows/ci.yml`, `docker-compose.yml`, `.devcontainer/*`。
- 実行Command: `docker compose ps`、`curl -i http://localhost:8080/applications`、`docker compose exec backend getent hosts db`。
- 自分で考える問題: CIだけ失敗する差は何か。各Containerのlocalhostは誰か。
- 確認ポイント: Java17/Node20、lock file、case、service名`db`、port 3000/8080/5432、Docker socket。
- 解答/解説: `CI_TROUBLESHOOTING.md`, `DOCKER_NETWORK_LEARNING.md`, `TEST_DATA_PRACTICE.md`。

## 最後の全回帰（20分）

`cd backend && mvn test package`、`cd ../frontend && npm test && npm run build`、rootで`docker compose up -d --build`。GET・登録・承認・Swaggerを確認する。終了時はDBデータを残すなら`docker compose down`、消すなら明示的に`docker compose down -v`。
