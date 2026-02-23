WITH input_documents AS (
    -- Replace with your 500 document IDs (temp table, TVP, or inline values)
    SELECT UNNEST(ARRAY[
        'DOC-1', 'DOC-2', 'DOC-3'  -- ... up to 500
    ]) AS document_id
),
ranked_quotes AS (
    SELECT
        q.*,
        ROW_NUMBER() OVER (
            PARTITION BY q.document_id
            ORDER BY q.created_at DESC, q.quote_id DESC
        ) AS rn
    FROM quote q
    JOIN input_documents d
      ON q.document_id = d.document_id
)
SELECT *
FROM ranked_quotes
WHERE rn = 1;

CREATE INDEX idx_quote_document_created
    ON quote (document_id, created_at DESC, quote_id DESC);

CREATE INDEX idx_quote_document_created_covering
    ON quote (document_id, created_at DESC, quote_id DESC)
    INCLUDE (amount, status);