package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.LeaderboardEntryDto
import ar.edu.unsam.phm.service.LeaderboardService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

@DgsComponent
class LeaderboardDataFetcher(private val leaderboardService: LeaderboardService) {
    @DgsQuery
    fun kpiLeaderboardBibliokarmas(): List<LeaderboardEntryDto> =
        leaderboardService.getTop5Bibliokarmas()
}
