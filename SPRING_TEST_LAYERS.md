# Springテスト層ハンズオン

## 先に答える

- ControllerテストはHTTP変換・Validation・status/JSONを確認し、ServiceはMockにする。
- RepositoryテストはJPA mappingとquery methodをH2で確認する。
- 統合テストはController → Service → Repository → DBを本物でつなぎ、業務フローを確認する。

## 依存関係

```text
MockMvc Controller test
HTTP -> ApplicationController -> [Mock ApplicationService]

@DataJpaTest
ApplicationRepository -> Hibernate/JPA -> H2

@SpringBootTest + MockMvc
HTTP -> Controller -> Service -> Repository -> Hibernate -> H2
```

## 実行

```bash
cd backend
./mvnw test                         # mvnwがない場合は mvn test
mvn -Dtest=ApplicationControllerTest test
mvn -Dtest=ApplicationRepositoryTest test
mvn -Dtest=ApplicationIntegrationTest test
```

## 何を見ればよいか

| 層 | ファイル | 主な確認 |
|---|---|---|
| Controller | `ApplicationControllerTest` | GET/POST/approve、200/201/400/404/409/500、JSON |
| Repository | `ApplicationRepositoryTest` | save/find、status検索、ManyToOne |
| Integration | `ApplicationIntegrationTest` | 登録→承認→一覧→履歴、二重承認409 |

Mockは境界を速く確認する道具で、DB mappingの保証にはならない。統合テストは安心度が高い一方、起動と原因切り分けのコストが増えるため、両方を使い分ける。
