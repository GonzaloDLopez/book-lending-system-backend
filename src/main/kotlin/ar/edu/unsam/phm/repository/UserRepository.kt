package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean

    @Modifying
    @Query("update User u set u.bibliokarmas = u.bibliokarmas + :amount where u.id = :userId")
    fun addBibliokarmas(@Param("userId") userId: Long, @Param("amount") amount: Int)

    fun findTop5ByOrderByBibliokarmasDesc(): List<User>
}