# OpenAPI / Swagger UI 学習

## URL

Docker起動後:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 記載対象

| Method | Path | 正常 | 主な異常 |
|---|---|---:|---:|
| GET | `/applications` | 200 | 500 |
| POST | `/applications` | 201 | 400, 500 |
| POST | `/applications/{id}/approve` | 200 | 404, 409, 500 |

Controller annotationからpath、request body、response code、schemaを生成する。`OpenApiIntegrationTest`はUIの見た目ではなくJSON契約の必須部分を検査する。

面談では「OpenAPIは機械可読なAPI契約、Swagger UIはその契約を閲覧・試行する画面」と区別して説明する。

## 3点の突き合わせ方

1. 実装Code: Controllerのmethod/path、DTO validation、戻り型、例外変換を見る。
2. OpenAPI: `/v3/api-docs`で同じpath、schema、response codeが契約化されているかを見る。
3. Browser Network: 実際のmethod/URL/request payload/status/response bodyが契約と一致するかを見る。

差があれば、Code変更後のspec生成漏れ、Frontendの古いrequest、例外statusの記載漏れ、の順に切り分ける。
