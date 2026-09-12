import { createSlice, PayloadAction } from '@reduxjs/toolkit'

export type ApplicationFilter = 'ALL' | 'PENDING' | 'APPROVED'

type ApplicationFilterState = {
  value: ApplicationFilter
}

export const initialState: ApplicationFilterState = { value: 'ALL' }

const applicationFilterSlice = createSlice({
  name: 'applicationFilter',
  initialState,
  reducers: {
    filterChanged(state, action: PayloadAction<ApplicationFilter>) {
      state.value = action.payload
    },
  },
})

export const { filterChanged } = applicationFilterSlice.actions
export default applicationFilterSlice.reducer
