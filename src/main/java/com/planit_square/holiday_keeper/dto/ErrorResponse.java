package com.planit_square.holiday_keeper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success = false;
    private final String message;
    private final String error;
    private final LocalDateTime timestamp;

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, null, LocalDateTime.now());
    }

    public static ErrorResponse of(String message, String error) {
        return new ErrorResponse(message, error, LocalDateTime.now());
    }

}