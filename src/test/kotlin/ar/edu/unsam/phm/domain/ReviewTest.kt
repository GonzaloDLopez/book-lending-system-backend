package ar.edu.unsam.phm.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReviewTest {

    @Test
    fun `valid score between 1 and 5 is accepted`() {
        val review = Review(
            readerId = 2L,
            bookId   = "book-1",
            score    = 4,
            comment  = "Muy bueno"
        )
        assertEquals(4, review.score)
    }

    @Test
    fun `score above 5 throws exception`() {
        assertThrows<IllegalArgumentException> {
            Review(
                readerId = 2L,
            bookId   = "book-1",
                score    = 6,
                comment  = "Excelente"
            )
        }
    }

    @Test
    fun `score below 1 throws exception`() {
        assertThrows<IllegalArgumentException> {
            Review(
                readerId = 2L,
            bookId   = "book-1",
                score    = -1,
                comment  = "Malo"
            )
        }
    }

    @Test
    fun `update modifies score and comment`() {
        val review = Review(
            readerId = 2L,
            bookId   = "book-1",
            score    = 3,
            comment  = "Bueno"
        )

        review.update(5, "Excelente")

        assertEquals(5, review.score)
        assertEquals("Excelente", review.comment)
    }
}
