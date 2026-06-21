package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.LeaderboardEntryDto
import ar.edu.unsam.phm.service.LeaderboardService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LeaderboardDataFetcherTest {

    private val leaderboardService: LeaderboardService = mockk()
    private val fetcher = LeaderboardDataFetcher(leaderboardService)

    @Test
    fun `kpiLeaderboardBibliokarmas delega en LeaderboardService y devuelve sus resultados`() {
        val expected = listOf(LeaderboardEntryDto(1, "anita", 800))
        every { leaderboardService.getTop5Bibliokarmas() } returns expected

        val result = fetcher.kpiLeaderboardBibliokarmas()

        assertEquals(expected, result)
        verify { leaderboardService.getTop5Bibliokarmas() }
    }

    @Test
    fun `kpiLeaderboardBibliokarmas retorna lista vacia cuando no hay datos`() {
        every { leaderboardService.getTop5Bibliokarmas() } returns emptyList()

        val result = fetcher.kpiLeaderboardBibliokarmas()

        assertEquals(emptyList<LeaderboardEntryDto>(), result)
    }
}
