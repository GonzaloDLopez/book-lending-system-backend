CREATE TABLE review_score_history (
    id             SERIAL    PRIMARY KEY,
    review_id      BIGINT    NOT NULL,
    book_id        BIGINT    NOT NULL,
    previous_score NUMERIC   NOT NULL,
    new_score      NUMERIC   NOT NULL,
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION fn_track_review_score_change()
RETURNS TRIGGER AS $$
DECLARE
    v_previous_avg NUMERIC;
    v_new_avg      NUMERIC;
    v_count        INT;
BEGIN
    IF OLD.score <> NEW.score THEN
        SELECT COALESCE(AVG(score), 0), COUNT(*)
        INTO v_previous_avg, v_count
        FROM reviews
        WHERE book_id = OLD.book_id;

        v_new_avg := (v_previous_avg * v_count - OLD.score + NEW.score) / v_count;

        INSERT INTO review_score_history (review_id, book_id, previous_score, new_score)
        VALUES (OLD.id, OLD.book_id, v_previous_avg, v_new_avg);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_review_score_change
BEFORE UPDATE ON reviews
FOR EACH ROW
EXECUTE FUNCTION fn_track_review_score_change();