package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.ConversionStatsDto
import ar.edu.unsam.phm.service.BookClickService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KpiDataFetcherTest {

    private val bookClickService: BookClickService = mockk()
    private val fetcher = KpiDataFetcher(bookClickService)

    @Test
    fun `kpiTasaConversion delega en BookClickService y devuelve sus resultados`() {
        val expected = listOf(ConversionStatsDto("book-1", "El Aleph", 10, 3, 0.3))
        every { bookClickService.getKpiTasaConversion() } returns expected

        val result = fetcher.kpiTasaConversion()

        assertEquals(expected, result)
        verify { bookClickService.getKpiTasaConversion() }
    }

    @Test
    fun `kpiTasaConversion retorna lista vacia cuando no hay datos`() {
        every { bookClickService.getKpiTasaConversion() } returns emptyList()

        val result = fetcher.kpiTasaConversion()

        assertEquals(emptyList<ConversionStatsDto>(), result)
    }
}
