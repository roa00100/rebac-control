package com.example.rebac.engine.adapter.in.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * REST 계층에서 발생하는 검증 오류와 잘못된 인자를 공통 JSON 형태로 반환합니다.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Bean Validation 실패 시 첫 필드 오류 메시지를 {@code 400} 본문에 담습니다.
     *
     * @param ex 바인딩 검증 예외
     * @return {@code message} 키를 가진 맵
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        var first =
                ex.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .orElse("validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", first));
    }

    /**
     * 도메인·엔진에서 던진 {@link IllegalArgumentException}을 {@code 400}으로 매핑합니다.
     *
     * @param ex 잘못된 인자 예외
     * @return 예외 메시지를 담은 맵
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> illegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
