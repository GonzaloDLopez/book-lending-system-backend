package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.LeaderboardEntryDto
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.redis.UserLeaderboardRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class LeaderboardService(
    private val userRepository:            UserRepository,
    private val userLeaderboardRepository: UserLeaderboardRepository,
) {
    private val log = LoggerFactory.getLogger(LeaderboardService::class.java)

    fun getTop5Bibliokarmas(): List<LeaderboardEntryDto> {
        readFromCache()?.let {
            log.info("[REDIS] leaderboard servido desde caché")
            return it
        }
        log.info("[REDIS] cache miss — calculando leaderboard desde PostgreSQL")
        val leaderboard = computeFromDatabase()
        writeToCache(leaderboard)
        return leaderboard
    }

    private fun computeFromDatabase(): List<LeaderboardEntryDto> =
        userRepository.findTop5ByOrderByBibliokarmasDesc().map { it.toLeaderboardEntry() }

    private fun User.toLeaderboardEntry() = LeaderboardEntryDto(
        userId       = id!!.toInt(),
        username     = username,
        bibliokarmas = bibliokarmas,
    )

    private fun readFromCache(): List<LeaderboardEntryDto>? =
        try {
            userLeaderboardRepository.getLeaderboard()
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible al leer leaderboard: ${e.message}")
            null
        }

    private fun writeToCache(entries: List<LeaderboardEntryDto>) {
        try {
            userLeaderboardRepository.saveLeaderboard(entries)
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible al guardar leaderboard: ${e.message}")
        }
    }
}
