# 法人向け申請・ワークフロー管理システム（学習用ミニ版）

完成品を目指すのではなく、React → Spring Boot → PostgreSQL、テスト、Docker、GCPのつながりを短時間で確認するための小さな教材です。

**実案件との区別:** 1件目でJava 17、Spring Boot 3、React 18、TypeScript 5、PostgreSQL、Spring Data JPA、React Router、Axios、React Hook Form等を使用したことは固定情報です。Reduxと`useReducer`は使用していません。H2 Profile、具体的なEntity項目・状態名、port、Docker Compose詳細は教材用の簡略化です。最初の実習は `TEST_PRACTICE.md`、JPAの復習は `JPA_LEARNING.md`、案件整理は `PROJECT_CONTEXT.md`を参照してください。

実装する業務は次の3つだけです。

- 申請を登録する（初期状態 `PENDING`）
- 申請一覧を見る
- `PENDING` の申請を承認し、申請を `APPROVED` にして履歴を1件残す

認証、JWT、Spring Security、Role、Kafka、Redis、Kubernetesなどは意図的に入れていません。

## 1. 全体構成

```text
React + TypeScript (localhost:3000)
        ↓ HTTP / JSON
Spring Boot Controller (localhost:8080)
        ↓ DI
Service（業務ルール・Transaction）
        ↓ DI
Spring Data JPA Repository
        ↓ SQL
PostgreSQL (localhost:5432)
```

ローカルはPostgreSQL、課金を抑えたCloud Run学習環境はインメモリH2、任意の実践環境はCloud SQL for PostgreSQLを使います。

| 実行場所 | Profile | DB | データの寿命 |
|---|---|---|---|
| Docker Compose | default | PostgreSQL | Docker volumeを消すまで保持 |
| Cloud Runの低コスト学習 | `cloud-run-learning` | H2 | インスタンス停止・再起動で消える |
| Cloud Run + Cloud SQL | `cloud-sql` | PostgreSQL | Cloud SQLを削除するまで保持 |

H2版は構造確認用です。Cloud Runは複数インスタンスになり得るため、永続化が必要な実運用でH2を使ってはいけません。

## 2. ファイル構成

```text
.
├── docker-compose.yml
├── README.md
├── backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java/com/example/workflow
│       │   │   ├── WorkflowApplication.java
│       │   │   ├── config/WebConfig.java
│       │   │   ├── controller/ApplicationController.java
│       │   │   ├── controller/GlobalExceptionHandler.java
│       │   │   ├── dto/{CreateApplicationRequest,ApplicationResponse,ErrorResponse}.java
│       │   │   ├── entity/{Application,ApplicationHistory,ApplicationStatus}.java
│       │   │   ├── exception/{ApplicationNotFoundException,AlreadyApprovedException}.java
│       │   │   ├── repository/{ApplicationRepository,ApplicationHistoryRepository}.java
│       │   │   └── service/ApplicationService.java
│       │   └── resources
│       │       ├── application.yml
│       │       ├── application-cloud-run-learning.yml
│       │       └── application-cloud-sql.yml
│       └── test/java/com/example/workflow/service/ApplicationServiceTest.java
└── frontend
    ├── Dockerfile
    ├── package.json
    ├── vite.config.ts
    └── src/{main.tsx,App.tsx,style.css}
```

## 3. 最短のローカル起動

必要なものはDocker Desktop（またはDocker Engine + Compose）だけです。JavaやMaven、Node.jsをホストへ入れなくても起動できます。

```bash
docker compose up --build
```

起動後にブラウザで `http://localhost:3000` を開き、タイトルを入力して登録し、一覧の「承認」を押します。APIは `http://localhost:8080`、PostgreSQLは `localhost:5432` です。

停止（データは残る）:

```bash
docker compose down
```

停止し、PostgreSQLデータも消す:

```bash
docker compose down -v
```

`-v` は学習データを削除します。必要なデータがないことを確認してから実行してください。

## 4. APIとReactからの通信確認

Reactでは `frontend/src/App.tsx` のAxiosが次のAPIを呼び、React Hook Formで入力を扱い、成功後に一覧を再取得します。RouterはReact Routerです。

| 操作 | Method / path | 成功時 |
|---|---|---|
| 一覧 | `GET /applications` | `200` |
| 登録 | `POST /applications` | `201` |
| 承認 | `POST /applications/{id}/approve` | `200` |

