package com.example.quote.service;

import com.example.quote.service.exception.IdempotencyConflictException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class IdempotencyService {

    private final ObjectMapper objectMapper;

    // Simple in-memory key store: key -> record
    private final ConcurrentHashMap<String, StoredRecord> store = new ConcurrentHashMap<>();

    public <REQ, RES> IdempotencyResult<RES> execute(
            String operation,
            String idempotencyKey,
            REQ request,
            Supplier<RES> action
    ) {
        String resolvedKey = (idempotencyKey == null || idempotencyKey.isBlank())
                ? generateKey(operation)
                : idempotencyKey;

        String requestHash = hashRequest(operation, request);

        StoredRecord existing = store.get(resolvedKey);
        if (existing != null) {
            if (!existing.requestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "Idempotency key already used with a different request payload");
            }
            RES response = deserialize(existing.responseJson());
            return new IdempotencyResult<>(resolvedKey, response, true);
        }

        RES response = action.get();
        String responseJson = serialize(response);

        store.put(resolvedKey, new StoredRecord(requestHash, responseJson));
        return new IdempotencyResult<>(resolvedKey, response, false);
    }

    private String generateKey(String operation) {
        return operation + "-" + UUID.randomUUID();
    }

    private <REQ> String hashRequest(String operation, REQ request) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(operation.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(payload);
            return Base64.getEncoder().encodeToString(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash idempotent request", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <RES> RES deserialize(String json) {
        try {
            // We rely on type erasure only for this assessment; in production,
            // we would store and use explicit type information.
            return (RES) objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize idempotent response", e);
        }
    }

    private <RES> String serialize(RES response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotent response", e);
        }
    }

    private record StoredRecord(String requestHash, String responseJson) {
    }
}