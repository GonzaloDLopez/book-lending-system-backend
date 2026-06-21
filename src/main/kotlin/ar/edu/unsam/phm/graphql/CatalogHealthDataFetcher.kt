package ar.edu.unsam.phm.graphql

import ar.edu.unsam.phm.dto.CatalogHealthDto
import ar.edu.unsam.phm.service.CatalogHealthService
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery

@DgsComponent
class CatalogHealthDataFetcher(
    private val catalogHealthService: CatalogHealthService
) {

    @DgsQuery
    fun kpiCatalogHealth(): CatalogHealthDto {
        return catalogHealthService.getCatalogHealth()
    }
}