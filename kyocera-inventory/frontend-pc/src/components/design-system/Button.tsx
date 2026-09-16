import type { ButtonHTMLAttributes, PropsWithChildren } from 'react'

type ButtonProps = PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement>>

export function Button({ children, ...props }: ButtonProps) {
  return (
    <button className="ds-button" {...props}>
      {children}
    </button>
  )
}
