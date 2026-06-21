CREATE OR REPLACE VIEW vw_users_returned_bookings AS
SELECT
    u.id AS user_id,
    u.username,
    u.email,
    COUNT(b.id) AS returned_bookings_count
FROM users u
JOIN bookings b ON b.reader_id = u.id
WHERE b.cancelled = false
  AND b.to_date < CURRENT_DATE
GROUP BY u.id, u.username, u.email
HAVING COUNT(b.id) > 2;