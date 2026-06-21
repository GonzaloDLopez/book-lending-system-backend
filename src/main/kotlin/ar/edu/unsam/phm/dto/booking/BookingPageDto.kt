package ar.edu.unsam.phm.dto.booking

data class BookingPageDto(
    val items:       List<BookingCardDto>,
    val page:        Int,
    val size:        Int,
    val totalItems:  Int,
    val totalPages:  Int,
    val hasNext:     Boolean,
    val hasPrevious: Boolean
)
