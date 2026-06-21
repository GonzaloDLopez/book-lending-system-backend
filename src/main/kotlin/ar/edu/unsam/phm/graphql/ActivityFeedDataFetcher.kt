package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.ActivityFeedItemDto
import ar.edu.unsam.phm.dto.BookCreatedActivityDto
import ar.edu.unsam.phm.dto.BookingConfirmedActivityDto
import ar.edu.unsam.phm.service.ActivityFeedService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.DgsTypeResolver

@DgsComponent
class ActivityFeedDataFetcher(private val activityFeedService: ActivityFeedService) {
    @DgsQuery
    fun recentActivityFeed(): List<ActivityFeedItemDto> =
        activityFeedService.getRecentActivity()

    @DgsTypeResolver(name = "ActivityFeedItem")
    fun resolveActivityFeedItemType(item: ActivityFeedItemDto): String =
        when (item) {
            is BookCreatedActivityDto       -> "BookCreatedActivity"
            is BookingConfirmedActivityDto -> "BookingConfirmedActivity"
        }
}
