import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { MemoryRouter } from 'react-router-dom'
import App from './App'

jest.mock('axios')
const axiosMock = axios as jest.Mocked<typeof axios>

beforeEach(() => {
  jest.resetAllMocks()
})

test('PENDING申請の承認ボタンを押すと承認APIを呼ぶ', async () => {
  // Arrange: axiosをMockし、初期一覧と承認後一覧を準備します。
  axiosMock.get
    .mockResolvedValueOnce({
      data: [{ id: 1, title: 'PC購入申請', status: 'PENDING', createdAt: '2026-09-09T00:00:00Z' }],
    })
    .mockResolvedValueOnce({
      data: [{ id: 1, title: 'PC購入申請', status: 'APPROVED', createdAt: '2026-09-09T00:00:00Z' }],
    })
  axiosMock.post.mockResolvedValueOnce({ data: {} })
  const user = userEvent.setup()
  render(
    <MemoryRouter>
      <App />
    </MemoryRouter>,
  )

  // Act: ユーザーと同じように承認ボタンを押します。
  await user.click(await screen.findByRole('button', { name: '承認' }))

  // Assert: 承認APIのURLが正しいことを確認します。
  expect(axiosMock.post).toHaveBeenCalledWith('TODO: 承認APIのURLを記入')
})
