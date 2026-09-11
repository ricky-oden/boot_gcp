# Backend Test と Frontend Test 比較

Arrange、Act、Assertは言語やframeworkに依存しない考え方です。準備し、対象を1回動かし、結果または依存先とのやり取りを検証します。

| 観点 | Java Backend | Python Backend | React Frontend |
|---|---|---|---|
| Test対象 | `ApplicationService` | `TaskService` | 1件目は`App`、2件目はAPI関数・状態判断 |
| Tool | JUnit 5 + Mockito | pytest + `unittest.mock` | Jest、1件目のみReact Testing Library |
| Mock | JPA Repository | TaskRepository | Axios |
| Arrange | `when(...).thenReturn(...)` | `return_value` / `side_effect` | `axiosMock.get.mockResolvedValueOnce(...)` |
| Act | Service method呼び出し | Service method呼び出し | `render`、`userEvent`、API関数呼び出し |
| Result Verification | `assertEquals` / `assertThrows` | Python `assert` / `pytest.raises` | `expect` / `screen` |
| Interaction Verification | Mockito `verify` | `assert_called_once_with` | `toHaveBeenCalledWith` |
| 正常系 | PENDINGをAPPROVEDへ変更 | 担当者・期限・状態更新 | 一覧表示、登録・更新API、操作可否 |
| 異常系 | 対象なし、再承認 | 対象なし、不正状態遷移 | API失敗時のerror |

## 代表コード

Java:

```java
when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
ApplicationResponse response = applicationService.approve(1L);
assertEquals(ApplicationStatus.APPROVED, response.status());
verify(historyRepository).save(any(ApplicationHistory.class));
```

Python:

```python
repository.find_by_id.return_value = task
result = service.update_task(1, request)
assert result.assignee == "鈴木"
repository.save.assert_called_once_with(task)
```

React:

```tsx
axiosMock.get.mockResolvedValueOnce({ data: [pendingApplication] })
render(<App />)
expect(await screen.findByText('PC購入申請')).toBeInTheDocument()
```

## Toolの役割

- JUnit 5: Java Testの記述・実行、assertion。
- Mockito: Java依存先のMock、戻り値設定、interaction verification。
- pytest: Python Testの収集・実行、fixture、例外検証。通常の`assert`を使える。
- `unittest.mock`: Python依存先のMockと呼び出し検証。
- Jest: JavaScript / TypeScriptのTest Runner、`expect`、Mock。
- React Testing Library: Componentをrenderし、内部実装ではなくユーザーが見る表示と操作を検証。スキルシート上では1件目のみ確定。

JUnitとpytest、MockitoとPython Mockは役割が似ていますが、APIや実行modelが完全に対応するものではありません。
