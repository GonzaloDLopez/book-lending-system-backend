package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.CatalogHealthDto
import ar.edu.unsam.phm.service.CatalogHealthService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogHealthDataFetcherTest {
    @Test
    fun `kpiCatalogHealth delegates to its service`() {
        val service: CatalogHealthService = mockk()
        val expected = CatalogHealthDto(15, 3, 5, 1, 6)
        every { service.getCatalogHealth() } returns expected

        assertEquals(expected, CatalogHealthDataFetcher(service).kpiCatalogHealth())
        verify { service.getCatalogHealth() }
    }
}
