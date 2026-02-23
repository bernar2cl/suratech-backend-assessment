package com.example.quote.api;

import com.example.quote.api.dto.QuoteRequest;
import com.example.quote.api.dto.QuoteResponse;
import com.example.quote.api.dto.QuoteStatus;
import com.example.quote.service.IdempotencyResult;
import com.example.quote.service.IdempotencyService;
import com.example.quote.service.QuoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuoteController.class)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuoteService quoteService;

    @MockBean
    private IdempotencyService idempotencyService;

    @Test
    void createQuote_shouldReturn201AndLocation() throws Exception {
        QuoteRequest request = QuoteRequest.builder()
                .documentId("DOC-1")
                .customerId("CUST-1")
                .currency("USD")
                .amount(new BigDecimal("10.00"))
                .effectiveFrom(OffsetDateTime.now())
                .build();

        QuoteResponse response = QuoteResponse.builder()
                .quoteId("Q-123")
                .documentId("DOC-1")
                .customerId("CUST-1")
                .currency("USD")
                .amount(new BigDecimal("10.00"))
                .status(QuoteStatus.ISSUED)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Mockito.when(idempotencyService.execute(
                        eq("create-quote"),
                        eq("KEY-1"),
                        any(QuoteRequest.class),
                        any()))
                .thenReturn(new IdempotencyResult<>("KEY-1", response, false));

        mockMvc.perform(post("/api/v1/quotes")
                        .header("X-Idempotency-Key", "KEY-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Idempotency-Key", "KEY-1"))
                .andExpect(header().string("Location", "/api/v1/quotes/Q-123"))
                .andExpect(jsonPath("$.quoteId").value("Q-123"));
    }
}