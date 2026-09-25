import reducer, { filterChanged, initialState } from './applicationFilterSlice'

test('初期値はALL', () => {
  expect(reducer(undefined, { type: 'unknown' })).toEqual(initialState)
})

test('filterChanged actionをreducerが反映する', () => {
  expect(reducer(initialState, filterChanged('PENDING')).value).toBe('PENDING')
})
