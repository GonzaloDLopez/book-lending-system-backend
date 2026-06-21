package ar.edu.unsam.phm.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookAvailabilityTest {

    @Test
    fun `book with no bookings is available for any range`() {
        val book = createCommonBook()

        assertTrue(
            book.isAvailable(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 10)
            )
        )
    }

    @Test
    fun `book is not available if existing booking overlaps`() {
        val book = createCommonBook()

        book.addBookedRange(
            from = LocalDate.of(2025, 3, 5),
            to   = LocalDate.of(2025, 3, 15)
        )

        assertFalse(
            book.isAvailable(
                LocalDate.of(2025, 3, 10),
                LocalDate.of(2025, 3, 20)
            )
        )
    }

    @Test
    fun `book is not available if ranges touch on the same day`() {
        val book = createCommonBook()

        book.addBookedRange(
            from = LocalDate.of(2025, 3, 1),
            to   = LocalDate.of(2025, 3, 5)
        )

        assertFalse(
            book.isAvailable(
                LocalDate.of(2025, 3, 5),
                LocalDate.of(2025, 3, 10)
            )
        )
    }
}
