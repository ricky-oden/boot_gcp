import { FormEvent, useCallback, useEffect, useState } from 'react'
import {
  changeStatus,
  createTask,
  listTasks,
  nextStatus,
  Task,
  TaskFilter,
  TaskStatus,
  updateTask,
} from './api'

type EditValues = { assignee: string; dueDate: string }

export default function App() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [filter, setFilter] = useState<TaskFilter>({ status: '', assignee: '' })
  const [title, setTitle] = useState('')
  const [assignee, setAssignee] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [edits, setEdits] = useState<Record<number, EditValues>>({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const load = useCallback(async (currentFilter: TaskFilter = filter) => {
    try {
      const result = await listTasks(currentFilter)
      setTasks(result)
      setEdits(Object.fromEntries(result.map((task) => [task.id, {
        assignee: task.assignee,
        dueDate: task.dueDate,
      }])))
      setError('')
    } catch (e) {
      setError(messageOf(e))
    }
  }, [filter])

  useEffect(() => { void load({ status: '', assignee: '' }) }, [])

  async function run(action: () => Promise<unknown>) {
    setLoading(true)
    try {
      await action()
      await load()
    } catch (e) {
      setError(messageOf(e))
    } finally {
      setLoading(false)
    }
  }

  function submit(event: FormEvent) {
    event.preventDefault()
    void run(async () => {
      await createTask({ title, assignee, dueDate })
      setTitle('')
    })
  }

  function saveEdit(taskId: number) {
    const edit = edits[taskId]
    if (edit) void run(() => updateTask(taskId, edit))
  }

  function advance(task: Task) {
    const status = nextStatus(task.status)
    if (status) void run(() => changeStatus(task.id, status))
  }

  return (
    <main>
      <h1>法人向け業務依頼・進捗管理</h1>

      <section>
        <h2>検索</h2>
        <select value={filter.status} onChange={(e) => setFilter({ ...filter, status: e.target.value as TaskStatus | '' })}>
          <option value="">すべての状態</option>
          <option>TODO</option><option>IN_PROGRESS</option><option>DONE</option>
        </select>
        <input placeholder="担当者で検索" value={filter.assignee} onChange={(e) => setFilter({ ...filter, assignee: e.target.value })} />
        <button disabled={loading} onClick={() => void load()}>検索</button>
      </section>

      <form onSubmit={submit}>
        <h2>新規依頼</h2>
        <input aria-label="タイトル" placeholder="タイトル" value={title} onChange={(e) => setTitle(e.target.value)} required />
        <input aria-label="担当者" placeholder="担当者" value={assignee} onChange={(e) => setAssignee(e.target.value)} required />
        <input aria-label="期限" type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} required />
        <button disabled={loading}>登録</button>
      </form>

      {error && <p className="error">{error}</p>}

      <h2>依頼一覧</h2>
      {tasks.length === 0 ? <p>依頼はありません。</p> : (
        <ul>{tasks.map((task) => {
          const edit = edits[task.id] ?? { assignee: task.assignee, dueDate: task.dueDate }
          const next = nextStatus(task.status)
          return <li key={task.id}>
            <strong>{task.title}</strong><span className="status">{task.status}</span>
            <input aria-label={`${task.title}の担当者`} value={edit.assignee} onChange={(e) => setEdits({ ...edits, [task.id]: { ...edit, assignee: e.target.value } })} />
            <input aria-label={`${task.title}の期限`} type="date" value={edit.dueDate} onChange={(e) => setEdits({ ...edits, [task.id]: { ...edit, dueDate: e.target.value } })} />
            <button disabled={loading} onClick={() => saveEdit(task.id)}>担当者・期限を保存</button>
            {next && <button disabled={loading} onClick={() => advance(task)}>{next}へ</button>}
          </li>
        })}</ul>
      )}
    </main>
  )
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : 'APIエラーが発生しました'
}

