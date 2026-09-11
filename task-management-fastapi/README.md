# 法人向け業務依頼・進捗管理Webシステム 学習用ミニ版

スキルシートの2件目を理解するための小さな教材です。React → FastAPI → PostgreSQLの接続、pytestとJestの違いを短時間で追えることを優先しています。

## 実案件の確定情報と教材上の選択

スキルシート上で確定しているのは、TypeScript、React、Python、FastAPI、PostgreSQL、pytest、Jest、Docker、GitHubを使い、既存Webシステムの追加開発・保守として検索条件、担当者・期限・状態更新などを担当したことです。

次は**教材を成立させるための実装選択で、実案件設定としては未確定**です。

- SQLAlchemyの採用
- Repository Patternの採用
- FastAPI `Depends` の採用
- Taskの具体的な項目名、`TODO / IN_PROGRESS / DONE`という状態名と遷移順
- Docker ComposeのService名、port、dependency version

React Testing Libraryはこの2件目の実務使用技術として扱っていません。Frontend TestはJestでAPI関数と状態判断をテストしています。

## 構成

```text
React + TypeScript :3001
        ↓ HTTP / JSON
FastAPI Router :8000
        ↓ TaskService
TaskRepository（教材上の責務分離）
        ↓ SQLAlchemy（教材上の実装選択）
PostgreSQL :5433
```

```text
.
├── docker-compose.yml
├── backend
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── app
│   │   ├── main.py / database.py
│   │   ├── routers/tasks.py
│   │   ├── services/task_service.py
│   │   ├── repositories/task_repository.py
│   │   ├── schemas/task.py
│   │   └── models/task.py
│   └── tests/test_task_service.py
└── frontend
    ├── Dockerfile / package.json
    └── src/App.tsx / api.ts / api.test.ts
```

## 起動

ホスト側にPythonやNode.jsは不要です。

```bash
cd task-management-fastapi
docker compose up --build
```

- React: `http://localhost:3001`
- FastAPI Swagger UI: `http://localhost:8000/docs`
- PostgreSQL: `localhost:5433`

停止は `docker compose down`、学習データも削除する場合だけ `docker compose down -v` を使います。

## API

```bash
curl http://localhost:8000/tasks

curl -X POST http://localhost:8000/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"見積確認","assignee":"佐藤","dueDate":"2026-09-15"}'

curl 'http://localhost:8000/tasks?status=TODO&assignee=%E4%BD%90%E8%97%A4'

curl -X PUT http://localhost:8000/tasks/1 \
  -H 'Content-Type: application/json' \
  -d '{"assignee":"鈴木","dueDate":"2026-09-20"}'

curl -X PATCH http://localhost:8000/tasks/1/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"IN_PROGRESS"}'
```

状態は `TODO → IN_PROGRESS → DONE` の順だけ許可します。存在しないTaskは404、不正遷移は409、Pydantic入力エラーは422です。この遷移ルール自体は教材上の仮設定です。

## Test

Backendは本物の `TaskService` に偽物の `TaskRepository` を渡し、DBなしで業務ルールをテストします。

```bash
docker compose run --rm backend pytest -q
```

- 正常系: 担当者、期限、状態を更新
- 異常系: 対象Taskなし
- 異常系: `TODO → DONE` の不正遷移

FrontendはJestだけで `api.ts` の一覧結果、検索Query、更新API、APIエラー、状態に応じた操作可否を確認します。

```bash
docker build --target build -t task-frontend-test ./frontend
docker run --rm task-frontend-test npm test
```

React Testing Libraryを2件目の実務技術として追加しないため、Component renderではなく、Componentが使うAPI・状態判断をJestでテストしています。

## PostgreSQL確認

```bash
docker compose exec db psql -U workflow -d task_management
```

```sql
SELECT id, title, assignee, due_date, status, created_at FROM tasks ORDER BY id;
\q
```

## PydanticとSpring Boot DTO

`schemas/task.py` のPydantic ModelはFastAPIのRequest / Response型です。型、必須、文字数、日付、Enumを検証し、不正ならFastAPIが422を返します。役割は1件目のSpring Boot DTO + Bean Validationに近いですが、仕組みは同一ではありません。

## Python基本をコードで読む

- `def`: `routers/tasks.py` のAPI関数やService methodを定義します。TypeScriptの`function`、Javaのmethodに近い書き方です。
- `class`: `TaskService`、`TaskRepository`、Pydantic Model、SQLAlchemy Modelを定義します。
- `import`: 別moduleのclassやfunctionを読み込みます。Javaの`import`、TypeScriptの`import`と目的は似ています。
- `list[Task]`: Taskのlistというtype hintです。Javaの`List<Task>`、TypeScriptの`Task[]`に近い表現です。
- `dict`: Serviceの`allowed`は「現在状態 → 次状態」の対応表です。TypeScriptのobject/Mapに近いものです。
- `X | None`: 値がXまたはNoneです。従来の`Optional[X]`と同じ意味で、Javaの`Optional<X>`とは使い方が異なります。
- `None`: 値がないことを表します。Javaの`null`、TypeScriptの`null`/`undefined`に近い値です。
- type hint: `task_id: int`や`-> Task`です。Python実行時の強制型ではありませんが、IDEや検査、FastAPI/Pydanticが活用します。
- `exception`: `TaskNotFoundError`などを`raise`し、FastAPIのhandlerがHTTP Errorへ変換します。
- `__init__`: classの初期化methodです。Java/TypeScriptのconstructorに相当します。
- `with`: pytestの`with pytest.raises(...)`は、その範囲で例外が出ることを確認し、後処理も安全に行えるcontext manager構文です。
- `async def`: `main.py`の`lifespan`だけで登場し、起動・終了を扱います。DB処理は同期SQLAlchemyなのでAPI routeは`def`にし、無理に非同期処理を混在させていません。

## pytestとMock

- pytest: Python Testの収集・実行とassertionを担当します。`test_`から始まる関数が自動収集されます。
- `unittest.mock.Mock`: Repositoryの偽物です。`return_value`で検索結果を準備し、`assert_called_once_with`で呼び出しを確認します。
- Arrange / Act / Assert: 準備 → 実行 → 結果と呼び出しの検証、という言語共通の整理です。

JUnit/Mockitoとの対応は似た役割を理解する目安であり、完全な1対1対応ではありません。

## GCPについて

Google Cloudは2件目のスキルシート上の使用技術ではないため、この実案件再現には組み込んでいません。一般論として、FastAPIもContainer化すればCloud Runで動かせますが、この教材ではDeployしません。

## 面談で見るFile TOP10

1. `README.md`
2. `backend/app/routers/tasks.py`
3. `backend/app/services/task_service.py`
4. `backend/app/schemas/task.py`
5. `backend/app/repositories/task_repository.py`
6. `backend/app/models/task.py`
7. `backend/tests/test_task_service.py`
8. `frontend/src/App.tsx`
9. `frontend/src/api.ts` と `api.test.ts`
10. `docker-compose.yml`
