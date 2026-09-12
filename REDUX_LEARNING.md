# Redux Toolkit ミニハンズオン

## 今回Reduxに置いたもの

申請一覧ではなく、`ALL / PENDING / APPROVED`の表示フィルターだけをstoreへ置いた。

```text
radio onChange
 -> dispatch(filterChanged("PENDING"))
 -> applicationFilterSlice.reducer
 -> store.applicationFilter.value
 -> useSelector
 -> visibleApplications再計算
```

## 用語とファイル

- store: `src/store/store.ts`
- slice / initial state / reducer / action: `applicationFilterSlice.ts`
- typed `useDispatch` / `useSelector`: `hooks.ts`
- Provider: `main.tsx`
- dispatch / selector: `App.tsx`

## 使い分け

| 手段 | 今回の例 | 向く状態 |
|---|---|---|
| `useState` | API結果、loading、error | 画面内だけの一時状態 |
| React Hook Form | title入力 | 入力・validation・submit |
| Redux Toolkit | 一覧フィルター | 複数箇所で共有・追跡したい状態 |

何でもReduxに入れるのではなく、所有者と寿命で決める。サーバー状態が複雑ならRTK Query等も候補だが、今回は概念を分離するため導入していない。

```bash
cd frontend
npm test
npm run build
```
