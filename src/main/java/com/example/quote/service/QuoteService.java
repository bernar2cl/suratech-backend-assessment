package com.example.quote.service;

import com.example.quote.api.dto.QuoteRequest;
import com.example.quote.api.dto.QuoteResponse;
import com.example.quote.api.dto.QuoteStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    // Simple in-memory store for demo/assessment purposes
    private final Map<String, QuoteResponse> store = new ConcurrentHashMap<>();

    @CircuitBreaker(name = "quoteService", fallbackMethod = "createQuoteFallback")
    @Retry(name = "quoteService")
    public QuoteResponse createQuote(QuoteRequest request) {
        // In a real system, this would persist to a database and possibly call external systems.
        String quoteId = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        QuoteResponse response = QuoteResponse.builder()
                .quoteId(quoteId)
                .documentId(request.getDocumentId())
                .customerId(request.getCustomerId())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .riskScore(request.getRiskScore())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(QuoteStatus.ISSUED)
                .createdAt(now)
                .updatedAt(now)
                .metadata(request.getMetadata())
                .build();

        store.put(quoteId, response);

        log.info("quote_created_internal quoteId={} documentId={} customerId={}",
                quoteId, request.getDocumentId(), request.getCustomerId());

        // This is the point where domain events (e.g. QuoteIssued) would be published.
        return response;
    }

    // Resilience4j fallback – must match method signature + Throwable
    @SuppressWarnings("unused")
    private QuoteResponse createQuoteFallback(QuoteRequest request, Throwable throwable) {
        log.error("quote_create_failed_fallback documentId={} reason={}",
                request.getDocumentId(), throwable.toString());

        OffsetDateTime now = OffsetDateTime.now();

        return QuoteResponse.builder()
                .quoteId("FAILED-" + UUID.randomUUID())
                .documentId(request.getDocumentId())
                .customerId(request.getCustomerId())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .riskScore(request.getRiskScore())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(QuoteStatus.REJECTED)
                .createdAt(now)
                .updatedAt(now)
                .metadata(request.getMetadata())
                .build();
    }
}