package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.BookCondition
import ar.edu.unsam.phm.domain.Genre
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class BookCardDto(
    val id:        String,
    var title:     String,
    var image:     String = "",
    var genre:     Genre,
    var author:    String,
    var addedAt:   LocalDate,
    var available: Boolean,
    var active:    Boolean,
)

data class BookDetailDto(
    val id:              String,
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
    val owner:           OwnerDto,
)

data class BookDetailWithoutOwnerDto(
    val id:              String,
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
)

data class BookRequest(
    @field:NotBlank(message = "El título no puede estar vacío")
    @field:Size(max = 200, message = "El título no puede superar los 200 caracteres")
    val title: String,

    @field:NotBlank(message = "La descripción no puede estar vacía")
    @field:Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    val description: String,

    @field:NotBlank(message = "El género no puede estar vacío")
    val genre: String,

    @field:NotBlank(message = "El autor no puede estar vacío")
    @field:Size(max = 100, message = "El autor no puede superar los 100 caracteres")
    val author: String,

    @field:Min(value = 1, message = "La cantidad de páginas debe ser al menos 1")
    @field:Max(value = 5000, message = "La cantidad de páginas no puede superar 5000")
    val pageCount: Int,

    @field:NotBlank(message = "El ISBN no puede estar vacío")
    @field:Pattern(
        regexp = "^97[89]-\\d{10}$",
        message = "El ISBN debe tener el formato 978-XXXXXXXXXX o 979-XXXXXXXXXX"
    )
    val isbn13: String,

    @field:NotBlank(message = "El idioma no puede estar vacío")
    val language: String,

    @field:NotBlank(message = "La editorial no puede estar vacía")
    @field:Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
    val publisher: String,

    @field:NotBlank(message = "La fecha de publicación no puede estar vacía")
    val publicationDate: String,

    @field:NotBlank(message = "El estado no puede estar vacío")
    val condition: String,

    @field:NotBlank(message = "El tipo no puede estar vacío")
    val type: String,

    @field:Pattern(
        regexp = "^(https?://.+\\..+|/.*)?$",
        message = "La URL debe tener el formato https://sitio.com/imagen.jpg"
    )
    val image: String,
)

data class BookPageDto(
    val books:      List<BookCardDto>,
    val totalPages: Int
)

data class IdResponse(
    val id: String
)

data class BookClickCountDto(
    val id:         String,
    val clickCount: Long
)

data class ConversionStatsDto(
    val bookId:         String,
    val title:          String,
    val clicks:         Int,
    val reservas:       Int,
    val tasaConversion: Double,
)

data class RatingAverageByBookTypeDto(
    val bookType:      String,
    val averageRating: Double,
)

