package com.example.quote.service;

public record IdempotencyResult<T>(
        String idempotencyKey,
        T response,
        boolean fromCache
) {
}