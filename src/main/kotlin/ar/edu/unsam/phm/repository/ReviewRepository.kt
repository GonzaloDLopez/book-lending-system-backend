package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Review
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {

    fun findByReaderIdAndBookId(readerId: Long, bookId: String): Review?

    fun findAllByBookId(bookId: String): List<Review>

    @Query(
        """
        select r
        from Review r
        where r.bookId = :bookId
        order by r.id desc
        """
    )
    fun findLatestByBookId(@Param("bookId") bookId: String, pageable: Pageable): List<Review>

    @Query("select coalesce(avg(r.score), 0.0) from Review r where r.bookId = :bookId")
    fun averageScoreByBookId(@Param("bookId") bookId: String): Double
}
