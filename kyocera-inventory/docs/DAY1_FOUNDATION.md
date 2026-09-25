# Day1 Backend Foundation

## 到達点

```text
curl / Browser
    ↓ localhost:8081
Spring Boot 2.7 Backend
    ↓ JDBC SELECT 1
kyocera-db:5432
    ↓
PostgreSQL / kyocera_inventory
```

Day1では業務Tableを作らず、接続確認のみを行う。これによりDay2のOpenAPI/MyBatis導入時に、Build・Application起動・DB接続のどこで問題が起きたかを分離できる。

## 既存環境を守る仕組み

- `kyocera-inventory`配下だけに新規Applicationを配置。
- RootのMaven BackendやReact Sourceは変更しない。
- 専用Compose Project、Database、Volumeを使用。
- Backend/DBのHost Portを8081/5433へ分離。
- Root DevContainerは既存Featureを維持し、forward portとGradle extensionだけ追加。

## 次回へ渡す前提

Day2はこの接続済みBackendへ、OpenAPI 3.0.3のSchema First定義とMyBatis XMLによる最小在庫検索を追加する。生成Codeの直接編集やJPA追加は行わない。
