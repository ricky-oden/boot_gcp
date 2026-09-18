# OpenAPI Schema First

`inventory-api.yaml`が在庫検索・入出庫APIの正です。Java Interface／ModelはGradleの`openApiGenerate`で`backend-api/build/generated/openapi/`へ生成します。

```bash
cd kyocera-inventory
./gradlew :backend-api:openApiValidate
./gradlew :backend-api:openApiGenerate
```

`build/`配下は自動生成物なので直接編集せず、仕様変更はYAMLへ戻して再生成してください。Day2の`warehouseId` ExerciseとDay4のOUT Exerciseは完了済みです。
