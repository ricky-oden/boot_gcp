# 面談直前学習ガイド

## 30分版

1. `PROJECT_CONTEXT.md`（5分）: 2案件の確定担当と教材上の仮設定を区別する。面談ではまず案件概要と担当範囲を話す。
2. `backend/.../ApplicationController.java` と `ApplicationService.java`（7分）: Controller → Service → Repository、transactionを追う。「承認と履歴を一体で更新」と説明する。
3. `task-management-fastapi/backend/app/routers/tasks.py` と `services/task_service.py`（7分）: Router → Service → DB Accessを追う。Spring Bootとの入口の書き方の差を話す。
4. 両Backend Test（6分）: Repository Mock、正常・異常、結果・interaction verificationを対応付ける。
5. `TECH_COMPARISON.md`（5分）: Java/Spring BootとPython/FastAPIの主要差分を自分の言葉で説明する。

## 60分版

30分版に次を追加します。

1. 1件目 `frontend/src/App.tsx` と `App.test.tsx`（10分）: 一覧、React Hook Form入力、Axios、loading、error、状態別button。JestとRTLの役割を分ける。
2. 2件目 `frontend/src/App.tsx` と `api.ts` / `api.test.ts`（8分）: 検索、担当者・期限・状態更新、JestによるAPIと操作可否Testを追う。
3. `TEST_COMPARISON.md`（7分）: Backendは業務logic、Frontendは表示・操作/API連携を主にTestする違いを説明する。
4. 両案件のschema/DTO、Entity/Model、Repository（5分）: API型とDB型の違いを確認する。

## 90分版

60分版に次を追加します。

1. 両 `docker-compose.yml`（10分）: frontend / backend / dbのContainer、portと接続先を説明する。
2. 1件目READMEのCloud Run / Cloud SQL / Cloud Logging（10分）: Container Image、Profile、log確認、課金resource削除を説明する。
3. 実行確認（10分）: Reactから1件登録し、API responseとPostgreSQL rowを見る。Test commandも1回実行する。

## 30分しかない場合のTOP5

1. `PROJECT_CONTEXT.md`
2. 1件目 `ApplicationService.java`
3. 2件目 `task_service.py`
4. 両Backend Test
5. `TECH_COMPARISON.md`

細かいannotationやPython構文を暗記するより、「画面 → HTTP/JSON → API入口 → Service → DB Access → PostgreSQL」と「依存先をMockへ交換する理由」を説明できることを優先します。
