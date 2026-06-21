CREATE VIEW vw_current_year_reserved_books AS
SELECT
    booking.reader_id,
    booking.book_id,
    book.title,
    booking.from_date,
    booking.to_date
FROM bookings booking
JOIN books book ON book.id = booking.book_id
WHERE booking.cancelled = false
  AND EXTRACT(YEAR FROM booking.from_date) = EXTRACT(YEAR FROM CURRENT_DATE);
