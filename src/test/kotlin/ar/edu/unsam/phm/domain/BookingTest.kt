package ar.edu.unsam.phm.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class BookingTest {

    @Test
    fun `cannot create a booking with start date after end date`() {
        val book   = createCommonBook()
        val reader = createReaderUser()

        assertThrows<IllegalArgumentException> {
            Booking(
                book   = book,
                reader = reader,
                from   = LocalDate.of(2025, 1, 10),
                to     = LocalDate.of(2025, 1, 5)
            )
        }
    }

    @Test
    fun `durationInDays counts both endpoints`() {
        val booking = createBooking(
            createCommonBook(),
            createReaderUser(),
            from = LocalDate.of(2025, 1, 1),
            to   = LocalDate.of(2025, 1, 6)
        )

        assertEquals(6L, booking.durationInDays())
    }

    @Test
    fun `overlapsWith detects overlap`() {
        val booking = createBooking(
            createCommonBook(),
            createReaderUser(),
            from = LocalDate.of(2025, 1, 5),
            to   = LocalDate.of(2025, 1, 10)
        )

        assertTrue(
            booking.overlapsWith(
                LocalDate.of(2025, 1, 7),
                LocalDate.of(2025, 1, 12)
            )
        )
    }

    @Test
    fun `overlapsWith detects overlap on consecutive ranges`() {
        val booking = createBooking(
            createCommonBook(),
            createReaderUser(),
            from = LocalDate.of(2025, 1, 1),
            to   = LocalDate.of(2025, 1, 5)
        )

        assertTrue(
            booking.overlapsWith(
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 1, 10)
            )
        )
    }

    @Test
    fun `wasReturned is false if booking has not ended yet`() {
        val booking = createBooking(
            createCommonBook(),
            createReaderUser(),
            from = LocalDate.now().minusDays(1),
            to   = LocalDate.now().plusDays(2)
        )

        assertFalse(booking.wasReturned())
    }

    @Test
    fun `wasReturned is true if end date has passed`() {
        val booking = createBooking(
            createCommonBook(),
            createReaderUser(),
            from = LocalDate.now().minusDays(10),
            to   = LocalDate.now().minusDays(1)
        )

        assertTrue(booking.wasReturned())
    }
}
