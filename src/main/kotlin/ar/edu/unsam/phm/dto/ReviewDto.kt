package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.User

data class ReviewDetailDto(
    val readerId: Long,
    val username: String,
    val image:    String,
    val score:    Int,
    val comment:  String
)

fun Review.toReviewDetailDto(reader: User): ReviewDetailDto =
    ReviewDetailDto(
        readerId = this.readerId,
        username = "${reader.firstName} ${reader.lastName}",
        image    = reader.avatar,
        score    = this.score,
        comment  = this.comment
    )
