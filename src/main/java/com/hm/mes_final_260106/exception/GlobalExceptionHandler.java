package com.hm.mes_final_260106.exception;

import com.hm.mes_final_260106.dto.ErrorResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponseDto(e.getCode(), e.getMessage()));
    }
}
