package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository
) {
        fun findById(id: Long): User =
            userRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado") }

        fun findAllById(ids: List<Long>): List<User> =
            userRepository.findAllById(ids).toList()

        fun findByEmail(email: String): User? =
            userRepository.findByEmail(email)

        fun findByUsername(username: String): User? =
            userRepository.findByUsername(username)

        fun emailExists(email: String): Boolean = 
            userRepository.existsByEmail(email)

        fun usernameExists(username: String): Boolean = 
            userRepository.existsByUsername(username)

        fun create(user: User): User = 
            userRepository.save(user)

        fun update(user: User): User = 
            userRepository.save(user)
 
}
