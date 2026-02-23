package com.example.quote.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private String quoteId;
    private String documentId;
    private String customerId;
    private String currency;
    private BigDecimal amount;
    private Double riskScore;
    private OffsetDateTime effectiveFrom;
    private OffsetDateTime effectiveTo;
    private QuoteStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Map<String, String> metadata;
}