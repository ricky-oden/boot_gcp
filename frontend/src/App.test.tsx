import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { MemoryRouter } from 'react-router-dom'
import App from './App'
import { Provider } from 'react-redux'
import { createAppStore } from './store/store'

jest.mock('axios')
const axiosMock = axios as jest.Mocked<typeof axios>

const pendingApplication = {
  id: 1,
  title: 'PC購入申請',
  status: 'PENDING' as const,
  createdAt: '2026-09-09T00:00:00Z',
}

const approvedApplication = { ...pendingApplication, status: 'APPROVED' as const }

beforeEach(() => {
  jest.resetAllMocks()
})

function renderApp() {
  return render(
    <MemoryRouter>
      <Provider store={createAppStore()}>
        <App />
      </Provider>
    </MemoryRouter>,
  )
}

test('初期表示で一覧APIを呼び、取得した申請を表示する', async () => {
  axiosMock.get.mockResolvedValueOnce({ data: [pendingApplication] })
  renderApp()
  expect(await screen.findByText('PC購入申請')).toBeInTheDocument()
  expect(axiosMock.get).toHaveBeenCalledWith('http://localhost:8080/applications')
})

test('タイトルを入力して登録するとPOST APIを呼ぶ', async () => {
  axiosMock.get
    .mockResolvedValueOnce({ data: [] })
    .mockResolvedValueOnce({ data: [pendingApplication] })
  axiosMock.post.mockResolvedValueOnce({ data: pendingApplication })
  const user = userEvent.setup()
  renderApp()
  await screen.findByText('申請はまだありません。')

  await user.type(screen.getByLabelText('申請タイトル'), 'PC購入申請')
  await user.click(screen.getByRole('button', { name: '登録' }))

  await waitFor(() => expect(axiosMock.post).toHaveBeenCalledTimes(1))
  expect(axiosMock.post).toHaveBeenCalledWith('http://localhost:8080/applications', {
    title: 'PC購入申請',
  })
})

test('PENDINGの申請には承認ボタンを表示する', async () => {
  axiosMock.get.mockResolvedValueOnce({ data: [pendingApplication] })
  renderApp()
  expect(await screen.findByRole('button', { name: '承認' })).toBeInTheDocument()
})

test('APPROVEDの申請には承認ボタンを表示しない', async () => {
  axiosMock.get.mockResolvedValueOnce({ data: [approvedApplication] })
  renderApp()
  await screen.findByText('APPROVED')
  expect(screen.queryByRole('button', { name: '承認' })).not.toBeInTheDocument()
})

test('API失敗時にエラーメッセージを表示する', async () => {
  axiosMock.get.mockRejectedValueOnce(new Error('ネットワークエラー'))
  renderApp()
  expect(await screen.findByText('ネットワークエラー')).toBeInTheDocument()
})

test('API処理中は登録ボタンを無効にする', async () => {
  axiosMock.get.mockResolvedValueOnce({ data: [] })
  const user = userEvent.setup()
  renderApp()
  await screen.findByText('申請はまだありません。')
  await user.type(screen.getByLabelText('申請タイトル'), '処理中確認')

  let completeRequest: ((response: { data: typeof pendingApplication }) => void) | undefined
  axiosMock.post.mockReturnValueOnce(
    new Promise((resolve) => {
      completeRequest = resolve
    }),
  )
  axiosMock.get.mockResolvedValueOnce({ data: [pendingApplication] })
  await user.click(screen.getByRole('button', { name: '登録' }))
  expect(screen.getByRole('button', { name: '登録' })).toBeDisabled()

  completeRequest?.({ data: pendingApplication })
  await waitFor(() => expect(screen.getByRole('button', { name: '登録' })).not.toBeDisabled())
})

test('Reduxの状態フィルターでAPPROVEDだけを表示する', async () => {
  axiosMock.get.mockResolvedValueOnce({
    data: [pendingApplication, { ...approvedApplication, id: 2, title: '承認済み申請' }],
  })
  const user = userEvent.setup()
  renderApp()
  await screen.findByText('PC購入申請')

  await user.click(screen.getByLabelText('APPROVED'))

  expect(screen.queryByText('PC購入申請')).not.toBeInTheDocument()
  expect(screen.getByText('承認済み申請')).toBeInTheDocument()
  expect(screen.getByText('APPROVED', { selector: 'span' })).toBeInTheDocument()
  expect(screen.queryByRole('button', { name: '承認' })).not.toBeInTheDocument()
})
