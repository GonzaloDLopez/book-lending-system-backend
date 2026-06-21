package ar.edu.unsam.phm.dto.booking

import java.time.LocalDate

data class BookingCardDto(
    val bookingId:    Long,
    val title:        String,
    val author:       String,
    val image:        String,
    val personLabel:  String,
    val personName:   String,
    val startDate:    LocalDate,
    val endDate:      LocalDate,
    val bibliokarmas: Int,
    val rating:       Double,
    val status:       BookingStatus,
    val canReview:    Boolean,
    val hasReview:    Boolean,
    val userReview:   UserReviewDto?
)
