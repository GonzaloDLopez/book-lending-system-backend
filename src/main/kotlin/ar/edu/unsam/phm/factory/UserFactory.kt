package ar.edu.unsam.phm.factory

import ar.edu.unsam.phm.domain.Role
import ar.edu.unsam.phm.domain.User
import java.time.LocalDate

object UserFactory {

    /**
     * Creates a default user from primitive values.
     * Does not depend on DTOs — the service unpacks the DTO and passes the values.
     */
    fun createDefault(
        firstName: String,
        lastName:  String,
        email:     String,
        password:  String,
        username:  String,
    ): User = User(
        firstName   = firstName,
        lastName    = lastName,
        email       = email,
        username    = username,
        password    = password,
        avatar      = "",
        phone       = "",
        city        = "",
        description = "",
        bibliokarmas= 0,
        createdAt   = LocalDate.now(),
        role        = Role.reader
    )
}
