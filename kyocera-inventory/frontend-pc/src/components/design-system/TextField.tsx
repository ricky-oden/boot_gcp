import { forwardRef } from 'react'
import type { InputHTMLAttributes } from 'react'

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

export const TextField = forwardRef<HTMLInputElement, TextFieldProps>(
  ({ label, error, id, ...props }, ref) => {
    const inputId = id || props.name
    return (
      <label className="ds-field" htmlFor={inputId}>
        <span>{label}</span>
        <input
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${inputId}-error` : undefined}
          className="ds-input"
          id={inputId}
          ref={ref}
          {...props}
        />
        {error && (
          <span className="ds-field-error" id={`${inputId}-error`} role="alert">
            {error}
          </span>
        )}
      </label>
    )
  },
)

TextField.displayName = 'TextField'
