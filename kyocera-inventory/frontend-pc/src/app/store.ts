import { configureStore } from '@reduxjs/toolkit'
import inventoryReducer from '../features/inventory/inventorySlice'

export const createAppStore = () =>
  configureStore({
    reducer: {
      inventory: inventoryReducer,
    },
  })

export const store = createAppStore()

export type AppStore = ReturnType<typeof createAppStore>
export type RootState = ReturnType<AppStore['getState']>
export type AppDispatch = AppStore['dispatch']
