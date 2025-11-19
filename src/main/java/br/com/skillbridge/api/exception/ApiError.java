package br.com.skillbridge.api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    @Builder.Default
    List<String> details = List.of();

    public static ApiError fromStatus(HttpStatus status, String message, String path, List<String> details) {
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .details(details == null ? List.of() : details)
                .build();
    }
}

