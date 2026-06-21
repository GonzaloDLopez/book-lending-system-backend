package ar.edu.unsam.phm.dto

enum class ActivityEventType {
    BOOK_CREATED,
    BOOKING_CONFIRMED
}

data class ActivityUserDto(
    val id:       Long,
    val username: String,
    val fullName: String,
)

sealed interface ActivityFeedItemDto {
    val fecha:      String
    val tipoEvento: ActivityEventType
    val usuario:    ActivityUserDto
}

data class BookCreatedActivityDto(
    override val fecha:      String,
    override val tipoEvento: ActivityEventType = ActivityEventType.BOOK_CREATED,
    override val usuario:    ActivityUserDto,
    val bookId:              String,
    val title:               String,
) : ActivityFeedItemDto

data class BookingConfirmedActivityDto(
    override val fecha:      String,
    override val tipoEvento: ActivityEventType = ActivityEventType.BOOKING_CONFIRMED,
    override val usuario:    ActivityUserDto,
    val bookingId:           Long,
    val bookId:              String,
    val bookTitle:           String,
) : ActivityFeedItemDto
