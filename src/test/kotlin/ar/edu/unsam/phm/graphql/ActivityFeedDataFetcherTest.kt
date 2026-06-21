package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.ActivityEventType
import ar.edu.unsam.phm.dto.ActivityFeedItemDto
import ar.edu.unsam.phm.dto.ActivityUserDto
import ar.edu.unsam.phm.dto.BookCreatedActivityDto
import ar.edu.unsam.phm.dto.BookingConfirmedActivityDto
import ar.edu.unsam.phm.service.ActivityFeedService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ActivityFeedDataFetcherTest {

    private val activityFeedService: ActivityFeedService = mockk()
    private val fetcher = ActivityFeedDataFetcher(activityFeedService)

    @Test
    fun `recentActivityFeed delega en ActivityFeedService`() {
        val expected = listOf<ActivityFeedItemDto>(
            BookCreatedActivityDto(
                fecha = "2026-06-18",
                usuario = ActivityUserDto(1, "ana", "Ana Tester"),
                bookId = "book-1",
                title = "El Aleph",
            )
        )
        every { activityFeedService.getRecentActivity() } returns expected

        val result = fetcher.recentActivityFeed()

        assertEquals(expected, result)
        verify { activityFeedService.getRecentActivity() }
    }

    @Test
    fun `resolveActivityFeedItemType devuelve el tipo concreto para GraphQL`() {
        val user = ActivityUserDto(1, "ana", "Ana Tester")
        val bookEvent = BookCreatedActivityDto(
            fecha = "2026-06-18",
            usuario = user,
            bookId = "book-1",
            title = "El Aleph",
        )
        val bookingEvent = BookingConfirmedActivityDto(
            fecha = "2026-06-17",
            tipoEvento = ActivityEventType.BOOKING_CONFIRMED,
            usuario = user,
            bookingId = 10,
            bookId = "book-1",
            bookTitle = "El Aleph",
        )

        assertEquals("BookCreatedActivity", fetcher.resolveActivityFeedItemType(bookEvent))
        assertEquals("BookingConfirmedActivity", fetcher.resolveActivityFeedItemType(bookingEvent))
    }
}