画面を使わずAPIだけ確認する場合:

```bash
curl -i http://localhost:8080/applications

curl -i -X POST http://localhost:8080/applications \
  -H 'Content-Type: application/json' \
  -d '{"title":"PC購入申請"}'

curl -i -X POST http://localhost:8080/applications/1/approve
```

`title` を空にすると `400`、存在しないIDの承認は `404`、承認済みIDの再承認は `409` と、JSONのエラーが返ります。

## 5. Spring Bootのレイヤー

### Controller / Service / Repository

- **Controller**: HTTPリクエストを受け、DTOをValidationし、Serviceを呼び、JSONレスポンスを返します。
- **Service**: 「PENDINGだけ承認できる」のような業務ルールとTransactionを担当します。
- **Repository**: Entityの検索・保存を担当します。ServiceはSQLやHTTPを知りません。

流れを分けることで、HTTP、業務判断、DBアクセスを別々に読み、別々にテストできます。

### DTO / Entity

- **DTO** (`CreateApplicationRequest`, `ApplicationResponse`): APIの入力・出力専用です。`@NotBlank` によるtitle必須チェックもRequest DTOにあります。
- **Entity** (`Application`, `ApplicationHistory`): DBテーブルへ保存するデータです。JPAの `@Entity`、`@Id`、関連を表す `@ManyToOne` などが付きます。

APIの都合とDBの都合を同じクラスへ詰め込まないために分けています。

### DI（Dependency Injection、依存性注入）

DIとは、クラスが必要とする別のオブジェクトを外部から渡す仕組みです。このコードではSpringが次のConstructor（コンストラクタ）へ依存先を渡します。

```text
ApplicationController → ApplicationService
ApplicationService → ApplicationRepository / ApplicationHistoryRepository
```

Controller内で `new ApplicationService(...)` をすると、ControllerがRepositoryの組み立て方まで知る必要があり、依存先をテスト用Mockへ交換しにくくなります。Constructor Injectionなら、必要な依存が引数から明確に分かり、Spring実行時は本物、単体テスト時はMockを渡せます。

### JPAとPostgreSQL

JPAはJavaオブジェクト（Entity）とRDBのテーブルを対応付ける標準仕様です。Spring Data JPAがJPAを使いやすくします。`JpaRepository<Application, Long>` を継承したinterfaceだけで `findAll`、`findById`、`save` などが使えるのは、Spring Dataが実行時に実装を生成するためです。

JPAはDBそのものではありません。この教材ではJPAの下でPostgreSQL JDBC DriverがPostgreSQLと通信します。

### Transaction

承認処理の `@Transactional` は次を1つの処理単位にします。

1. `Application.status` を `APPROVED` に更新
2. `ApplicationHistory` を登録

Applicationだけ更新された後にHistory登録が失敗すると、「承認済みなのに履歴がない」という不整合になります。Transaction内で例外が発生すれば全体をrollbackし、両方成功または両方失敗にします。

## 6. JUnit 5 / Mockitoの単体テスト

Dockerでテストするため、ホスト側にJava/Mavenは不要です。

```bash
docker build --target build -t workflow-backend-test ./backend
```

`backend/Dockerfile` のbuild stage内で `mvn package` が実行され、Testも実行されます。Java 17とMavenがローカルにある場合は次でも構いません。

```bash
cd backend
mvn test
```

このService Testでは本物は `ApplicationService`、偽物は2つのRepositoryです。DBを起動せず、業務ルールだけを速く確かめられます。

- **JUnit 5**: テストを書き、実行するためのフレームワーク。`@Test`、`assertEquals`、`assertThrows` を提供します。
- **Mockito**: 依存先のMockを作り、振る舞いや呼び出しを検証するライブラリです。
- **`@Mock`**: Mockitoが偽物のRepositoryを作ります。
- **`@InjectMocks`**: 本物のServiceを作り、`@Mock` をConstructorへ注入します。
- **`when(...).thenReturn(...)`**: Mockが呼ばれたときの返り値を準備します。
- **`verify(...)`**: 保存などの呼び出しが実際に行われたかを確認します。
- **`assertEquals`**: 期待値と実際の値が等しいことを確認します。
- **`assertThrows`**: 指定した例外が発生することを確認します。

各テストは **Arrange**（準備）→ **Act**（実行）→ **Assert**（検証）の順です。

### ReactのJest / React Testing Library

Frontend Testは次で実行します。Dockerを使うためホスト側Node.jsは不要です。

