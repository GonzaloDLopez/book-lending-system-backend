package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.booking.ReviewBookingRequestDto
import ar.edu.unsam.phm.service.ReviewService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bookings")
class ReviewController(
    private val reviewService: ReviewService
) {
    @PostMapping("/{bookingId}/review")
    fun reviewBooking(
        @PathVariable bookingId: Long,
        @Valid @RequestBody request: ReviewBookingRequestDto,
        authentication: Authentication
    ) {
        reviewService.reviewBooking(
            bookingId = bookingId,
            authenticatedUserId = authentication.name.toLong(),
            score = request.score,
            comment = request.comment
        )
    }
}
