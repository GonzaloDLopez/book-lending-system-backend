package ar.edu.unsam.phm.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var tokenHash: String = ""

    @Column(nullable = false)
    var email: String = ""

    @Column(nullable = false)
    var expirationDate: LocalDateTime = LocalDateTime.now()

    // Fecha máxima absoluta — no se extiende con la rotación
    // Fuerza un nuevo login después de 30 días aunque el usuario siga activo
    @Column(nullable = false)
    var absoluteExpiration: LocalDateTime = LocalDateTime.now().plusDays(30)

    @Column(nullable = false)
    var revoked: Boolean = false

    fun isValid() = !revoked
            && LocalDateTime.now().isBefore(expirationDate)
            && LocalDateTime.now().isBefore(absoluteExpiration)
}