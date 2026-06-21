package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.RatingAverageByBookTypeDto
import ar.edu.unsam.phm.service.RatingAnalysisService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RatingAnalysisDataFetcherTest {
    @Test
    fun `rating KPI delegates to its service`() {
        val service: RatingAnalysisService = mockk()
        val expected = listOf(RatingAverageByBookTypeDto("CommonBook", 4.5))
        every { service.getAverageRatingsByBookType() } returns expected

        assertEquals(expected, RatingAnalysisDataFetcher(service).kpiPromedioCalificacionesPorTipo())
        verify { service.getAverageRatingsByBookType() }
    }
}
