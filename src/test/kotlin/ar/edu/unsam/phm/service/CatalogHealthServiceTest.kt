package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.repository.mongo.BookRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogHealthServiceTest {
    private val repository: BookRepository = mockk()
    private val service = CatalogHealthService(repository)

    @Test
    fun `catalog health creates disjoint buckets that add up to total`() {
        val owner = createOwner()
        val current = createCommonBook(owner = owner).apply {
            bookedRanges.add(ar.edu.unsam.phm.domain.BookedRange(LocalDate.now(), LocalDate.now().plusDays(1)))
        }
        val neverBooked = createCommonBook(owner = owner)
        val returned = createCommonBook(owner = owner).apply {
            bookedRanges.add(ar.edu.unsam.phm.domain.BookedRange(LocalDate.now().minusDays(5), LocalDate.now().minusDays(1)))
        }
        val future = createCommonBook(owner = owner).apply {
            bookedRanges.add(ar.edu.unsam.phm.domain.BookedRange(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
        }
        val inactive = createCommonBook(owner = owner).apply { deactivate() }
        every { repository.findAll() } returns listOf(current, neverBooked, returned, future, inactive)

        val result = service.getCatalogHealth()

        assertEquals(4, result.total)
        assertEquals(1, result.prestados)
        assertEquals(1, result.disponiblesNuncaReservados)
        assertEquals(1, result.disponiblesDevueltos)
        assertEquals(1, result.disponiblesReservadosAFuturo)
    }
}
