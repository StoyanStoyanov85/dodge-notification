package com.dodge_notification.web;

import com.dodge_notification.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@ControllerAdvice
public class ExceptionAdvice {


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResourceFound() {

        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = "Invalid request. Please check.";

        ErrorResponse responseBody = createErrorResponse(status, message);

        return ResponseEntity
                .status(status)
                .body(responseBody);
    }

    private ErrorResponse createErrorResponse(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message);
    }
}
