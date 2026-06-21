-- Garantiza que bibliokarmas no sea nulo
ALTER TABLE users
    ALTER COLUMN bibliokarmas SET NOT NULL;

-- Garantiza que bibliokarmas no sea negativo
ALTER TABLE users
    ADD CONSTRAINT chk_bibliokarmas_non_negative
        CHECK (bibliokarmas >= 0);