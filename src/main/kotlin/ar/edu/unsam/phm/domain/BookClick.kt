package ar.edu.unsam.phm.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "book_clicks")
class BookClick(
    val bookId:   String,
    val username: String,
    val clickedAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    var id: String? = null
}
