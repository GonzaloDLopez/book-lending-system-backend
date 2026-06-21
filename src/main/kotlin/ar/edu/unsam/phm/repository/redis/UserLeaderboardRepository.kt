package ar.edu.unsam.phm.repository.redis

import ar.edu.unsam.phm.dto.LeaderboardEntryDto
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class UserLeaderboardRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper:  ObjectMapper
) {
    companion object {
        private const val LEADERBOARD_KEY         = "leaderboard:bibliokarmas"
        private const val LEADERBOARD_TTL_SECONDS = 1200L
    }

    fun getLeaderboard(): List<LeaderboardEntryDto>? {
        val json = redisTemplate.opsForValue().get(LEADERBOARD_KEY) ?: return null
        return runCatching {
            objectMapper.readValue(json, object : TypeReference<List<LeaderboardEntryDto>>() {})
        }.getOrNull()
    }

    fun saveLeaderboard(entries: List<LeaderboardEntryDto>) {
        val json = objectMapper.writeValueAsString(entries)
        redisTemplate.opsForValue().set(LEADERBOARD_KEY, json, LEADERBOARD_TTL_SECONDS, TimeUnit.SECONDS)
    }
}
