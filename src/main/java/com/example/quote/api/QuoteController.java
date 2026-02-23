package com.example.quote.api;

import com.example.quote.api.dto.QuoteRequest;
import com.example.quote.api.dto.QuoteResponse;
import com.example.quote.service.IdempotencyResult;
import com.example.quote.service.IdempotencyService;
import com.example.quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private static final Logger log = LoggerFactory.getLogger(QuoteController.class);

    private final QuoteService quoteService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody QuoteRequest request
    ) {
        IdempotencyResult<QuoteResponse> result = idempotencyService.execute(
                "create-quote",
                idempotencyKey,
                request,
                () -> quoteService.createQuote(request)
        );

        QuoteResponse response = result.response();

        if (result.fromCache()) {
            log.info("quote_request_reused event=quote_created_cached idempotencyKey={} quoteId={} documentId={}",
                    result.idempotencyKey(), response.getQuoteId(), response.getDocumentId());
            return ResponseEntity
                    .ok()
                    .header("X-Idempotency-Key", result.idempotencyKey())
                    .body(response);
        }

        log.info("quote_created event=quote_created_new idempotencyKey={} quoteId={} documentId={}",
                result.idempotencyKey(), response.getQuoteId(), response.getDocumentId());

        URI location = URI.create("/api/v1/quotes/" + response.getQuoteId());
        return ResponseEntity
                .created(location)
                .header("X-Idempotency-Key", result.idempotencyKey())
                .body(response);
    }
}