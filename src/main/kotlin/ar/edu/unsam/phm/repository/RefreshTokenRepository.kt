package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.RefreshToken
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    fun findByTokenHashForUpdate(@Param("tokenHash") tokenHash: String): Optional<RefreshToken>
}
