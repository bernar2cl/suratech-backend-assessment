package com.example.quote.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteAccepted {

    private String requestId;
    private String status; // ACCEPTED
    private OffsetDateTime estimatedCompletionTime;
    private Map<String, String> links; // e.g. "status" -> URL
}