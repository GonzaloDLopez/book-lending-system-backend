CREATE OR REPLACE FUNCTION fn_users_with_more_than_n_bookings(p_min_count INT)
RETURNS TABLE (
    user_id BIGINT,
    username VARCHAR,
    email VARCHAR,
    bookings_count BIGINT
) AS $$
BEGIN
RETURN QUERY
SELECT
    u.id,
    u.username,
    u.email,
    COUNT(b.id) AS bookings_count
FROM users u
         JOIN bookings b ON b.reader_id = u.id
WHERE b.cancelled = false
GROUP BY u.id, u.username, u.email
HAVING COUNT(b.id) > p_min_count;
END;
$$ LANGUAGE plpgsql;

/*
   SELECT * FROM fn_users_with_more_than_n_bookings(N);
*/