```bash
docker build --target build -t workflow-frontend-test ./frontend
docker run --rm workflow-frontend-test npm test
```

- **Jest**: JavaScript / TypeScriptのTest Runnerです。`test`を収集・実行し、`expect`による結果検証と`jest.fn()`によるMockを提供します。
- **React Testing Library**: React Componentをユーザー目線でTestします。`render`で画面を作り、`screen`で表示要素を探し、`userEvent`で入力・clickを行います。

役割を短く言うと、Jestは「Testの実行・expect・Mock」、React Testing Libraryは「Componentのrender・表示・操作」です。この教材ではAxiosをMockし、次の6点を確認します。

1. 初期表示で一覧APIが呼ばれ、取得した申請が表示される
2. title入力・登録で`POST /applications`が呼ばれる
3. PENDINGには承認buttonが表示される
4. APPROVEDには承認buttonが表示されない
5. API失敗時にerror messageが表示される
6. loading中にbuttonが無効になる

### Backend TestとFrontend Testの違い

| 観点 | Backend | Frontend |
|---|---|---|
| Test対象 | `ApplicationService` | React `App` Component |
| Tool | JUnit 5 + Mockito | Jest + React Testing Library |
| Mock | Repository | Axios |
| 主な検証 | 業務rule、状態変更、例外、保存呼び出し | 一覧・入力・API呼び出し・loading/error・操作可否 |
| Result | `assertEquals` / `assertThrows` | `expect` / `screen` |
| Interaction | `verify` | `toHaveBeenCalledWith` |

面談では「React側の単体Testで、一覧表示、入力から登録APIまで、loadingによるbutton無効化、API error表示、申請状態に応じた承認操作の出し分けを確認した」と、`frontend/src/App.test.tsx`へ対応付けて説明できます。loadingは実装上`disabled={loading}`で制御し、代表的な非同期処理のTestが登録caseです。

## 7. PostgreSQLのデータ確認

Compose起動中に次を実行します。

```bash
docker compose exec db psql -U workflow -d workflow
```

psql内:

```sql
SELECT id, title, status, created_at FROM applications ORDER BY id;
SELECT id, application_id, action, created_at FROM application_histories ORDER BY id;
\q
```

承認前は1つ目のテーブルだけ、承認後はstatusの変更と履歴追加の両方を確認できます。

## 8. Docker用語

- **Docker**: Imageを作り、Containerを動かすための仕組み・ツール群です。
- **Image**: アプリ、ランタイム、設定をまとめた実行前の読み取り専用テンプレートです。
- **Container**: Imageから起動した実行中のプロセスです。同じImageから複数起動できます。
- **Dockerfile**: Imageの作り方を書いたレシピです。
- **Docker Compose**: 複数Containerと接続関係を1つのYAMLで定義・起動する道具です。この教材ではfrontend、backend、dbをまとめます。

## 9. Logging

登録、承認開始、承認成功、Validation・対象なし・再承認エラーで `log.info` / `log.error` を出します。

- ローカル: `docker compose logs -f backend` またはTerminalで見る
- Cloud Run: 標準出力・標準エラーがCloud Loggingへ集約され、Logs ExplorerやCLIで見る

## 10. Cloud RunへDeploy（まずH2で低コスト学習）

この手順は有料サービスを操作します。Cloud Runには無料枠がありますが、完全無料は保証されません。Artifact Registryの保存量なども料金対象になり得ます。事前にGoogle Cloudの予算アラートも設定してください。

以下は `asia-northeast1`、サービス名 `workflow-api` の例です。`YOUR_PROJECT_ID` を自分のProject IDへ置き換えます。

### 10.1 Projectと認証

```bash
gcloud auth login
gcloud auth application-default login
gcloud config set project YOUR_PROJECT_ID
gcloud config set run/region asia-northeast1
```

### 10.2 必要APIを有効化

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  logging.googleapis.com
```

### 10.3 Artifact Registry作成、認証、Image build/push

```bash
gcloud artifacts repositories create workflow-repo \
  --repository-format=docker \
  --location=asia-northeast1 \
  --description='Workflow learning images'

gcloud auth configure-docker asia-northeast1-docker.pkg.dev

docker build \
  --platform linux/amd64 \
  -t asia-northeast1-docker.pkg.dev/YOUR_PROJECT_ID/workflow-repo/workflow-api:latest \
  ./backend

