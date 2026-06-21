package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.mapper.toUserDto
import java.time.LocalDate
import kotlin.math.ceil

data class BookRedisDto(
    val bookId:          String,
    val title:           String,
    val image:           String,
    val description:     String,
    val genre:           Genre,
    val author:          String,
    val pageCount:       Int,
    val isbn13:          String,
    val language:        String,
    val publisher:       String,
    val publicationDate: LocalDate,
    val condition:       BookCondition,
    val bookType:        String,
    val ranking:         Double,
    val ownerId:         Long,
    val ownerFirstName:  String,
    val ownerLastName:   String,
    val ownerUsername:   String,
    val bookingCount:    Int,
    val bookedRanges:    List<CachedBookedRange>,
    val active:          Boolean,
    val deleted:         Boolean
)

data class CachedBookedRange(
    val from: LocalDate,
    val to:   LocalDate
)

fun Book.toCachedDto(bookingCount: Int, owner: User) = BookRedisDto(
    bookId          = id!!,
    title           = title,
    image           = image,
    description     = description,
    genre           = genre,
    author          = author,
    pageCount       = pageCount,
    isbn13          = isbn13,
    language        = language.name,
    publisher       = publisher,
    publicationDate = publicationDate,
    condition       = condition,
    bookType        = when (this) {
        is CommonBook      -> "common"
        is DedicatedBook   -> "dedicated"
        is CollectibleBook -> "collectible"
        else               -> "common"
    },
    ranking         = ranking,
    ownerId         = ownerId,
    ownerFirstName  = owner.firstName,
    ownerLastName   = owner.lastName,
    ownerUsername   = owner.username,
    bookingCount    = bookingCount,
    bookedRanges    = bookedRanges.map { CachedBookedRange(it.from, it.to) },
    active          = active,
    deleted         = deleted
)

fun BookRedisDto.isAvailable(from: LocalDate, to: LocalDate): Boolean =
    bookedRanges.none { !it.to.isBefore(from) && !it.from.isAfter(to) }

fun BookRedisDto.bibliokarmas(userBibliokarmas: Int): Int = when (bookType) {
    "dedicated"   -> 200 + 10 * bookingCount
    "collectible" -> ceil(userBibliokarmas / 5.0).toInt() + pageCount
    else          -> if (userBibliokarmas < 1000) pageCount * 5 else pageCount * 2
}

fun BookRedisDto.passesFilters(filters: BookingFiltersRequest): Boolean {
    if (filters.minPages != null && pageCount < filters.minPages) return false
    if (filters.maxPages != null && pageCount > filters.maxPages) return false
    val checkFrom = filters.from ?: LocalDate.now()
    val checkTo   = filters.to   ?: LocalDate.now()
    return isAvailable(checkFrom, checkTo)
}

fun BookRedisDto.toBookingDto(
    user:    User,
    filters: BookingFiltersRequest,
): BookingDto {
    val from = filters.from ?: LocalDate.now()
    val to   = filters.to   ?: from
    return BookingDto(
        book = BookDetailDto(
            id              = bookId,
            title           = title,
            image           = image,
            description     = description,
            genre           = genre,
            author          = author,
            pageCount       = pageCount,
            isbn13          = isbn13,
            language        = language,
            publisher       = publisher,
            publicationDate = publicationDate,
            condition       = condition,
            bookType        = bookType,
            ranking         = ranking,
            owner           = OwnerDto(
                id        = ownerId,
                firstName = ownerFirstName,
                lastName  = ownerLastName,
                username  = ownerUsername
            )
        ),
        user         = toUserDto(user),
        from         = from,
        to           = to,
        bibliokarmas = bibliokarmas(user.bibliokarmas)
    )
}
