package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.RatingAverageByBookTypeDto
import ar.edu.unsam.phm.service.RatingAnalysisService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

@DgsComponent
class RatingAnalysisDataFetcher(private val ratingAnalysisService: RatingAnalysisService) {
    @DgsQuery
    fun kpiPromedioCalificacionesPorTipo(): List<RatingAverageByBookTypeDto> =
        ratingAnalysisService.getAverageRatingsByBookType()
}
