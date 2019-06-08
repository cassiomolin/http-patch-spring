package com.cassiomolin.patch.web.exception;


import com.cassiomolin.patch.web.resource.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ControllerAdvice
public class WebApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {

        List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> error.getObjectName() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .message("Validation error")
                .status(ex.getParameter().hasParameterAnnotation(RequestBody.class) ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_REQUEST)
                .errors(Stream.concat(globalErrors.stream(), fieldErrors.stream()).collect(Collectors.toList()))
                .build();

        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .message("Validation error")
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .errors(errors)
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), apiError.getStatus(), request);
    }
}