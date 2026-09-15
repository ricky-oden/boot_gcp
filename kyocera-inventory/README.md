# 京セラ案件向け 在庫管理Catch-up環境

このDirectoryは、京セラ案件への参画準備を目的とした独立学習領域です。既存の申請ワークフロー環境はRepository root側にそのまま残しています。

## 情報の区分

- 実案件確定情報: Java 17、Spring Boot 2.7系、Gradle、PostgreSQL、DevContainer／Dockerを利用する。
- 1/2案件情報: ReduxやSpring Batch等を利用する可能性がある。Day1では実装しない。
- 学習用仮定: Directory名、Port、Database名、`GET /api/health`は学習を成立させるための仮設定であり、実案件仕様ではない。

## Day1の構成

```text
kyocera-inventory/
├── backend-api/       Java 17 + Spring Boot + Gradle
├── batch/             Day5用placeholder
├── frontend-pc/       Day3用placeholder
├── frontend-mobile/   Day4用placeholder
├── openapi/           Day2用placeholder
├── docs/              学習記録
└── docker-compose.yml 京セラ専用Backend/PostgreSQL
```

## Version

| Component | 学習環境 | 備考 |
|---|---:|---|
| Java | 17 | 実案件確定情報と一致 |
| Spring Boot | 2.7.18 | 実案件資料の2.7.4ではなく、2.7系最終保守版を採用 |
| Gradle | 7.6.4 | Wrapperで固定 |
| PostgreSQL | 16-alpine | 既存環境と同系列だがDatabase/Volume/Portは分離 |

Spring Boot 2.7.18はJava 17およびGradle 7.xをサポートします。Spring Boot 2.7.4との差分はpatch-levelの学習環境上の差として扱い、実案件Versionを2.7.18と断定しません。

## Portと分離方針

| 用途 | 既存申請環境 | 京セラ環境 |
|---|---:|---:|
| Backend | 8080 | 8081 |
| PostgreSQL Host Port | 5432 | 5433 |
| Database | workflow | kyocera_inventory |
| Compose Project | root既存構成 | kyocera-inventory |
| Docker Volume | postgres-data | kyocera-postgres-data |

京セラBackendはJPAを含みません。Day1はSpring JDBCの`SELECT 1`だけでPostgreSQL接続を確認し、Day2でMyBatis XMLを追加できる土台にしています。

## 最短の起動方法（Docker Compose）

Repository rootから実行します。

```bash
docker compose -f kyocera-inventory/docker-compose.yml up -d --build
docker compose -f kyocera-inventory/docker-compose.yml ps
curl http://localhost:8081/api/health
docker compose -f kyocera-inventory/docker-compose.yml exec kyocera-db \
  psql -U kyocera -d kyocera_inventory -c "SELECT current_database(), current_user;"
```

期待するHealth Response:

```json
{"status":"UP","database":"UP"}
```

停止時はDatabase volumeを残すなら次を実行します。

```bash
docker compose -f kyocera-inventory/docker-compose.yml down
```

`down -v`は京セラ用Database dataも削除するため、必要な場合だけ明示的に使用してください。

## DevContainer内で手を動かす手順

1. VS CodeでRepository rootを開く。
2. `Dev Containers: Reopen in Container`を実行する。
3. DevContainer Terminalで次を実行する。

```bash
java -version
cd kyocera-inventory
./gradlew --version
./gradlew :backend-api:test
docker compose -f docker-compose.yml up -d kyocera-db
DB_URL=jdbc:postgresql://host.docker.internal:5433/kyocera_inventory \
  DB_USER=kyocera \
  DB_PASSWORD=kyocera_local \
  ./gradlew :backend-api:bootRun
```

別Terminalから確認します。

```bash
curl http://localhost:8081/api/health
docker compose -f kyocera-inventory/docker-compose.yml exec kyocera-db \
  psql -U kyocera -d kyocera_inventory -c "SELECT 1;"
```

DevContainer内の`localhost`はDevContainer自身です。Docker Desktop側で公開したPostgreSQLへは`host.docker.internal:5433`で接続します。BackendもCompose Containerとして起動する場合は`kyocera-db:5432`を使用します。

## Day1のTest

- `KyoceraInventoryApplicationTest`: Spring ContextがJava 17／Spring Boot 2.7で起動することを確認。
- `HealthControllerTest`: PostgreSQL応答をMockし、HTTP 200とJSONを確認。
- Docker Compose smoke test: 本物のPostgreSQLへ`SELECT 1`を実行し、Health APIで`database=UP`を確認。

## Day1では未実装

MyBatis、OpenAPI Schema First、在庫Table、Redux、React、Smartphone、Spring Batch、Checkstyle、GCS、GKE、Jira/結合Test Scenarioは後続Dayで扱います。
