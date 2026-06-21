package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.errors.BusinessException
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "users")
class User(
    var firstName:    String,
    var lastName:     String,

    @Column(unique = true, nullable = false)
    val email:        String,

    @Column(unique = true, nullable = false)
    val username:     String,

    val password:     String,
    var avatar:       String,
    var phone:        String,
    var city:         String,
    var description:  String,
    var bibliokarmas: Int = 0,
    val createdAt:    LocalDate,

    @Enumerated(EnumType.STRING)
    var role: Role
) : Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    fun book(book: Book, from: LocalDate, to: LocalDate, bookingCount: Int = 0): Booking {
        if (!canBook()) {
            throw BusinessException("No tenés permiso para reservar libros.")
        }
        val booking = Booking(book, this, from, to, bookingCount)
        book.addBookedRange(from, to)
        addBibliokarmas(booking.bibliokarmas)
        return booking
    }

    fun canBook(): Boolean =
        role == Role.reader || role == Role.reader_publisher

    fun canPublish(): Boolean =
        role.canPublish()

    fun addBibliokarmas(value: Int) {
        bibliokarmas += value
    }
}

//------------------------------------------

enum class Role {
    reader, publisher, reader_publisher;

    fun canPublish(): Boolean =
        this == publisher || this == reader_publisher
}
