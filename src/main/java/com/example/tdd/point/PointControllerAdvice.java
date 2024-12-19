package com.example.tdd.point;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.tdd.ErrorResponse;

@RestControllerAdvice
public class PointControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = PointUpdateFailureException.class)
    public ResponseEntity<ErrorResponse> handle(PointUpdateFailureException e) {

        final long transactionAmount = e.getAmount();
        final String transactionType = e.getType().name();

        final String code = "400";
        final String message = new StringBuilder()
                .append(transactionAmount).append(" point ")
                .append(transactionType).append(" failed.")
                .toString();

        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(code, message));
    }
}
