package ar.edu.unsam.phm.repository.redis

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.dto.BookRedisDto
import ar.edu.unsam.phm.dto.toCachedDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit
import java.util.UUID

@Repository
class BookRankingRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper:  ObjectMapper
) {
    companion object {
        private const val RANKING_KEY    = "ranking"
        private const val RANKING_INITIALIZED_KEY = "ranking:initialized"
        private const val BOOK_KEY_PREFIX = "book:"
        private const val BOOK_TTL_SECONDS = 3600L
    }

    fun incrementClick(bookId: String) {
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, bookId, 1.0)
    }

    fun rebuildRanking(clickStats: List<BookClickCountDto>) {
        if (clickStats.isEmpty()) {
            redisTemplate.delete(RANKING_KEY)
            redisTemplate.opsForValue().set(RANKING_INITIALIZED_KEY, "true")
            return
        }

        val temporaryKey = "$RANKING_KEY:rebuild:${UUID.randomUUID()}"
        clickStats.forEach { stat ->
            redisTemplate.opsForZSet().add(temporaryKey, stat.id, stat.clickCount.toDouble())
        }
        redisTemplate.rename(temporaryKey, RANKING_KEY)
        redisTemplate.opsForValue().set(RANKING_INITIALIZED_KEY, "true")
    }

    fun isRankingInitialized(): Boolean =
        redisTemplate.opsForValue().get(RANKING_INITIALIZED_KEY) == "true"

    fun getTop10BookIds(): List<String> =
        redisTemplate.opsForZSet().reverseRange(RANKING_KEY, 0, 9)?.toList() ?: emptyList()

    fun updateBookCache(book: Book, bookingCount: Int, owner: User) {
        val json = objectMapper.writeValueAsString(book.toCachedDto(bookingCount, owner))
        redisTemplate.opsForValue().set("$BOOK_KEY_PREFIX${book.id}", json, BOOK_TTL_SECONDS, TimeUnit.SECONDS)
    }

    fun getTop5WithScores(): List<Pair<String, Double>> =
        redisTemplate.opsForZSet()
            .reverseRangeWithScores(RANKING_KEY, 0, 4)
            ?.mapNotNull { typed -> typed.value?.let { Pair(it, typed.score ?: 0.0) } }
            ?: emptyList()

    fun getBookDto(bookId: String): BookRedisDto? {
        val json = redisTemplate.opsForValue().get("$BOOK_KEY_PREFIX$bookId") ?: return null
        return runCatching { objectMapper.readValue(json, BookRedisDto::class.java) }.getOrNull()
    }

    fun evictBookCache(bookId: String) {
        redisTemplate.delete("$BOOK_KEY_PREFIX$bookId")
    }
}
