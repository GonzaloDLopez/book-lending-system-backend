package ar.edu.unsam.phm.domain

import java.time.LocalDate

fun createReaderUser(bibliokarmas: Int = 0): User =
    User(
        firstName    = "Ana",
        lastName     = "Garcia",
        email        = "ana@mail.com",
        username     = "anagarcia",
        password     = "1234",
        avatar       = "img.png",
        phone        = "1234567890",
        city         = "Buenos Aires",
        description  = "Lectora avida",
        bibliokarmas = bibliokarmas,
        createdAt    = LocalDate.now(),
        role         = Role.reader
    ).apply { id = 2L }

fun createOwner(): User =
    User(
        firstName    = "Carlos",
        lastName     = "Lopez",
        email        = "carlos@mail.com",
        username     = "carloslopez",
        password     = "1234",
        avatar       = "img2.png",
        phone        = "9876543210",
        city         = "Rosario",
        description  = "Duenio de libros",
        bibliokarmas = 0,
        createdAt    = LocalDate.now(),
        role         = Role.publisher
    ).apply { id = 1L }

fun createReaderPublisher(): User =
    User(
        firstName    = "Lucia",
        lastName     = "Fernandez",
        email        = "lucia@mail.com",
        username     = "luciaf",
        password     = "1234",
        avatar       = "img3.png",
        phone        = "1122334455",
        city         = "Cordoba",
        description  = "Lectora y publicadora",
        bibliokarmas = 0,
        createdAt    = LocalDate.now(),
        role         = Role.reader_publisher
    ).apply { id = 3L }

fun createCommonBook(pageCount: Int = 200, owner: User = createOwner()): CommonBook =
    CommonBook(
        title           = "El Aleph",
        description     = "Cuentos de Borges",
        genre           = Genre.classic_literature,
        author          = "Borges",
        pageCount       = pageCount,
        isbn13          = "978-0000000001",
        language        = Language.spanish,
        publisher       = "Emece",
        publicationDate = LocalDate.of(1949, 1, 1),
        condition       = BookCondition.good,
        ownerId         = owner.id!!,
        image           = ""
    ).apply { id = "book-1" }

fun createBooking(
    book:   Book,
    reader: User,
    from:   LocalDate = LocalDate.now().plusDays(1),
    to:     LocalDate = LocalDate.now().plusDays(5)
): Booking = Booking(book, reader, from, to)
