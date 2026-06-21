package ar.edu.unsam.phm.repository

import ar.edu.unsam.phm.domain.Identifiable
import ar.edu.unsam.phm.domain.User

class RepositoryException(msg: String) : Throwable()
class SearchException(msg: String) : Throwable()

abstract class SearchStrategy<T : Identifiable> {
    fun search(collection: Set<T>, value: String): List<T> =
        collection.filter { matches(it, value) }

    abstract fun matches(entity: T, value: String): Boolean
}

class UserSearchStrategy : SearchStrategy<User>() {
    override fun matches(entity: User, value: String) =
        entity.firstName.contains(value, ignoreCase = true) ||
                entity.lastName.contains(value, ignoreCase = true) ||
                entity.username.equals(value, ignoreCase = true)
}

data class PaginatedResult<T>(
    val elements:   List<T>,
    val totalPages: Int,
    val totalItems: Int
)
