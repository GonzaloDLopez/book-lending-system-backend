package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Booking
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface BookingRepository : JpaRepository<Booking, Long> {

    @Query(value = "select pg_advisory_xact_lock(hashtext(:bookId))", nativeQuery = true)
    fun lockBookingForBook(@Param("bookId") bookId: String)

    fun countByReaderIdAndCancelledFalse(readerId: Long): Int

    fun countByBookId(bookId: String): Int

    fun countByBookIdAndCancelledFalse(bookId: String): Int

    fun countByOwnerIdAndCancelledFalse(userId: Long): Int

    @Query(
        """
        select booking
        from Booking booking
        where booking.cancelled = false
        order by coalesce(booking.createdAt, booking.from) desc, booking.id desc
        """
    )
    fun findRecentConfirmedBookings(pageable: Pageable): List<Booking>

    @Query(
        """
        select booking
        from Booking booking
        where booking.readerId = :readerId
          and booking.bookId   = :bookId
          and booking.cancelled = false
          and booking.to < :today
        order by booking.to desc, booking.id desc
        """
    )
    fun findReturnedBookingsForReaderAndBook(
        @Param("readerId") readerId: Long,
        @Param("bookId")   bookId:   String,
        @Param("today")    today:    LocalDate
    ): List<Booking>

    @Query(
        """
        select booking
        from Booking booking
        where booking.readerId = :readerId
          and booking.bookId in :bookIds
          and booking.cancelled = false
          and booking.to < :today
          and booking.id in (
              select max(innerBooking.id)
              from Booking innerBooking
              where innerBooking.readerId = :readerId
                and innerBooking.bookId   = booking.bookId
                and innerBooking.cancelled = false
                and innerBooking.to < :today
              group by innerBooking.bookId
          )
        """
    )
    fun findLatestReturnedBookingsForUserAndBooks(
        @Param("readerId") readerId: Long,
        @Param("bookIds")  bookIds:  List<String>,
        @Param("today")    today:    LocalDate
    ): List<Booking>

    @Query(
        """
        select booking
        from Booking booking
        where booking.bookId = :bookId
          and booking.from > :today
          and booking.cancelled = false
        """
    )
    fun findFutureBookingsByBookId(
        @Param("bookId") bookId: String,
        @Param("today")  today:  LocalDate
    ): List<Booking>

    @Query(
        """
        select count(b) > 0
        from Booking b
        where b.bookId = :bookId
          and b.cancelled = false
          and b.from <= :to
          and b.to >= :from
        """
    )
    fun existsOverlappingBooking(
        @Param("bookId") bookId: String,
        @Param("from")   from:   LocalDate,
        @Param("to")     to:     LocalDate
    ): Boolean

    @Query(
        """
        select booking
        from Booking booking
        where booking.cancelled = false
          and booking.from <= :today
          and booking.readerId = :userId
        """
    )
    fun findBookingsForReader(
        @Param("userId") userId: Long,
        @Param("today")  today:  LocalDate,
        pageable: Pageable
    ): Page<Booking>

    @Query(
        """
        select booking
        from Booking booking
        where booking.cancelled = false
          and booking.from <= :today
          and booking.readerId = :userId
          and (
                lower(booking.bookTitle)  like lower(concat('%', :search, '%'))
                or lower(booking.bookAuthor) like lower(concat('%', :search, '%'))
              )
        """
    )
    fun findBookingsForReaderBySearch(
        @Param("userId") userId: Long,
        @Param("search") search: String,
        @Param("today")  today:  LocalDate,
        pageable: Pageable
    ): Page<Booking>

    @Query(
        """
        select booking
        from Booking booking
        where booking.cancelled = false
          and booking.from <= :today
          and booking.ownerId = :userId
        """
    )
    fun findBookingsForOwner(
        @Param("userId") userId: Long,
        @Param("today")  today:  LocalDate,
        pageable: Pageable
    ): Page<Booking>

    @Query(
        """
        select booking
        from Booking booking
        where booking.cancelled = false
          and booking.from <= :today
          and booking.ownerId = :userId
          and (
                lower(booking.bookTitle)  like lower(concat('%', :search, '%'))
                or lower(booking.bookAuthor) like lower(concat('%', :search, '%'))
              )
        """
    )
    fun findBookingsForOwnerBySearch(
        @Param("userId") userId: Long,
        @Param("search") search: String,
        @Param("today")  today:  LocalDate,
        pageable: Pageable
    ): Page<Booking>

    @Modifying
    @Transactional
    @Query(
        """
        update Booking b
        set b.bookRating = :rating
        where b.bookId = :bookId
        """
    )
    fun updateBookRatingForBook(
        @Param("bookId") bookId: String,
        @Param("rating") rating: Double
    ): Int
}
