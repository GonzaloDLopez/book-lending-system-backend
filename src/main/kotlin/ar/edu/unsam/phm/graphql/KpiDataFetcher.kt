package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.ConversionStatsDto
import ar.edu.unsam.phm.service.BookClickService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

@DgsComponent
class KpiDataFetcher(private val bookClickService: BookClickService) {
    @DgsQuery
    fun kpiTasaConversion(): List<ConversionStatsDto> =
        bookClickService.getKpiTasaConversion()
}
