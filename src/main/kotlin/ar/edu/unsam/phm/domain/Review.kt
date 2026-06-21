package ar.edu.unsam.phm.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = [UniqueConstraint(
        name = "uk_review_reader_book",
        columnNames = ["reader_id", "book_id"]
    )]
)
class Review(
    @Column(name = "reader_id", nullable = false)
    val readerId: Long,

    @Column(name = "book_id", nullable = false)
    val bookId:   String,

    @Column(nullable = false)
    var score:    Int,

    @Column(nullable = false, length = 1000)
    var comment:  String
) : Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    init {
        require(score in 1..5)        { "El puntaje debe ser entre 1 y 5" }
        require(comment.isNotBlank()) { "El comentario no puede estar vacio" }
    }

    fun update(newScore: Int, newComment: String) {
        require(newScore in 1..5)        { "El puntaje debe ser entre 1 y 5" }
        require(newComment.isNotBlank()) { "El comentario no puede estar vacio" }
        score   = newScore
        comment = newComment
    }
}
