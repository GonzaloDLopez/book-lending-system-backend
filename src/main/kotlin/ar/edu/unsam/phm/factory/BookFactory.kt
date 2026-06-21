package ar.edu.unsam.phm.factory

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookCondition
import ar.edu.unsam.phm.domain.CollectibleBook
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.DedicatedBook
import ar.edu.unsam.phm.domain.Genre
import ar.edu.unsam.phm.domain.Language
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookRequest
import java.time.LocalDate

object BookFactory {

    fun create(req: BookRequest, owner: User): Book =
        instantiate(req, owner)

    fun update(req: BookRequest, existingBook: Book): Book {
        existingBook.title           = req.title
        existingBook.image           = req.image
        existingBook.description     = req.description
        existingBook.genre           = Genre.valueOf(req.genre)
        existingBook.author          = req.author
        existingBook.pageCount       = req.pageCount
        existingBook.isbn13          = req.isbn13
        existingBook.language        = Language.valueOf(req.language)
        existingBook.publisher       = req.publisher
        existingBook.publicationDate = LocalDate.parse(req.publicationDate)
        existingBook.condition       = BookCondition.valueOf(req.condition)
        return existingBook
    }

    private fun instantiate(req: BookRequest, owner: User): Book {
        val ownerId         = owner.id ?: error("El owner debe estar persistido antes de crear un libro")
        val title           = req.title
        val image           = req.image
        val description     = req.description
        val genre           = Genre.valueOf(req.genre)
        val author          = req.author
        val pageCount       = req.pageCount
        val isbn13          = req.isbn13
        val language        = Language.valueOf(req.language)
        val publisher       = req.publisher
        val publicationDate = LocalDate.parse(req.publicationDate)
        val condition       = BookCondition.valueOf(req.condition)

        return when (req.type) {
            "common"      -> CommonBook(title, image, description, genre, author, pageCount, isbn13, language, publisher, publicationDate, condition, ownerId)
            "dedicated"   -> DedicatedBook(title, image, description, genre, author, pageCount, isbn13, language, publisher, publicationDate, condition, ownerId)
            "collectible" -> CollectibleBook(title, image, description, genre, author, pageCount, isbn13, language, publisher, publicationDate, condition, ownerId)
            else          -> throw IllegalArgumentException("Unknown book type: ${req.type}")
        }
    }
}
