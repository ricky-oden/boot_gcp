import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import axios from 'axios'
import { searchInventories } from '../../api/inventoryApi'
import type { InventoryItem, InventorySearchCriteria, InventoryState } from './types'

export const initialInventoryState: InventoryState = {
  items: [],
  loading: false,
  error: null,
  hasSearched: false,
  lastSearchCondition: null,
}

export const fetchInventories = createAsyncThunk<
  InventoryItem[],
  InventorySearchCriteria,
  { rejectValue: string }
>('inventory/fetchInventories', async (criteria, { rejectWithValue }) => {
  try {
    return await searchInventories(criteria)
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const message =
        typeof error.response?.data?.message === 'string'
          ? error.response.data.message
          : error.message
      return rejectWithValue(message || '在庫検索APIへの接続に失敗しました。')
    }
    return rejectWithValue('在庫検索中に予期しないエラーが発生しました。')
  }
})

const inventorySlice = createSlice({
  name: 'inventory',
  initialState: initialInventoryState,
  reducers: {
    resetInventoryState: () => ({ ...initialInventoryState }),
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchInventories.pending, (state, action) => {
        state.loading = true
        state.error = null
        state.lastSearchCondition = action.meta.arg
      })
      .addCase(fetchInventories.fulfilled, (state, action) => {
        state.loading = false
        state.items = action.payload
        state.hasSearched = true
      })
      .addCase(fetchInventories.rejected, (state, action) => {
        state.loading = false
        state.items = []
        state.error = action.payload ?? '在庫検索に失敗しました。'
        state.hasSearched = true
      })
  },
})

export const { resetInventoryState } = inventorySlice.actions

export default inventorySlice.reducer
