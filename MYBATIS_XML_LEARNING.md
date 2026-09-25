# MyBatis XML ハンズオン

## 経路

```text
MyBatisApplicationService
  -> ApplicationMapper.java (method/parameter contract)
  -> mappers/ApplicationMapper.xml (SQL/ResultMap)
  -> PostgreSQL または test profileのH2
```

既存REST APIはJPAのまま。MyBatisは比較学習用の並走経路で、同じ`applications` tableに対してSELECT/INSERT/UPDATEする。

## XMLで追うポイント

- `namespace`がMapper Interfaceの完全修飾名と一致する。
- `id`がJava method名と一致する。
- `#{id}`等が`@Param`名と一致する。文字列連結ではなくbind parameterになる。
- `resultMap`が`created_at`を`createdAt`へ対応付ける。
- UPDATEは`WHERE id = ... AND status = 'PENDING'`相当になり、更新0件を二重承認として扱う。

## JPAとの比較

| 観点 | JPA | MyBatis XML |
|---|---|---|
| SQL | ORMが生成 | 自分で明示 |
| mapping | Annotation/Entity | ResultMap |
| 更新 | dirty checking | UPDATE文 |
| 得意 | CRUD・関連 | 複雑SQL・既存SQL統制 |
| 注意 | N+1、生成SQL | XML/parameter typo、DB差異 |

```bash
cd backend
mvn -Dtest=MyBatisApplicationMapperIntegrationTest test

# PostgreSQLまで手で追う（Repository root / Compose起動後）
curl -X POST http://localhost:8080/labs/mybatis/applications \
  -H 'Content-Type: application/json' -d '{"title":"MyBatis手動確認"}'
curl http://localhost:8080/labs/mybatis/applications/pending
curl -X POST http://localhost:8080/labs/mybatis/applications/{返されたid}/approve
```

`/labs/mybatis/**`はComposeが指定する`local-labs` profile限定で、OpenAPIからも非表示。Cloud用profileではController Beanが作られない。

デバッグ時は Service method → Mapper method → XML namespace/id → parameter名 → SQL → ResultMap の順で追う。
