package com.example.kyocera.inventory.stockmovement;

import com.example.kyocera.inventory.generated.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import javax.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            RequestValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleValidation(Exception exception, ServletWebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException exception,
            ServletWebRequest request
    ) {
        return error(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, ServletWebRequest request) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "UNEXPECTED_ERROR",
                "予期しないServer Errorが発生しました。",
                request
        );
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            ServletWebRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                code,
                message,
                request.getRequest().getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
