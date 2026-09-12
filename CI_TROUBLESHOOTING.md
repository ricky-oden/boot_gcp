# CIトラブルシューティング

## まず見る順番

1. 失敗したjob / step / 最初のerrorを特定する。
2. CIのJava/Node versionとローカルを比較する。
3. working-directory、file path、Linuxでの大文字小文字を確認する。
4. lock fileを使った依存解決が再現可能か確認する。
5. test failureかcompile/build failureかを分離する。
6. 同じcommandをDevContainerで再現し、最小差分で直す。

## このRepositoryのworkflow

```text
push / pull_request
  backend: checkout -> Temurin 17 -> Maven cache -> mvn test package
  frontend: checkout -> Node 20 -> npm ci -> npm test -> npm run build
```

| 症状 | 確認 |
|---|---|
| ローカルだけ成功 | JDK/Node、環境変数、未追跡ファイル依存 |
| Module not found | package-lock/pom、case、install step |
| file not found | working-directory、相対path、case |
| flaky test | 時刻、順序、共有DB、非同期wait |
| Dockerだけ接続失敗 | `localhost`誤用、service名、port |

秘密値をlogへ出さず、必要なsecretはCIのsecret storeから環境変数で渡す。
