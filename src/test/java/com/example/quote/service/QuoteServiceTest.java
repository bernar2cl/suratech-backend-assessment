package com.example.quote.service;

import com.example.quote.api.dto.QuoteRequest;
import com.example.quote.api.dto.QuoteResponse;
import com.example.quote.api.dto.QuoteStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteServiceTest {

    private final QuoteService quoteService = new QuoteService();

    @Test
    void createQuote_shouldReturnIssuedQuote() {
        QuoteRequest request = QuoteRequest.builder()
                .documentId("DOC-123")
                .customerId("CUST-1")
                .currency("USD")
                .amount(new BigDecimal("100.00"))
                .effectiveFrom(OffsetDateTime.now())
                .build();

        QuoteResponse response = quoteService.createQuote(request);

        assertThat(response.getQuoteId()).isNotBlank();
        assertThat(response.getDocumentId()).isEqualTo("DOC-123");
        assertThat(response.getStatus()).isEqualTo(QuoteStatus.ISSUED);
    }

    @Test
    void createQuoteFallback_shouldReturnRejectedQuote() throws Exception {
        QuoteRequest request = QuoteRequest.builder()
                .documentId("DOC-FAIL")
                .customerId("CUST-2")
                .currency("EUR")
                .amount(new BigDecimal("50.00"))
                .effectiveFrom(OffsetDateTime.now())
                .build();

        QuoteResponse response = invokeFallback(request, new RuntimeException("boom"));

        assertThat(response.getStatus()).isEqualTo(QuoteStatus.REJECTED);
        assertThat(response.getQuoteId()).startsWith("FAILED-");
    }

    // Use reflection to invoke private fallback for unit coverage
    private QuoteResponse invokeFallback(QuoteRequest request, Throwable t) throws Exception {
        var method = QuoteService.class.getDeclaredMethod("createQuoteFallback", QuoteRequest.class, Throwable.class);
        method.setAccessible(true);
        return (QuoteResponse) method.invoke(quoteService, request, t);
    }
}