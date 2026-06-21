package ar.edu.unsam.phm.repository.mongo

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Genre
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : MongoRepository<Book, String> {

    fun findAllByRankingGreaterThan(ranking: Double): List<Book>

    fun findAllByOwnerIdAndActiveTrue(ownerId: Long, pageable: Pageable): Page<Book>

    fun findAllByOwnerId(ownerId: Long): List<Book>

    fun findAllByOwnerIdAndActiveTrueAndDeletedFalse(ownerId: Long): List<Book>

    fun findAllByOwnerIdAndActiveFalseAndDeletedFalse(
        ownerId: Long,
        pageable: Pageable
    ): Page<Book>

    fun findAllByOwnerIdAndActiveFalseAndDeletedFalse(ownerId: Long): List<Book>

    fun findByIdAndOwnerIdAndDeletedFalse(id: String, ownerId: Long): Book?

    fun findTop5ByActiveTrueAndDeletedFalseOrderByCreatedAtDescIdDesc(): List<Book>

    @Query(
        "{ 'ownerId': ?0, 'active': true, 'deleted': false, " +
        "'bookedRanges': { \$elemMatch: { 'from': { \$lte: ?1 }, 'to': { \$gte: ?1 } } } }"
    )
    fun findBorrowedNowByOwnerId(
        ownerId: Long,
        today: LocalDate,
        pageable: Pageable
    ): Page<Book>

    @Query(
        "{ 'ownerId': ?0, 'active': true, 'deleted': false, " +
        "\$or: [ " +
        "  { 'bookedRanges': { \$exists: false } }, " +
        "  { 'bookedRanges': { \$size: 0 } }, " +
        "  { 'bookedRanges': { \$not: { \$elemMatch: { 'from': { \$lte: ?1 }, 'to': { \$gte: ?1 } } } } } " +
        "] }"
    )
    fun findAvailableNowByOwnerId(
        ownerId: Long,
        today: LocalDate,
        pageable: Pageable
    ): Page<Book>

    @Query(
        "{ " +
        "  'active': true, " +
        "  'deleted': false, " +
        "  'ownerId': { \$ne: ?0 }, " +
        "  'genre':   { \$in: ?5 }, " +
        "  'title':   { \$regex: ?#{[1] == null ? '' : [1]}, \$options: 'i' }, " +
        "  'author':  { \$regex: ?#{[3] == null ? '' : [3]}, \$options: 'i' }, " +
        "  \$and: [ " +
        "    { \$or: [ { \$expr: { \$eq: [?2, null] } }, { 'isbn13':  ?2 } ] }, " +
        "    { \$or: [ { \$expr: { \$eq: [?4, null] } }, { 'ownerId': ?4 } ] }, " +
        "    { \$or: [ { \$expr: { \$eq: [?6, null] } }, { 'pageCount': { \$gte: ?6 } } ] }, " +
        "    { \$or: [ { \$expr: { \$eq: [?7, null] } }, { 'pageCount': { \$lte: ?7 } } ] }, " +
        "    { 'bookedRanges': { \$not: { \$elemMatch: { 'from': { \$lte: ?9 }, 'to': { \$gte: ?8 } } } } } " +
        "  ] " +
        "}"
    )
    fun findFilteredBooks(
        userId:    Long,
        title:     String?,
        isbn13:    String?,
        author:    String?,
        ownerId:   Long?,
        genres:    List<Genre>,
        minPages:  Int?,
        maxPages:  Int?,
        checkFrom: LocalDate,
        checkTo:   LocalDate,
        pageable:  Pageable
    ): Page<Book>

    @Aggregation(pipeline = [
        "{ \$match: { 'ownerId': ?0, 'active': true, 'deleted': false } }",
        "{ \$addFields: { 'borrowedFlag': { \$cond: [ " +
            "{ \$anyElementTrue: { \$map: { " +
                "'input': { \$ifNull: ['\$bookedRanges', []] }, " +
                "'as': 'r', " +
                "'in': { \$and: [ { \$lte: ['\$\$r.from', ?1] }, { \$gte: ['\$\$r.to', ?1] } ] } " +
            "} } }, 0, 1 ] } } }",
        "{ \$sort: { 'borrowedFlag': 1, '_id': 1 } }",
        "{ \$skip: ?2 }",
        "{ \$limit: ?3 }"
    ])
    fun findUserBooksSortedByAvailableAsc(
        ownerId: Long,
        today: LocalDate,
        skip: Long,
        limit: Int
    ): List<Book>

    @Aggregation(pipeline = [
        "{ \$match: { 'ownerId': ?0, 'active': true, 'deleted': false } }",
        "{ \$addFields: { 'borrowedFlag': { \$cond: [ " +
            "{ \$anyElementTrue: { \$map: { " +
                "'input': { \$ifNull: ['\$bookedRanges', []] }, " +
                "'as': 'r', " +
                "'in': { \$and: [ { \$lte: ['\$\$r.from', ?1] }, { \$gte: ['\$\$r.to', ?1] } ] } " +
            "} } }, 0, 1 ] } } }",
        "{ \$sort: { 'borrowedFlag': -1, '_id': 1 } }",
        "{ \$skip: ?2 }",
        "{ \$limit: ?3 }"
    ])
    fun findUserBooksSortedByAvailableDesc(
        ownerId: Long,
        today: LocalDate,
        skip: Long,
        limit: Int
    ): List<Book>

    fun countByOwnerIdAndActiveTrueAndDeletedFalse(ownerId: Long): Long

    fun findUserBooksOrderByAvailableAsc(ownerId: Long, today: LocalDate, pageable: Pageable): Page<Book> {
        val items = findUserBooksSortedByAvailableAsc(ownerId, today, pageable.offset, pageable.pageSize)
        val total = countByOwnerIdAndActiveTrueAndDeletedFalse(ownerId)
        return PageImpl(items, pageable, total)
    }

    fun findUserBooksOrderByAvailableDesc(ownerId: Long, today: LocalDate, pageable: Pageable): Page<Book> {
        val items = findUserBooksSortedByAvailableDesc(ownerId, today, pageable.offset, pageable.pageSize)
        val total = countByOwnerIdAndActiveTrueAndDeletedFalse(ownerId)
        return PageImpl(items, pageable, total)
    }
}