docker push \
  asia-northeast1-docker.pkg.dev/YOUR_PROJECT_ID/workflow-repo/workflow-api:latest
```

Apple SiliconでもCloud Runで動かしやすいよう `--platform linux/amd64` を明示しています。

### 10.4 H2 ProfileでDeploy

```bash
gcloud run deploy workflow-api \
  --image=asia-northeast1-docker.pkg.dev/YOUR_PROJECT_ID/workflow-repo/workflow-api:latest \
  --region=asia-northeast1 \
  --no-allow-unauthenticated \
  --set-env-vars=SPRING_PROFILES_ACTIVE=cloud-run-learning \
  --cpu=1 \
  --memory=512Mi \
  --min-instances=0 \
  --max-instances=1 \
  --concurrency=20 \
  --cpu-throttling
```

`--min=0` で未使用時に0 instanceまで縮退し、`--cpu-throttling` でrequest-based billingを使います。VPC Connectorは作りません。H2データはinstance再起動で消えるため、数回APIを叩く学習専用です。

URL取得とAPI確認:

```bash
gcloud run services describe workflow-api \
  --region=asia-northeast1 \
  --format='value(status.url)'
```

表示されたURLを使います。非公開ServiceなのでID Tokenを付けます。

```bash
ID_TOKEN=$(gcloud auth print-identity-token)

curl -i -H "Authorization: Bearer ${ID_TOKEN}" https://YOUR_CLOUD_RUN_URL/applications

curl -i -X POST https://YOUR_CLOUD_RUN_URL/applications \
  -H "Authorization: Bearer ${ID_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Cloud Run確認申請"}'
```

公式資料: [Cloud Runの料金](https://cloud.google.com/run/pricing)、[minimum instances](https://cloud.google.com/run/docs/configuring/min-instances)、[Artifact RegistryへImageをpush](https://cloud.google.com/artifact-registry/docs/docker/pushing-and-pulling)

## 11. Cloud Logging確認

1. Cloud Consoleで **Logging → Logs Explorer** を開く
2. 対象Projectを選ぶ
3. 次のQueryを入れる

```text
resource.type="cloud_run_revision"
resource.labels.service_name="workflow-api"
```

`Application created`、`Approval started`、`Approval succeeded` などを確認します。CLIなら:

```bash
gcloud logging read \
  'resource.type="cloud_run_revision" AND resource.labels.service_name="workflow-api"' \
  --limit=50 \
  --format='value(timestamp,textPayload)'
```

公式資料: [Logs Explorerでログを見る](https://cloud.google.com/logging/docs/view/overview)

## 12. Optional: Cloud RunからCloud SQL for PostgreSQLへ接続

**ここからはCloud SQLの継続課金が発生する手順です。既存の学習Resourceを確認する場合は新規作成せず、`TEST_PRACTICE.md`の既存Resource確認手順を使ってください。Cloud Runを0 instanceにしてもCloud SQLの料金は止まりません。**

Cloud SQLはGoogleがPostgreSQLのOS、バックアップ、パッチなどを管理するサービスです。構成は `Cloud Run → Cloud SQL Java Connector → Cloud SQL for PostgreSQL` になります。このプロジェクトは `postgres-socket-factory` と `cloud-sql` Profileを用意済みです。

料金と利用可能なMachine Typeは作成直前に必ず[Cloud SQL pricing](https://cloud.google.com/sql/pricing)で確認してください。

### 12.1 API、Cloud SQL、DB/User作成

例では学習用の小さなshared-core machineを使います。組織ポリシーやリージョンにより利用できない場合があります。

```bash
gcloud services enable sqladmin.googleapis.com

gcloud sql instances create workflow-db \
  --database-version=POSTGRES_16 \
  --edition=ENTERPRISE \
  --tier=db-f1-micro \
  --region=asia-northeast1 \
  --storage-type=SSD \
  --storage-size=10GB \
  --no-storage-auto-increase \
  --availability-type=zonal

gcloud sql databases create workflow --instance=workflow-db

gcloud sql users create workflow \
  --instance=workflow-db \
  --password='CHANGE_THIS_LEARNING_PASSWORD'
```

### 12.2 Cloud Run用Service Account

```bash
gcloud iam service-accounts create workflow-runner \
  --display-name='Workflow Cloud Run learning account'

gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member='serviceAccount:workflow-runner@YOUR_PROJECT_ID.iam.gserviceaccount.com' \
  --role='roles/cloudsql.client'

