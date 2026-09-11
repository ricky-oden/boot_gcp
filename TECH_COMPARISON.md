# Java Spring Boot と Python FastAPI 教材比較

この比較は2案件を説明しやすくするためのものです。Spring Data JPAは1件目の固定情報です。SQLAlchemy、Depends、Repository Patternは2件目教材上の実装選択です。

| 比較項目 | 1件目 Java Spring Boot | 2件目 Python FastAPI |
|---|---|---|
| Language | Java 17 | Python 3.12 |
| Backend Framework | Spring Boot | FastAPI |
| API定義 | annotation中心 | decorator + type hint中心 |
| Controller / Router | `@RestController` | `APIRouter` |
| GET | `@GetMapping` | `@router.get` |
| DTO / Pydantic | Java record + Bean Validation | Pydantic `BaseModel` |
| Service | `@Service` class | 通常のPython class |
| DB Access | Spring Data JPA Repository | SQLAlchemy Repository |
| Entity / Model | JPA `@Entity` | SQLAlchemy mapped class |
| DI | Spring Container + Constructor Injection | RouterでService生成、`Depends`でDB Session注入 |
| Validation | `@NotBlank` | `Field`、date、Enum |
| Exception Handling | `@RestControllerAdvice` | `@app.exception_handler` |
| Transaction | `@Transactional` | SQLAlchemy Sessionのcommit/rollback単位 |
| DB | PostgreSQL | PostgreSQL |
| Backend Test | JUnit 5 | pytest |
| Mock | Mockito | `unittest.mock.Mock` |
| Frontend | TypeScript + React | TypeScript + React |
| Frontend Test | Jest + React Testing Library | Jest。RTLの実務使用は設定しない |
| Dependency管理 | `pom.xml` / Maven | `requirements.txt` / pip |
| Build Tool | Maven | Pythonはcompile build不要。Image build時にpip install |
| Docker | Java jar + PostgreSQL + React | Uvicorn + PostgreSQL + React |
| GCPとの関係 | Cloud Run、Cloud SQL接続、Cloud Loggingが確定 | 2件目の実務利用は記載なし。教材でもDeployしない |

## 読み方の要点

Spring BootはannotationとDI Containerが多くの組み立てを担い、Java compilerが型を厳密に確認します。FastAPIはPythonのtype hintとPydanticをAPI定義・Validation・document生成に活用し、短い記述でAPIを作れます。

両方ともこの教材では「入口 → 業務ロジック → DB Access」を分けていますが、これは教材の比較を容易にするためです。2件目の実案件でRepository PatternやDependsを採用していたという主張には使いません。

Spring Data JPAは1件目で使用した固定情報です。SQLAlchemyは2件目教材とのDB Access比較のための選択で、実務使用を主張しません。
