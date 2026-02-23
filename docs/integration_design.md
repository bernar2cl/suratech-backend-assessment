Transport: Use Azure Service Bus Topic quote-issued (or Azure Event Hubs if high throughput) as the primary event backbone. MuleSoft consumes via the Azure connector.
At-least-once delivery:
Backend publishes QuoteIssued within the same transaction boundary as persisting the quote (outbox pattern or transactional outbox table + background publisher).
Publisher is idempotent using eventId (UUID) – retries re-send the same event; consumers de-duplicate on eventId.
Azure Service Bus provides at-least-once semantics; publisher retries on transient errors with exponential backoff (via Resilience4j or Service Bus SDK).
Dead Letter Queues (DLQ):
Configure Service Bus subscription with max delivery count (e.g. 10). After 10 failed deliveries, message is automatically moved to the subscription DLQ.
MuleSoft flows: on processing errors, let the broker handle retry (do not ACK on failure). Non-retryable business errors can move directly to DLQ using dead-letter operations.
A separate DLQ handler (Mule flow or Azure Function) reads from DLQ, logs to central logging, and optionally stores the payload in a quarantine store (blob/DB) for manual remediation or replay.
Idempotent consumer:
MuleSoft flow stores processed eventId (or (eventId, quoteId)) in a lightweight store (Redis / DB) to avoid double-processing if Service Bus redelivers.
End-to-end traceability:
traceId propagated from REST call to event; MuleSoft logs and passes it downstream (to CRM, policy admin, etc.) for full trace across Azure, MuleSoft, and backend.