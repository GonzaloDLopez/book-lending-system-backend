package ar.edu.unsam.phm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import kotlin.math.ceil

@Document(collection = "books")
abstract class Book(
    var title:           String,
    var image:           String = "",
    var description:     String,
    var genre:           Genre,
    var author:          String,
    var pageCount:       Int,
    var isbn13:          String,
    var language:        Language,
    var publisher:       String,
    var publicationDate: LocalDate,
    var condition:       BookCondition,
    val ownerId:         Long
) {

    @Id
    var id: String? = null

    var createdAt: LocalDate = LocalDate.now()
    var active:    Boolean   = true
    var deleted:   Boolean   = false

    var bookedRanges: MutableList<BookedRange> = mutableListOf()

    var ranking:     Double               = 0.0
    var lastReviews: MutableList<Review>  = mutableListOf()

    abstract fun bibliokarmasBonus(userBibliokarmas: Int, bookingCount: Int): Int

    fun isAvailable(from: LocalDate, to: LocalDate): Boolean =
        bookedRanges.none { it.overlapsWith(from, to) }

    fun isAvailable(): Boolean {
        val today = LocalDate.now()
        return isAvailable(today, today)
    }

    fun deactivate() { active = false }

    fun activate() { if (!deleted) active = true }

    fun delete() {
        active  = false
        deleted = true
    }

    fun addBookedRange(from: LocalDate, to: LocalDate) {
        bookedRanges.add(BookedRange(from, to))
    }

    fun bookingCount(): Int = bookedRanges.size

    fun applyReviewsUpdate(latestTwo: List<Review>, newRanking: Double) {
        ranking     = newRanking
        lastReviews = latestTwo.toMutableList()
    }
}

//------------------------------------------

data class BookedRange(
    val from: LocalDate,
    val to:   LocalDate
) {
    fun overlapsWith(otherFrom: LocalDate, otherTo: LocalDate): Boolean =
        !to.isBefore(otherFrom) && !from.isAfter(otherTo)
}

//------------------------------------------

@TypeAlias("CommonBook")
class CommonBook(
    title:           String,
    image:           String,
    description:     String,
    genre:           Genre,
    author:          String,
    pageCount:       Int,
    isbn13:          String,
    language:        Language,
    publisher:       String,
    publicationDate: LocalDate,
    condition:       BookCondition,
    ownerId:         Long
) : Book(title, image, description, genre, author, pageCount, isbn13, language, publisher, publicationDate, condition, ownerId) {

    override fun bibliokarmasBonus(userBibliokarmas: Int, bookingCount: Int): Int =
        if (userBibliokarmas < 1000) pageCount * 5
        else pageCount * 2
}

@TypeAlias("DedicatedBook")
class DedicatedBook(
    title:           String,
    image:           String,
    description:     String,
    genre:           Genre,
    author:          String,
    pageCount:       Int,
    isbn13:          String,
    language:        Language,
    publisher:       String,
    publicationDate: LocalDate,
    condition:       BookCondition,
    ownerId:         Long
) : Book(title, image, description, genre, author, pageCount, isbn13, language, publisher, publicationDate, condition, ownerId) {

    override fun bibliokarmasBonus(userBibliokarmas: Int, bookingCount: Int): Int =
        200 + 10 * bookingCount
}

@TypeAlias("CollectibleBook")
class CollectibleBook(
    title:           String,
    image:           String,
    description:     String,
    genre:           Genre,
    author:          String,
    pageCount:       Int,
    isbn13:          String,
    language:        Language,
    publisher:       String,
    publicationDate: LocalDate,
    condition:       BookCondition,
    ownerId:         Long
) : Book(title, image, description, genre, author, pageCount, isbn13, language, publisher, publicationDate, condition, ownerId) {

    override fun bibliokarmasBonus(userBibliokarmas: Int, bookingCount: Int): Int =
        ceil(userBibliokarmas / 5.00).toInt() + pageCount
}

//------------------------------------------

enum class BookCondition {
    excellent, very_good, good, regular, bad
}

enum class Language {
    spanish, english, french, portuguese, italian, chinese
}

enum class Genre {
    drama, science_fiction, romance, self_help, design, classic_literature
}
