import type { PropsWithChildren } from 'react'

interface AlertProps extends PropsWithChildren {
  tone?: 'error' | 'info'
}

export function Alert({ children, tone = 'info' }: AlertProps) {
  return (
    <div className={`ds-alert ds-alert--${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
