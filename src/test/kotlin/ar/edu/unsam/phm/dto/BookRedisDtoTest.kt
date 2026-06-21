package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookRedisDtoTest {

    private val today    = LocalDate.now()
    private val tomorrow = today.plusDays(1)
    private val nextWeek = today.plusDays(7)

    private fun makeDto(
        title:          String               = "El Aleph",
        author:         String               = "Borges",
        genre:          Genre                = Genre.classic_literature,
        pageCount:      Int                  = 200,
        isbn13:         String               = "978-0000000001",
        bookType:       String               = "common",
        bookingCount:   Int                  = 0,
        bookedRanges:   List<CachedBookedRange> = emptyList(),
        ownerId:        Long                 = 1L,
        ownerFirstName: String               = "Owner",
        ownerLastName:  String               = "Test",
        ownerUsername:  String               = "owner_test"
    ) = BookRedisDto(
        bookId          = "book-1",
        title           = title,
        image           = "",
        description     = "",
        genre           = genre,
        author          = author,
        pageCount       = pageCount,
        isbn13          = isbn13,
        language        = "spanish",
        publisher       = "Emece",
        publicationDate = LocalDate.of(1949, 1, 1),
        condition       = BookCondition.good,
        bookType        = bookType,
        ranking         = 4.5,
        ownerId         = ownerId,
        ownerFirstName  = ownerFirstName,
        ownerLastName   = ownerLastName,
        ownerUsername   = ownerUsername,
        bookingCount    = bookingCount,
        bookedRanges    = bookedRanges,
        active          = true,
        deleted         = false
    )

    // ── isAvailable ──────────────────────────────────────────────────────────

    @Test
    fun `isAvailable returns true when there are no booked ranges`() {
        assertTrue(makeDto().isAvailable(tomorrow, nextWeek))
    }

    @Test
    fun `isAvailable returns true when requested range is entirely before a booked range`() {
        val dto = makeDto(bookedRanges = listOf(CachedBookedRange(nextWeek, nextWeek.plusDays(5))))
        assertTrue(dto.isAvailable(tomorrow, today.plusDays(3)))
    }

    @Test
    fun `isAvailable returns true when requested range is entirely after a booked range`() {
        val dto = makeDto(bookedRanges = listOf(CachedBookedRange(tomorrow, today.plusDays(3))))
        assertTrue(dto.isAvailable(nextWeek, nextWeek.plusDays(2)))
    }

    @Test
    fun `isAvailable returns false when requested range overlaps a booked range`() {
        val dto = makeDto(bookedRanges = listOf(CachedBookedRange(today.plusDays(3), today.plusDays(10))))
        assertFalse(dto.isAvailable(tomorrow, today.plusDays(5)))
    }

    @Test
    fun `isAvailable returns false when requested range is exactly the booked range`() {
        val dto = makeDto(bookedRanges = listOf(CachedBookedRange(tomorrow, nextWeek)))
        assertFalse(dto.isAvailable(tomorrow, nextWeek))
    }

    // ── bibliokarmas ─────────────────────────────────────────────────────────

    @Test
    fun `bibliokarmas for common book with less than 1000 bibliokarmas returns pageCount times 5`() {
        val dto = makeDto(bookType = "common", pageCount = 200)
        assertEquals(1000, dto.bibliokarmas(500))
    }

    @Test
    fun `bibliokarmas for common book with 1000 or more bibliokarmas returns pageCount times 2`() {
        val dto = makeDto(bookType = "common", pageCount = 200)
        assertEquals(400, dto.bibliokarmas(1000))
    }

    @Test
    fun `bibliokarmas for dedicated book is 200 plus 10 times bookingCount`() {
        val dto = makeDto(bookType = "dedicated", bookingCount = 5)
        assertEquals(250, dto.bibliokarmas(0))
    }

    @Test
    fun `bibliokarmas for collectible book is ceil of userBibliokarmas divided by 5 plus pageCount`() {
        val dto = makeDto(bookType = "collectible", pageCount = 100)
        assertEquals(103, dto.bibliokarmas(15))  // ceil(15/5.0) + 100 = 3 + 100
    }

    // ── passesFilters ────────────────────────────────────────────────────────

    @Test
    fun `passesFilters returns true when no filters are applied`() {
        assertTrue(makeDto().passesFilters(BookingFiltersRequest()))
    }

    @Test
    fun `passesFilters rejects when pageCount is below minPages`() {
        assertFalse(makeDto(pageCount = 100).passesFilters(BookingFiltersRequest(minPages = 200)))
    }

    @Test
    fun `passesFilters rejects when pageCount exceeds maxPages`() {
        assertFalse(makeDto(pageCount = 500).passesFilters(BookingFiltersRequest(maxPages = 300)))
    }

    @Test
    fun `passesFilters rejects when book is not available in requested dates`() {
        val booked = listOf(CachedBookedRange(tomorrow, nextWeek))
        val dto    = makeDto(bookedRanges = booked)
        assertFalse(dto.passesFilters(BookingFiltersRequest(from = tomorrow, to = nextWeek)))
    }

    // ── toCachedDto ──────────────────────────────────────────────────────────

    @Test
    fun `toCachedDto maps CommonBook fields correctly`() {
        val owner = createOwner()
        val book  = createCommonBook(pageCount = 300, owner = owner)
        book.bookedRanges.add(BookedRange(tomorrow, nextWeek))

        val dto = book.toCachedDto(bookingCount = 2, owner = owner)

        assertEquals(book.id,          dto.bookId)
        assertEquals(book.title,        dto.title)
        assertEquals(book.author,       dto.author)
        assertEquals(book.pageCount,    dto.pageCount)
        assertEquals(book.isbn13,       dto.isbn13)
        assertEquals("common",          dto.bookType)
        assertEquals(2,                 dto.bookingCount)
        assertEquals(owner.id,          dto.ownerId)
        assertEquals(1,                 dto.bookedRanges.size)
        assertEquals(tomorrow,          dto.bookedRanges[0].from)
        assertEquals(nextWeek,          dto.bookedRanges[0].to)
    }
}
