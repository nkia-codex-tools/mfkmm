package com.mkfmm.dataio.adapter.inbound.controller;

import com.mkfmm.shared.dto.ErrorResponse;
import com.mkfmm.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        HttpStatus status = resolveStatus(ex.getCode());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage(), Instant.now(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("DATAIO_001", "파일 크기가 5MB를 초과합니다", Instant.now(), null));
    }

    private HttpStatus resolveStatus(String code) {
        if ("DATAIO_001".equals(code)) return HttpStatus.PAYLOAD_TOO_LARGE;
        if ("DATAIO_007".equals(code)) return HttpStatus.NO_CONTENT;
        return HttpStatus.BAD_REQUEST;
    }
}
