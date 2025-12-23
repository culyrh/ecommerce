package ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final String path;
    private final int status;
    private final String code;
    private final String message;
    private final Object details;

    public static ErrorResponse of(int status, String code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(status)
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorResponse of(int status, String code, String message, String path, Object details) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(status)
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}