package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.dto.BookCardDto

fun toBookCardDto(books: List<Book>): List<BookCardDto> =
    books.map { book ->
        BookCardDto(
            id        = book.id!!,
            title     = book.title,
            image     = book.image,
            genre     = book.genre,
            author    = book.author,
            addedAt   = book.createdAt,
            available = book.isAvailable(),
            active    = book.active
        )
    }
