# Day2 Exercise: warehouseId検索を追加する

現在は`itemCode`だけが完成しています。次の順で`warehouseId`を追加し、Schema Firstの変更伝播を自分で確認してください。

## 触るFileのヒント

1. `openapi/inventory-api.yaml`
   - `itemCode`と同じ場所に任意Query Parameterを追加する。
   - 型は`warehouse` TableのIDと整合させる。
2. `backend-api/build.gradle`
   - 変更不要。YAML変更後に既存の生成Taskを再実行する。
3. 生成された`InventoriesApi`
   - 読んでmethod signatureの変化だけを確認する。直接編集しない。
4. `InventoryController` / `InventoryService`
   - 新しい引数を下位層へ受け渡す。
5. `InventoryMapper` / `InventoryMapper.xml`
   - Interfaceのparameter名とXMLの参照名を一致させる。
   - 既存の`<where>`内に、値がある場合だけ成立する条件を追加する。
6. Test
   - Controller、Service、Mapperの各Testに、倉庫で絞れるcaseを1つずつ追加する。
   - parameter未指定時に全件検索が維持されることも確認する。

## 完了確認

```bash
cd kyocera-inventory
./gradlew :backend-api:openApiGenerate
./gradlew :backend-api:test
```

Swagger UIでQuery Parameterが増え、異なる倉庫の同一商品から1件だけ取得できれば完了です。具体的な完成CodeとSQL条件は、このGuideには意図的に載せていません。
