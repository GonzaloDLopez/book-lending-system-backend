package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.LeaderboardEntryDto
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.redis.UserLeaderboardRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.dao.QueryTimeoutException

class LeaderboardServiceTest {

    private val userRepository: UserRepository = mockk()
    private val userLeaderboardRepository: UserLeaderboardRepository = mockk(relaxUnitFun = true)
    private val service = LeaderboardService(userRepository, userLeaderboardRepository)

    @Test
    fun `getTop5Bibliokarmas sirve desde cache sin tocar PostgreSQL cuando hay cache hit`() {
        val cached = listOf(LeaderboardEntryDto(2, "anagarcia", 800))
        every { userLeaderboardRepository.getLeaderboard() } returns cached

        val result = service.getTop5Bibliokarmas()

        assertEquals(cached, result)
        verify(exactly = 0) { userRepository.findTop5ByOrderByBibliokarmasDesc() }
    }

    @Test
    fun `getTop5Bibliokarmas calcula desde PostgreSQL y guarda en cache ante cache miss`() {
        val user = createReaderUser(bibliokarmas = 800)
        every { userLeaderboardRepository.getLeaderboard() } returns null
        every { userRepository.findTop5ByOrderByBibliokarmasDesc() } returns listOf(user)

        val result = service.getTop5Bibliokarmas()

        val expected = listOf(LeaderboardEntryDto(user.id!!.toInt(), user.username, user.bibliokarmas))
        assertEquals(expected, result)
        verify { userLeaderboardRepository.saveLeaderboard(expected) }
    }

    @Test
    fun `getTop5Bibliokarmas cae a PostgreSQL cuando Redis no esta disponible`() {
        val user = createReaderUser(bibliokarmas = 800)
        every { userLeaderboardRepository.getLeaderboard() } throws QueryTimeoutException("redis down")
        every { userRepository.findTop5ByOrderByBibliokarmasDesc() } returns listOf(user)

        val result = service.getTop5Bibliokarmas()

        val expected = listOf(LeaderboardEntryDto(user.id!!.toInt(), user.username, user.bibliokarmas))
        assertEquals(expected, result)
    }
}
