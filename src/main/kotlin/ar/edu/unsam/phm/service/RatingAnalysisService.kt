package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.dto.RatingAverageByBookTypeDto
import ar.edu.unsam.phm.repository.mongo.BookRepository
import org.springframework.stereotype.Service

@Service
class RatingAnalysisService(
    private val bookRepository: BookRepository,
) {
    fun getAverageRatingsByBookType(): List<RatingAverageByBookTypeDto> {
        val ratedBooks  = findRatedBooks()
        val booksByType = groupBooksByType(ratedBooks)

        return booksByType
            .map { (bookType, books) -> buildAverageDto(bookType, books) }
    }

    private fun findRatedBooks(): List<Book> =
        bookRepository.findAllByRankingGreaterThan(0.0)

    private fun groupBooksByType(books: List<Book>): Map<String, List<Book>> =
        books.groupBy { it::class.simpleName ?: "Desconocido" }

    private fun buildAverageDto(bookType: String, books: List<Book>): RatingAverageByBookTypeDto =
        RatingAverageByBookTypeDto(
            bookType      = bookType,
            averageRating = averageRanking(books),
        )

    private fun averageRanking(books: List<Book>): Double =
        books.map { it.ranking }.average()
}
