package ar.edu.unsam.phm.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "bookings")
class Booking(
    book:        Book,
    reader:      User,

    @Column(name = "from_date", nullable = false)
    val from:    LocalDate,

    @Column(name = "to_date",   nullable = false)
    val to:      LocalDate,

    bookingCount: Int = 0,

    @Column(nullable = true)
    var createdAt: LocalDate? = LocalDate.now()
) : Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    @Column(nullable = false)
    val bookId:     String = book.id ?: error("El libro debe estar persistido antes de reservar")

    @Column(nullable = false)
    val ownerId:    Long   = book.ownerId

    @Column(nullable = false)
    val bookTitle:  String = book.title

    @Column(nullable = false)
    val bookAuthor: String = book.author

    @Column(length = 500)
    val bookImage:  String = book.image

    @Column(nullable = false)
    val readerId:   Long   = reader.id ?: error("El lector debe estar persistido antes de reservar")

    @Column(nullable = false)
    val bibliokarmas: Int

    @Column(nullable = false)
    var cancelled: Boolean = false

    @Column(nullable = false)
    var bookRating: Double = book.ranking

    init {
        require(from <= to) { "La fecha de inicio no puede ser posterior a la de fin" }
        val base = durationInDays().toInt() * 5
        val bonus = book.bibliokarmasBonus(reader.bibliokarmas, bookingCount)
        bibliokarmas = base + bonus
    }

    fun durationInDays(): Long = ChronoUnit.DAYS.between(from, to) + 1

    fun isActive(): Boolean {
        val today = LocalDate.now()
        return !cancelled && !today.isBefore(from) && !today.isAfter(to)
    }

    fun isAboutToExpire(): Boolean =
        isActive() && ChronoUnit.DAYS.between(LocalDate.now(), to) in 0..2

    fun wasReturned(): Boolean = !cancelled && LocalDate.now().isAfter(to)

    fun overlapsWith(from: LocalDate, to: LocalDate): Boolean =
        !this.to.isBefore(from) && !this.from.isAfter(to)

    fun cancel() {
        cancelled = true
    }
}
