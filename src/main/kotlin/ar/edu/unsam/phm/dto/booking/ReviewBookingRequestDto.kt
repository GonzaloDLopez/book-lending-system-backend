package ar.edu.unsam.phm.dto.booking

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReviewBookingRequestDto(
    @field:Min(1)
    @field:Max(5)
    val score:   Int,

    @field:NotBlank
    @field:Size(max = 1000)
    val comment: String
)
