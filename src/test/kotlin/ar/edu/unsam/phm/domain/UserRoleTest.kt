package ar.edu.unsam.phm.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class UserRoleTest {

    @Test
    fun `reader can book but cannot publish`() {
        val reader = createReaderUser()
        assertTrue(reader.canBook())
        assertFalse(reader.canPublish())
    }

    @Test
    fun `publisher can publish but cannot book`() {
        val publisher = createOwner()
        assertFalse(publisher.canBook())
        assertTrue(publisher.canPublish())
    }

    @Test
    fun `reader_publisher can both book and publish`() {
        val readerPublisher = createReaderPublisher()
        assertTrue(readerPublisher.canBook())
        assertTrue(readerPublisher.canPublish())
    }

    @Test
    fun `publisher cannot book and throws exception`() {
        val publisher = createOwner()

        assertThrows<Exception> {
            publisher.book(
                createCommonBook(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
            )
        }
    }
}
