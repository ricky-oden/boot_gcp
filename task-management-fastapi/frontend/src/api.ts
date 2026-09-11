export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'

export type Task = {
  id: number
  title: string
  assignee: string
  dueDate: string
  status: TaskStatus
  createdAt: string
}

export type TaskInput = Pick<Task, 'title' | 'assignee' | 'dueDate'>
export type TaskEdit = Partial<Pick<Task, 'title' | 'assignee' | 'dueDate' | 'status'>>
export type TaskFilter = { status?: TaskStatus | ''; assignee?: string }

const API_BASE_URL =
  (globalThis as { __API_BASE_URL__?: string }).__API_BASE_URL__ ?? 'http://localhost:8000'

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!response.ok) {
    const error = (await response.json().catch(() => ({}))) as { message?: string; detail?: unknown }
    throw new Error(error.message ?? JSON.stringify(error.detail) ?? `API error: ${response.status}`)
  }
  return response.json() as Promise<T>
}

export function listTasks(filter: TaskFilter): Promise<Task[]> {
  const params = new URLSearchParams()
  if (filter.status) params.set('status', filter.status)
  if (filter.assignee) params.set('assignee', filter.assignee)
  const query = params.size ? `?${params}` : ''
  return request<Task[]>(`/tasks${query}`)
}

export function createTask(input: TaskInput): Promise<Task> {
  return request<Task>('/tasks', { method: 'POST', body: JSON.stringify(input) })
}

export function updateTask(id: number, edit: TaskEdit): Promise<Task> {
  return request<Task>(`/tasks/${id}`, { method: 'PUT', body: JSON.stringify(edit) })
}

export function changeStatus(id: number, status: TaskStatus): Promise<Task> {
  return request<Task>(`/tasks/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}

export function nextStatus(status: TaskStatus): TaskStatus | null {
  if (status === 'TODO') return 'IN_PROGRESS'
  if (status === 'IN_PROGRESS') return 'DONE'
  return null
}
