package com.example.quote.service;

import com.example.quote.service.exception.IdempotencyConflictException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class IdempotencyServiceTest {

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(new ObjectMapper());
    }

    record DummyRequest(String value) {}

    @Test
    void execute_shouldReuseResponseForSameKeyAndPayload() {
        DummyRequest request = new DummyRequest("A");

        IdempotencyResult<String> first = idempotencyService.execute(
                "op", "key-1", request, () -> "response-1");

        IdempotencyResult<String> second = idempotencyService.execute(
                "op", "key-1", request, () -> "response-2");

        assertThat(first.fromCache()).isFalse();
        assertThat(second.fromCache()).isTrue();
        assertThat(second.response()).isEqualTo("response-1");
    }

    @Test
    void execute_shouldThrowOnDifferentPayloadWithSameKey() {
        DummyRequest request1 = new DummyRequest("A");
        DummyRequest request2 = new DummyRequest("B");

        idempotencyService.execute("op", "key-1", request1, () -> "response-1");

        assertThatThrownBy(() ->
                idempotencyService.execute("op", "key-1", request2, () -> "response-2")
        ).isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void execute_shouldGenerateKeyIfMissing() {
        DummyRequest request = new DummyRequest("A");

        IdempotencyResult<String> result = idempotencyService.execute(
                "op", null, request, () -> "response-1");

        assertThat(result.idempotencyKey()).isNotBlank();
        assertThat(result.fromCache()).isFalse();
    }
}