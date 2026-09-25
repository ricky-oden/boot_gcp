import axios from 'axios'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Route, Routes } from 'react-router-dom'
import { filterChanged, type ApplicationFilter } from './store/applicationFilterSlice'
import { useAppDispatch, useAppSelector } from './store/hooks'

export type Application = {
  id: number
  title: string
  status: 'PENDING' | 'APPROVED'
  createdAt: string
}

type ApplicationForm = { title: string }
type ApiError = { response?: { data?: { message?: string } }; message?: string }

const API_BASE_URL =
  (globalThis as { __API_BASE_URL__?: string }).__API_BASE_URL__ ?? 'http://localhost:8080'

function errorMessage(error: unknown, fallback: string) {
  const apiError = error as ApiError
  return apiError.response?.data?.message ?? apiError.message ?? fallback
}

export function WorkflowPage() {
  const [applications, setApplications] = useState<Application[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const dispatch = useAppDispatch()
  const filter = useAppSelector((state) => state.applicationFilter.value)
  const { register, handleSubmit, reset } = useForm<ApplicationForm>({
    defaultValues: { title: '' },
  })

  const loadApplications = useCallback(async () => {
    try {
      const response = await axios.get<Application[]>(`${API_BASE_URL}/applications`)
      setApplications(response.data)
      setError('')
    } catch (e) {
      setError(errorMessage(e, '一覧取得に失敗しました'))
    }
  }, [])

  useEffect(() => {
    void loadApplications()
  }, [loadApplications])

  const createApplication = handleSubmit(async (form) => {
    setLoading(true)
    try {
      await axios.post<Application>(`${API_BASE_URL}/applications`, { title: form.title })
      reset()
      await loadApplications()
    } catch (e) {
      setError(errorMessage(e, '登録に失敗しました'))
    } finally {
      setLoading(false)
    }
  })

  async function approve(id: number) {
    setLoading(true)
    try {
      await axios.post<Application>(`${API_BASE_URL}/applications/${id}/approve`)
      await loadApplications()
    } catch (e) {
      setError(errorMessage(e, '承認に失敗しました'))
    } finally {
      setLoading(false)
    }
  }

  const visibleApplications = applications.filter(
    (application) => filter === 'ALL' || application.status === filter,
  )

  return (
    <main>
      <h1>法人向け申請ワークフロー</h1>
      <form onSubmit={createApplication}>
        <label htmlFor="title">申請タイトル</label>
        <input
          id="title"
          {...register('title', { required: true })}
          placeholder="例: PC購入申請"
        />
        <button disabled={loading}>登録</button>
      </form>

      {error && <p className="error">{error}</p>}

      <h2>申請一覧</h2>
      <fieldset className="filters">
        <legend>状態で絞り込み</legend>
        {(['ALL', 'PENDING', 'APPROVED'] as ApplicationFilter[]).map((value) => (
          <label key={value}>
            <input
              type="radio"
              name="application-filter"
              value={value}
              checked={filter === value}
              onChange={() => dispatch(filterChanged(value))}
            />
            {value}
          </label>
        ))}
      </fieldset>
      {visibleApplications.length === 0 ? (
        <p>申請はまだありません。</p>
      ) : (
        <ul>
          {visibleApplications.map((application) => (
            <li key={application.id}>
              <div>
                <strong>{application.title}</strong>
                <span className={`status ${application.status.toLowerCase()}`}>
                  {application.status}
                </span>
                <small>{new Date(application.createdAt).toLocaleString('ja-JP')}</small>
              </div>
              {application.status === 'PENDING' && (
                <button disabled={loading} onClick={() => void approve(application.id)}>
                  承認
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="*" element={<WorkflowPage />} />
    </Routes>
  )
}
