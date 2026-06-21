package ar.edu.unsam.phm.repository.mongo

import ar.edu.unsam.phm.domain.BookClick
import ar.edu.unsam.phm.dto.BookClickCountDto
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BookClickRepository : MongoRepository<BookClick, String> {

    @Aggregation(pipeline = [
        "{ \$match: { 'bookId': { \$in: ?0 } } }",
        "{ \$group: { '_id': '\$bookId', 'clickCount': { \$sum: 1 } } }",
        "{ \$sort: { 'clickCount': -1 } }"
    ])
    fun countClicksByBookIds(bookIds: List<String>): List<BookClickCountDto>

    @Aggregation(pipeline = [
        "{ \$group: { '_id': '\$bookId', 'clickCount': { \$sum: 1 } } }",
        "{ \$sort: { 'clickCount': -1, '_id': 1 } }",
        "{ \$project: { '_id': 0, 'id': '\$_id', 'clickCount': 1 } }"
    ])
    fun findAllByClickCount(): List<BookClickCountDto>
}
