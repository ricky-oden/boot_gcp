import { configureStore } from '@reduxjs/toolkit'
import applicationFilterReducer from './applicationFilterSlice'

export function createAppStore() {
  return configureStore({
    reducer: { applicationFilter: applicationFilterReducer },
  })
}

export const store = createAppStore()
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
