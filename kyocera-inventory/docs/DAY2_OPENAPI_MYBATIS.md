# Day2: OpenAPI Schema First + MyBatis XML

## 情報の区分

- 実案件確定情報: OpenAPI 3.0.3、Schema First、MyBatis XML中心の構成を学習対象とする。
- 学習用仮定: API path、商品・倉庫・在庫のTable名／Column／Seed値は学習用の最小設計であり、実案件仕様ではない。
- Day2実装範囲: `itemCode`検索まで。`warehouseId`検索は学習者Exerciseとして意図的に未実装。

## 処理を追う順番

```text
openapi/inventory-api.yaml
  ↓ Gradle openApiGenerate
generated InventoriesApi / InventoryResponse（build配下）
  ↓ implements
InventoryController
  ↓
InventoryService
  ↓
InventoryMapper interface
  ↓ namespace + statement idで対応
InventoryMapper.xml
  ↓ JOIN / dynamic SQL
PostgreSQL: item_master + warehouse + inventory
  ↓
JSON Response
```

読む順番もこのFlowに合わせると、契約、HTTP、業務処理、DB処理を混同しにくくなります。

## 自分で動かす

Repository rootから:

```bash
cd kyocera-inventory
./gradlew :backend-api:openApiValidate
./gradlew :backend-api:openApiGenerate
./gradlew :backend-api:test
docker compose up -d --build
curl "http://localhost:8081/api/inventories?itemCode=ITEM001"
curl "http://localhost:8081/api/inventories"
```

Swagger UIは`http://localhost:8081/swagger-ui.html`で開き、`/openapi/inventory-api.yaml`を表示します。AnnotationからAPI仕様を生成する構成ではありません。

## MyBatis XMLで確認する箇所

- `namespace`: Java Mapper Interfaceの完全修飾名と対応する。
- `select id="search"`: Interfaceの`search` methodと対応する。
- `resultMap`: SQL ColumnをJava propertyへ明示的に割り当てる。
- `#{itemCode}`: `@Param("itemCode")`で渡した値をbindする。
- `JOIN`: 在庫に商品名と倉庫名を結合する。
- `<where>`と`<if>`: 検索条件がある時だけWHERE句を組み立てる。

`itemCode=ITEM001`なら`WHERE i.item_code = ?`が追加され、未指定なら`<if>`が成立せずWHERE句自体が出ません。

## 説明練習Question

1. OpenAPIとSwagger UIは、それぞれ何ですか。
2. Schema Firstとは何を先に決める開発方法ですか。
3. なぜ生成Codeを直接編集してはいけませんか。
4. ControllerとServiceは何を分担していますか。
5. Mapper InterfaceとMapper XMLはどう結び付きますか。
6. MyBatis XMLの`namespace`は何を表しますか。
7. `resultMap`を使う利点は何ですか。
8. JPAとMyBatisではSQLの扱いがどう異なりますか。
9. dynamic SQLは何のために使いますか。
10. Query Parameter未指定時、生成されるSQLはどう変わりますか。

答えを暗記するより、YAML、生成Interface、Controller、XMLを指しながら30秒で説明してください。
