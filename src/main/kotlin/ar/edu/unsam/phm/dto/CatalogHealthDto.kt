package ar.edu.unsam.phm.dto

data class CatalogHealthDto(
    val total: Int,
    val prestados: Int,
    val disponiblesNuncaReservados: Int,
    val disponiblesReservadosAFuturo: Int,
    val disponiblesDevueltos: Int
)