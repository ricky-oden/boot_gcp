import { changeStatus, listTasks, nextStatus, updateTask } from './api'

const task = {
  id: 1,
  title: '見積確認',
  assignee: '佐藤',
  dueDate: '2026-09-10',
  status: 'TODO' as const,
  createdAt: '2026-09-09T00:00:00Z',
}

const fetchMock = jest.fn()

beforeEach(() => {
  fetchMock.mockReset()
  globalThis.fetch = fetchMock
})

test('一覧取得結果を返す', async () => {
  fetchMock.mockResolvedValueOnce(ok([task]))
  await expect(listTasks({})).resolves.toEqual([task])
})

test('statusとassigneeを検索Queryへ付ける', async () => {
  fetchMock.mockResolvedValueOnce(ok([task]))
  await listTasks({ status: 'TODO', assignee: '佐藤' })
  expect(fetchMock).toHaveBeenCalledWith(
    'http://localhost:8000/tasks?status=TODO&assignee=%E4%BD%90%E8%97%A4',
    expect.any(Object),
  )
})

test('担当者と期限の更新APIを呼ぶ', async () => {
  fetchMock.mockResolvedValueOnce(ok({ ...task, assignee: '鈴木' }))
  await updateTask(1, { assignee: '鈴木', dueDate: '2026-09-15' })
  expect(fetchMock).toHaveBeenCalledWith(
    'http://localhost:8000/tasks/1',
    expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ assignee: '鈴木', dueDate: '2026-09-15' }),
    }),
  )
})

test('APIエラーを呼び出し元へ伝える', async () => {
  fetchMock.mockResolvedValueOnce(error(404, 'Task not found: id=99'))
  await expect(changeStatus(99, 'IN_PROGRESS')).rejects.toThrow('Task not found: id=99')
})

test('DONEでは次の状態操作を許可しない', () => {
  expect(nextStatus('TODO')).toBe('IN_PROGRESS')
  expect(nextStatus('IN_PROGRESS')).toBe('DONE')
  expect(nextStatus('DONE')).toBeNull()
})

function ok(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as Response
}

function error(status: number, message: string): Response {
  return { ok: false, status, json: async () => ({ message }) } as Response
}
