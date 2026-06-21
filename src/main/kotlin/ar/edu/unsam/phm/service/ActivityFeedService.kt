package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.ActivityFeedItemDto
import ar.edu.unsam.phm.dto.ActivityUserDto
import ar.edu.unsam.phm.dto.BookCreatedActivityDto
import ar.edu.unsam.phm.dto.BookingConfirmedActivityDto
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityFeedService(
    private val bookRepository:    BookRepository,
    private val bookingRepository: BookingRepository,
    private val userRepository:    UserRepository,
) {
    @Transactional(readOnly = true)
    fun getRecentActivity(): List<ActivityFeedItemDto> {
        val recentBooks    = bookRepository.findTop5ByActiveTrueAndDeletedFalseOrderByCreatedAtDescIdDesc()
        val recentBookings = bookingRepository.findRecentConfirmedBookings(PageRequest.of(0, 5))
        val usersById      = loadUsers(recentBooks, recentBookings)

        return (recentBooks.mapNotNull { it.toActivity(usersById) } +
            recentBookings.mapNotNull { it.toActivity(usersById) })
            .sortedWith(
                compareByDescending<ActivityFeedItemDto> { it.fecha }
                    .thenByDescending { eventOrder(it) }
            )
            .take(5)
    }

    private fun loadUsers(books: List<Book>, bookings: List<Booking>): Map<Long, User> {
        val userIds = (books.map { it.ownerId } + bookings.map { it.readerId }).distinct()
        return userRepository.findAllById(userIds).associateBy { it.id!! }
    }

    private fun Book.toActivity(usersById: Map<Long, User>): BookCreatedActivityDto? {
        val owner = usersById[ownerId] ?: return null
        return BookCreatedActivityDto(
            fecha   = createdAt.toString(),
            usuario = owner.toActivityUser(),
            bookId  = id ?: return null,
            title   = title,
        )
    }

    private fun Booking.toActivity(usersById: Map<Long, User>): BookingConfirmedActivityDto? {
        val reader = usersById[readerId] ?: return null
        return BookingConfirmedActivityDto(
            fecha     = (createdAt ?: from).toString(),
            usuario   = reader.toActivityUser(),
            bookingId = id ?: return null,
            bookId    = bookId,
            bookTitle = bookTitle,
        )
    }

    private fun User.toActivityUser(): ActivityUserDto =
        ActivityUserDto(
            id       = id!!,
            username = username,
            fullName = "$firstName $lastName",
        )

    private fun eventOrder(item: ActivityFeedItemDto): Int =
        when (item) {
            is BookCreatedActivityDto      -> 2
            is BookingConfirmedActivityDto -> 1
        }
}
