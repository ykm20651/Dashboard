package com.andamiro.Dashboard.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException → 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class) //컨트롤러/서비스에서 이 예외가 던져지면, 스프링 MVC의 ExceptionHandlerExceptionResolver가 이 메서드를 찾아 호출함.
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {

        //ex) UserService.login()에서 비밀번호 틀렸을 때 new IllegalArgumentException("비밀번호가 올바르지 않습니다.")를 던지면 여기로 들어옴.
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", e.getMessage()
        ));
    }

    // 기본 Exception → 500 Internal Server Error
    @ExceptionHandler(Exception.class) //더 넓은 타입(Exception)을 잡음. 위에서 처리하지 못한 예외는 여기로 폴백.
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", e.getMessage()
        ));
    }


}
