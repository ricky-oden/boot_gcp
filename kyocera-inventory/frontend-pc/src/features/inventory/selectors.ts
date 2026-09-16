import type { RootState } from '../../app/store'

export const selectInventoryItems = (state: RootState) => state.inventory.items
export const selectInventoryLoading = (state: RootState) => state.inventory.loading
export const selectInventoryError = (state: RootState) => state.inventory.error
export const selectInventoryHasSearched = (state: RootState) => state.inventory.hasSearched
export const selectLastSearchCondition = (state: RootState) =>
  state.inventory.lastSearchCondition
