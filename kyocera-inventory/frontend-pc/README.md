# PC向け在庫検索Frontend

Day3のReact／Redux学習Applicationです。`/inventory`で在庫検索画面を表示します。

## 情報の区分

- Reduxを使った既存画面調査・小改修を案件向け学習対象とする。
- React Router、React Hook Form、Axios、Vite、Jestはこの再現環境の学習用選定であり、実案件採用を断定しない。
- UIとAPIは学習用仮仕様であり、実案件画面ではない。

## 起動

Host Terminalから:

```bash
cd kyocera-inventory/frontend-pc
npm ci
npm run dev
```

DevContainerからBackendをComposeで起動して接続する場合:

```bash
cd kyocera-inventory/frontend-pc
VITE_API_PROXY_TARGET=http://host.docker.internal:8081 npm run dev
```

全体をComposeで起動する場合:

```bash
docker compose -f kyocera-inventory/docker-compose.yml up -d --build
```

画面は`http://localhost:5174/inventory`です。

## Command

```bash
npm test
npm run build
```

詳しい処理Flow、Browser確認、Exerciseは`../docs/DAY3_REACT_REDUX.md`を参照してください。
