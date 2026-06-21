package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.repository.mongo.BookRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RatingAnalysisServiceTest {
    private val repository: BookRepository = mockk()
    private val service = RatingAnalysisService(repository)

    @Test
    fun `groups rated books by concrete type and averages ranking`() {
        val first = createCommonBook().apply { ranking = 4.0 }
        val second = createCommonBook().apply { id = "book-2"; ranking = 5.0 }
        every { repository.findAllByRankingGreaterThan(0.0) } returns listOf(first, second)

        val result = service.getAverageRatingsByBookType()

        assertEquals(1, result.size)
        assertEquals("CommonBook", result.single().bookType)
        assertEquals(4.5, result.single().averageRating)
    }

    @Test
    fun `returns no groups when no book has ratings`() {
        every { repository.findAllByRankingGreaterThan(0.0) } returns emptyList()
        assertEquals(emptyList<Any>(), service.getAverageRatingsByBookType())
    }
}
