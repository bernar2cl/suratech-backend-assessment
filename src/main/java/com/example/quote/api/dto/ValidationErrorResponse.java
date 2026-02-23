package com.example.quote.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    private String traceId;
    private String errorCode;
    private String message;
    private List<FieldErrorDto> fieldErrors;
}