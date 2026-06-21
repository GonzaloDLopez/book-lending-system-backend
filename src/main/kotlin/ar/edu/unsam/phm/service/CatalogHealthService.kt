package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.dto.CatalogHealthDto
import ar.edu.unsam.phm.repository.mongo.BookRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CatalogHealthService(
    private val bookRepository: BookRepository
) {

    fun getCatalogHealth(): CatalogHealthDto {
        val today = LocalDate.now()

        val books = bookRepository.findAll()
            .filter { it.active && !it.deleted }

        var prestados = 0
        var disponiblesNuncaReservados = 0
        var disponiblesReservadosAFuturo = 0
        var disponiblesDevueltos = 0

        books.forEach { book ->
            val bookedRanges = book.bookedRanges

            val tieneReservaVigente = bookedRanges.any { range ->
                !today.isBefore(range.from) && !today.isAfter(range.to)
            }

            val tieneReservaPasada = bookedRanges.any { range ->
                range.to.isBefore(today)
            }

            val tieneReservaFutura = bookedRanges.any { range ->
                range.from.isAfter(today)
            }

            when {
                tieneReservaVigente -> prestados++
                bookedRanges.isEmpty() -> disponiblesNuncaReservados++
                tieneReservaPasada -> disponiblesDevueltos++
                tieneReservaFutura -> disponiblesReservadosAFuturo++
            }
        }

        return CatalogHealthDto(
            total = books.size,
            prestados = prestados,
            disponiblesNuncaReservados = disponiblesNuncaReservados,
            disponiblesReservadosAFuturo = disponiblesReservadosAFuturo,
            disponiblesDevueltos = disponiblesDevueltos
        )
    }
}