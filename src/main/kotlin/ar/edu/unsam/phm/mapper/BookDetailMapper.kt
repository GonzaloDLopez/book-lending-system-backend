package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.CollectibleBook
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.DedicatedBook
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookDetailDto
import ar.edu.unsam.phm.dto.BookDetailWithoutOwnerDto

private fun Book.bookType() = when (this) {
    is CommonBook      -> "common"
    is DedicatedBook   -> "dedicated"
    is CollectibleBook -> "collectible"
    else               -> throw IllegalStateException("Book type not recognized: ${this::class.simpleName}")
}

fun Book.toBookDetailDto(owner: User) = BookDetailDto(
    id              = this.id!!,
    title           = this.title,
    image           = this.image,
    description     = this.description,
    genre           = this.genre,
    author          = this.author,
    pageCount       = this.pageCount,
    isbn13          = this.isbn13,
    language        = this.language.name,
    publisher       = this.publisher,
    publicationDate = this.publicationDate,
    condition       = this.condition,
    bookType        = this.bookType(),
    ranking         = this.ranking,
    owner           = owner.toOwnerDto()
)

fun Book.toBookDetailWithoutOwnerDto() = BookDetailWithoutOwnerDto(
    id              = this.id!!,
    title           = this.title,
    image           = this.image,
    description     = this.description,
    genre           = this.genre,
    author          = this.author,
    pageCount       = this.pageCount,
    isbn13          = this.isbn13,
    language        = this.language.name,
    publisher       = this.publisher,
    publicationDate = this.publicationDate,
    condition       = this.condition,
    bookType        = this.bookType(),
    ranking         = this.ranking,
)