gcloud sql instances describe workflow-db \
  --format='value(connectionName)'
```

最後に表示される `PROJECT:REGION:INSTANCE` を次の `YOUR_INSTANCE_CONNECTION_NAME` に入れます。

### 12.3 Cloud SQL Profileで再Deploy

```bash
gcloud run deploy workflow-api \
  --image=asia-northeast1-docker.pkg.dev/YOUR_PROJECT_ID/workflow-repo/workflow-api:latest \
  --region=asia-northeast1 \
  --no-allow-unauthenticated \
  --service-account=workflow-runner@YOUR_PROJECT_ID.iam.gserviceaccount.com \
  --set-env-vars=SPRING_PROFILES_ACTIVE=cloud-sql,DB_NAME=workflow,DB_USER=workflow,DB_PASSWORD=CHANGE_THIS_LEARNING_PASSWORD,INSTANCE_CONNECTION_NAME=YOUR_INSTANCE_CONNECTION_NAME \
  --cpu=1 \
  --memory=512Mi \
  --min-instances=0 \
  --max-instances=1 \
  --concurrency=20 \
  --cpu-throttling
```

学習用に簡潔さを優先してpasswordを環境変数で渡しています。実務ではSecret Managerを使用します。JavaではUnix socketを直接扱わず、Cloud SQL Java Connectorを使います。公式資料: [Cloud RunからCloud SQL for PostgreSQLへ接続](https://cloud.google.com/sql/docs/postgres/connect-run)、[Java Connectorの接続例](https://cloud.google.com/sql/docs/postgres/connect-instance-cloud-run)

## 13. 課金Resourceの削除

面談・復習が終わったら、まず料金の大きいCloud SQLを削除します。削除するとDBデータは失われます。現在日が2026-09-11以降なら、残す明確な理由がない限りCleanup対象です。

```bash
gcloud sql instances delete workflow-db
gcloud run services delete workflow-api --region=asia-northeast1
gcloud artifacts repositories delete workflow-repo --location=asia-northeast1
gcloud iam service-accounts delete \
  workflow-runner@YOUR_PROJECT_ID.iam.gserviceaccount.com
```

確認promptが出ます。対象ProjectとResource名を読み、問題なければ承認してください。Consoleの「Cloud SQL」「Cloud Run」「Artifact Registry」で残っていないことも確認します。Project自体がこの学習専用なら、Project削除が最も確実ですが、そのProject内の全Resourceが失われます。

## 14. LocalとCloudの違い

| 観点 | Local | Cloud Run + H2 | Cloud Run + Cloud SQL |
|---|---|---|---|
| Backend | Docker Container | Cloud Run Container | Cloud Run Container |
| DB | PostgreSQL Container | instance内H2 | 管理対象PostgreSQL |
| 永続性 | volumeに保持 | なし | あり |
| ログ | Terminal / `docker compose logs` | Cloud Logging | Cloud Logging |
| Scale | 自分で1つ起動 | 自動、0まで縮退 | Cloud Runのみ自動 |
| 課金 | 手元のPC | 使用量・保存量等 | Cloud Run + 常時稼働DB等 |

## 15. 面談まで時間がない人向け 学習順序 / TOP 10

1. `backend/.../controller/ApplicationController.java` — HTTP入口とDTO
2. `backend/.../service/ApplicationService.java` — DI、業務ルール、Transaction、Logging
3. `backend/.../repository/ApplicationRepository.java` — Spring Data JPA
4. `backend/.../entity/Application.java` — Entityと状態変更
5. `backend/.../entity/ApplicationHistory.java` — 承認履歴と関連
6. `backend/.../dto/CreateApplicationRequest.java` — Validation
7. `backend/.../service/ApplicationServiceTest.java` — JUnit 5 / Mockito / AAA
8. `frontend/src/App.tsx` — ReactからAPIを呼び、再取得して画面反映
9. `docker-compose.yml` と `backend/Dockerfile` — Container同士の接続とImage build
10. `application-cloud-run-learning.yml` / `application-cloud-sql.yml` とREADMEのDeploy手順 — LocalとGCPの切り替え

面談では、まず「Controller → Service → Repository → PostgreSQL」を説明し、次に「Serviceの2保存を `@Transactional` で一体化」、最後に「RepositoryをMockに交換してServiceの業務ルールを単体テスト」と話すと、主要概念が1本の流れになります。
