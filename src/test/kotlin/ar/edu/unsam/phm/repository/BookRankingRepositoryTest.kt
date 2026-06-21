package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.repository.redis.BookRankingRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.core.ZSetOperations
import java.util.concurrent.TimeUnit

class BookRankingRepositoryTest {

    private val redisTemplate: StringRedisTemplate                  = mockk()
    private val objectMapper:  ObjectMapper                         = mockk()
    private val zSetOps:       ZSetOperations<String, String>       = mockk()
    private val valueOps:      ValueOperations<String, String>      = mockk()
    private val repository = BookRankingRepository(redisTemplate, objectMapper)

    private val owner = createOwner()
    private val book  = createCommonBook(owner = owner)

    @BeforeEach
    fun setup() {
        every { redisTemplate.opsForZSet() } returns zSetOps
        every { redisTemplate.opsForValue() } returns valueOps
    }

    // ── incrementClick ────────────────────────────────────────────────────────

    @Test
    fun `incrementClick llama a incrementScore en el ZSet de ranking`() {
        every { zSetOps.incrementScore("ranking", "book-1", 1.0) } returns 5.0

        repository.incrementClick("book-1")

        verify { zSetOps.incrementScore("ranking", "book-1", 1.0) }
    }

    // ── rebuildRanking ────────────────────────────────────────────────────────

    @Test
    fun `rebuildRanking construye una clave temporal y la reemplaza de forma atomica`() {
        val stats = listOf(
            BookClickCountDto(id = "book-1", clickCount = 10L),
            BookClickCountDto(id = "book-2", clickCount = 5L)
        )
        every { zSetOps.add(match { it.startsWith("ranking:rebuild:") }, "book-1", 10.0) } returns true
        every { zSetOps.add(match { it.startsWith("ranking:rebuild:") }, "book-2", 5.0) } returns true
        every { redisTemplate.rename(match { it.startsWith("ranking:rebuild:") }, "ranking") } just runs
        every { valueOps.set("ranking:initialized", "true") } just runs

        repository.rebuildRanking(stats)

        verify { zSetOps.add(match { it.startsWith("ranking:rebuild:") }, "book-1", 10.0) }
        verify { zSetOps.add(match { it.startsWith("ranking:rebuild:") }, "book-2", 5.0) }
        verify { redisTemplate.rename(match { it.startsWith("ranking:rebuild:") }, "ranking") }
        verify { valueOps.set("ranking:initialized", "true") }
    }

    @Test
    fun `rebuildRanking vacio elimina el ranking anterior y deja la cache inicializada`() {
        every { redisTemplate.delete("ranking") } returns true
        every { valueOps.set("ranking:initialized", "true") } just runs

        repository.rebuildRanking(emptyList())

        verify { redisTemplate.delete("ranking") }
        verify { valueOps.set("ranking:initialized", "true") }
        verify(exactly = 0) { redisTemplate.rename(any<String>(), any<String>()) }
    }

    // ── getTop10BookIds ───────────────────────────────────────────────────────

    @Test
    fun `getTop10BookIds retorna los ids del ZSet ordenados por score`() {
        every { zSetOps.reverseRange("ranking", 0, 9) } returns linkedSetOf("book-1", "book-2")

        val result = repository.getTop10BookIds()

        assertEquals(listOf("book-1", "book-2"), result)
    }

    @Test
    fun `getTop10BookIds retorna lista vacía cuando Redis devuelve null`() {
        every { zSetOps.reverseRange("ranking", 0, 9) } returns null

        val result = repository.getTop10BookIds()

        assertEquals(emptyList<String>(), result)
    }

    // ── getTop5WithScores ─────────────────────────────────────────────────────

    @Test
    fun `getTop5WithScores retorna los pares bookId-score del top 5`() {
        val tuple = mockk<ZSetOperations.TypedTuple<String>> {
            every { value } returns "book-1"
            every { score } returns 10.0
        }
        every { zSetOps.reverseRangeWithScores("ranking", 0, 4) } returns setOf(tuple)

        val result = repository.getTop5WithScores()

        assertEquals(1, result.size)
        assertEquals("book-1", result[0].first)
        assertEquals(10.0, result[0].second, 0.001)
    }

    @Test
    fun `getTop5WithScores retorna lista vacía cuando Redis devuelve null`() {
        every { zSetOps.reverseRangeWithScores("ranking", 0, 4) } returns null

        val result = repository.getTop5WithScores()

        assertEquals(emptyList<Pair<String, Double>>(), result)
    }

    @Test
    fun `getTop5WithScores ignora entradas con value null`() {
        val tupleConValor = mockk<ZSetOperations.TypedTuple<String>> {
            every { value } returns "book-1"
            every { score } returns 8.0
        }
        val tuplesNull = mockk<ZSetOperations.TypedTuple<String>> {
            every { value } returns null
            every { score } returns 3.0
        }
        every { zSetOps.reverseRangeWithScores("ranking", 0, 4) } returns setOf(tupleConValor, tuplesNull)

        val result = repository.getTop5WithScores()

        assertEquals(1, result.size)
        assertEquals("book-1", result[0].first)
    }

    // ── getBookDto ────────────────────────────────────────────────────────────

    @Test
    fun `getBookDto retorna null cuando la clave no existe en Redis`() {
        every { valueOps.get("book:book-1") } returns null

        val result = repository.getBookDto("book-1")

        assertNull(result)
    }

    @Test
    fun `getBookDto retorna null cuando el JSON no se puede deserializar`() {
        every { valueOps.get("book:book-1") } returns "json-invalido"
        every { objectMapper.readValue("json-invalido", any<Class<*>>()) } throws RuntimeException("parse error")

        val result = repository.getBookDto("book-1")

        assertNull(result)
    }

    // ── updateBookCache ───────────────────────────────────────────────────────

    @Test
    fun `updateBookCache serializa el libro y lo guarda con TTL de una hora`() {
        val json = """{"bookId":"book-1"}"""
        every { objectMapper.writeValueAsString(any()) } returns json
        every { valueOps.set("book:book-1", json, 3600L, TimeUnit.SECONDS) } just runs

        repository.updateBookCache(book, 0, owner)

        verify { valueOps.set("book:book-1", json, 3600L, TimeUnit.SECONDS) }
    }
}
