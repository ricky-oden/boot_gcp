# Day3: React + Redux Toolkit在庫検索

## 情報の区分

- 学習対象: 既存React／Redux画面の調査、小改修、API疎通、Browser DevTools確認。
- 学習用選定: React Router、React Hook Form、Axios、Vite、Jestを実案件採用技術とは断定しない。
- 学習用仮定: `/inventory`画面、表示項目、Validation、Local接続方法は再現環境用である。

## Codeを追う順番

```text
InventoryPage
  ↓ React Hook Form handleSubmit
dispatch(fetchInventories(criteria))
  ↓ createAsyncThunk / pending
inventoryApi.searchInventories
  ↓ Axios GET /api/inventories?itemCode=...
Vite Proxy
  ↓
Backend API :8081
  ↓ fulfilled または rejected
inventorySlice extraReducers
  ↓
Redux State
  ↓ selectors + useAppSelector
InventoryPage再描画
```

入力途中の`itemCode`はReact Hook Formだけが管理します。Reduxには検索結果、通信状態、Error、検索実行済みか、最後に実行した条件だけを保持します。

## Local接続にVite Proxyを選んだ理由

BrowserはFrontendと同じOriginの`/api`へRequestし、ViteだけがBackendへ転送します。学習用BackendへCORS設定を追加せず、既存Backendを保護できるためです。

| 実行場所 | `VITE_API_PROXY_TARGET` |
|---|---|
| Host Terminal | `http://localhost:8081`（default） |
| DevContainer | `http://host.docker.internal:8081` |
| Docker Compose | `http://backend-api:8081` |

この方式はLocal学習環境の仮定であり、実案件の接続方式を表しません。Browserから直接別Originへ接続する場合だけ`VITE_API_BASE_URL`を設定します。

## 起動と自動Test

Repository rootから全体を起動:

```bash
docker compose -f kyocera-inventory/docker-compose.yml up -d --build
```

FrontendだけをDevContainerから起動:

```bash
cd kyocera-inventory/frontend-pc
npm ci
npm test
npm run build
VITE_API_PROXY_TARGET=http://host.docker.internal:8081 npm run dev
```

## Manual Browser Practice

1. `http://localhost:5174/inventory`を開く。
2. Item Codeへ`ITEM001`を入力する。
3. Browser DevToolsを開き、Network Tabで`Fetch/XHR`に絞る。
4. Searchを押し、短時間表示される`Loading...`を確認する。
5. Tableに東京倉庫と大阪倉庫の2行が表示されることを確認する。
6. Network Tabの`inventories?itemCode=ITEM001`を開く。
7. HeadersでRequest URL、Query String、HTTP 200を確認する。
8. ResponseでJSON配列と画面表示を対応付ける。
9. `NOT_FOUND`で検索してEmpty表示を確認する。
10. Error練習ではDevToolsのNetworkをOfflineにするかBackendを一時停止し、Error表示を確認後に必ず戻す。
11. Address Barへ`/inventory`を直接入力し、Reloadする。
12. `/`からの遷移後、Back／Forwardを操作する。

Redux DevToolsが既に使える場合は`inventory/fetchInventories/pending`と`fulfilled`または`rejected`を選び、Actionの`meta.arg`、前後の`inventory` Stateを比較します。Extensionは必須Dependencyではありません。

## Exercise A: warehouseIdをPC画面へ追加

Day2 Backendは`warehouseId`に対応済みです。完成Codeは載せず、変更候補だけ示します。

- `pages/InventoryPage.tsx`: Form型、入力Component、Submit時の条件組立て。
- `features/inventory/types.ts`: APIへ渡す検索条件の型。
- `api/inventoryApi.ts`: Query Parameterの組立て。
- `InventoryPage.test.tsx`: 入力とAPI引数。
- `inventorySlice.test.ts`: 必要なら`lastSearchCondition`の期待値。

入力途中の値をReduxへ追加せず、React Hook Formと検索済みRedux Stateの境界を保ってください。

## Exercise B: Reset Button

期待動作はForm初期化、検索結果clear、Error clearです。

- React Hook Formのreset APIを調べる。
- `inventorySlice`にどの同期Actionが必要か考える。
- PageからForm側とRedux側の両方へ、どの順番で指示するか考える。
- Initial／Result／Error各状態からのTestを追加する。

## 説明練習Question

1. Redux Storeとは何ですか。
2. Slice、Reducer、Actionは何ですか。
3. dispatchは何を行いますか。
4. selectorは何のために分離しますか。
5. `createAsyncThunk`は何を生成しますか。
6. pending／fulfilled／rejectedはどの時点で発生しますか。
7. Searchを押した時、最初に呼ばれる自作Functionはどれですか。
8. ComponentからBackend APIまで、どのFileを通りますか。
9. API ResponseはどのReducerでStateへ入りますか。
10. State更新後に画面が再描画されるのはなぜですか。
11. `useState`とReduxをどう使い分けますか。
12. React Hook FormとReduxの責務は何が違いますか。
13. AxiosをComponentから分離する理由は何ですか。
14. API Errorはどこで捕捉され、どこで表示用Stateになりますか。

## Daily Meeting練習Template

```text
昨日／今回実装したこと:
- PC在庫検索画面と、Redux asyncThunkからBackend APIへ接続する経路を実装しました。

現在動いている範囲:
- itemCode検索、Loading、結果Table、Empty、Errorまで確認済みです。

State管理:
- 入力中はReact Hook Form、検索結果と通信状態はReduxへ分けています。

API Call:
- PageがThunkをdispatchし、Thunkから分離したAxios Clientを呼びます。

Test:
- Reducer lifecycleと画面のInitial／Loading／Result／Empty／Errorを確認しました。

未確認:
- （自分で実際に未確認のBrowser／DevTools操作を書く）

詰まりと確認済み事項:
- （再現条件、Console、Network、Redux State、Backend Logの確認結果を書く）
```
