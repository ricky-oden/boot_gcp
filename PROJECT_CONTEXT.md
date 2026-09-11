# 面談用プロジェクト文脈整理

## 1件目 法人向け申請 ワークフロー管理システム

### スキルシート上の確定情報

- 期間: 2024年4月から2026年8月
- 概要: 申請登録・検索、承認・差戻し、権限制御、状態・履歴を管理する既存法人向けWebシステムの追加開発・保守
- 技術: Java 17、Spring Boot 3、TypeScript 5、React 18、PostgreSQL、Spring Data JPA、React Router、Axios、React Hook Form、JUnit、Mockito、Jest、React Testing Library、Google Cloud、Docker、DevContainer、GitHub、GitHub Issues、GitHub Actions
- Phase: 基本設計、詳細設計、製造、単体・結合Test、review、release確認、保守改修
- 担当: 画面・API・状態遷移・DBの影響調査、申請・承認機能、入力検証、transaction・例外処理、React画面改修、単体・結合Test、PR・review対応
- Frontend状態管理: `useState` / `useEffect`等の標準Hooks。Reduxと`useReducer`は不使用
- GitHub Actions: 既存PipelineのTest / Build結果を確認する利用側。Pipeline構築担当ではない
- GCP: Cloud Run deploy、Cloud Logging、Cloud SQL接続、環境設定、release後確認を行うApplication側。Infrastructure設計・構築担当ではない
- 開発環境: Mac、VS Code、Docker、DevContainer
- Test: Unit TestとIntegration Test。Browser → API → DBまで確認

### 教材上の具体化

- 申請項目をid、title、status、createdAtへ限定
- 状態をPENDING / APPROVEDへ限定
- 承認時にApplicationHistoryを1件追加
- Cloud Run学習用にH2 Profileを追加
- Local port、Docker Compose構成、dependency version

H2、具体的なEntity項目・状態名、local port、Docker Composeの構成は教材用の簡略化であり、実案件設定ではありません。

### 面談での説明例

「既存の申請管理systemで、仕様・codeから影響範囲を調べ、Spring BootのAPI・業務logic・JPAによるDB AccessとReact画面を改修しました。単体TestではRepositoryやAxiosをMockし、結合Testでは画面からAPI・DB、状態遷移と履歴まで確認しました。GitHub ActionsとGCPは既存環境を利用・確認するApplication側として担当しました。」

## 2件目 法人向け業務依頼 進捗管理Webシステム

### スキルシート上の確定情報

- 期間: 2022年10月から2024年3月
- 概要: 社内外の業務依頼、担当者、期限、進捗状態を管理する既存Webシステムの追加開発・保守
- 技術: TypeScript、React、Python、FastAPI、PostgreSQL、pytest、Jest、Docker、GitHub
- Phase: 仕様確認、詳細設計、製造、単体・結合Test、release対応、保守改修
- 担当: 既存仕様・API・Test code調査、検索条件追加、担当者・期限・状態更新、React/FastAPI/入力検証/PostgreSQL SQL変更、pytest/Jest、PR・review対応
- Google CloudとReact Testing Libraryの使用は記載されていない

### 教材上の具体化

- Task項目をid、title、assignee、dueDate、status、createdAtに設定
- 状態をTODO / IN_PROGRESS / DONEとし、この順の遷移だけ許可
- SQLAlchemy、Repository Pattern、FastAPI Dependsを採用
- Local port、Docker Compose構成、dependency version

SQLAlchemy、Repository Pattern、Dependsの実案件使用は未確定です。教材の責務分離と比較のためだけに採用しています。

### 面談での説明例

「既存の依頼・進捗管理systemで、FastAPIの既存構成と影響範囲を調査し、検索条件、担当者・期限・状態更新をReact・API・DBまで改修しました。pytestでBackend logic、JestでFrontendの検索やAPI連携を確認しました。教材では同じ処理を小さなTask modelで再現しています。」
