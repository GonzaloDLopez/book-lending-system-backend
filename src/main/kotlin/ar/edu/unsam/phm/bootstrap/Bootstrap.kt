package ar.edu.unsam.phm.bootstrap

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.ReviewRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookClickRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.repository.redis.BookRankingRepository
import ar.edu.unsam.phm.validator.UserValidator
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class Bootstrap : InitializingBean {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookingRepository: BookingRepository

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Autowired
    private lateinit var bookClickRepository: BookClickRepository

    @Autowired
    private lateinit var bookRankingRepository: BookRankingRepository

    private fun hashPassword(raw: String) = UserValidator.hashPassword(raw)

    private fun Book.click(vararg usernames: String) {
        usernames.forEach {
            bookClickRepository.save(BookClick(bookId = this.id!!, username = it))
            bookRankingRepository.incrementClick(this.id!!)
        }
    }

    private fun Booking.review(score: Int, comment: String) {
        reviewRepository.save(
            Review(readerId = this.readerId, bookId = this.bookId, score = score, comment = comment)
        )
    }

    private fun User.bookWithCount(book: Book, from: LocalDate, to: LocalDate): Booking {
        if (!canBook()) throw ar.edu.unsam.phm.errors.BusinessException("No tenés permiso para reservar libros.")
        val bookingCount = bookingRepository.countByBookId(book.id!!)
        return book(book, from, to, bookingCount)
    }

    override fun afterPropertiesSet() {
        if (userRepository.count() > 0) return

        // ============================================================
        // USERS
        // ============================================================

        val gonza = userRepository.save(User(
            firstName   = "Gonzalo", lastName = "Lopez",
            email       = "gonza@gmail.com", username = "gonza",
            avatar      = "https://randomuser.me/api/portraits/men/15.jpg",
            phone       = "1199887766",
            city        = "Buenos Aires",
            description = "Usuario de prueba para Mis Préstamos",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2024, 8, 1),
            password    = hashPassword("1234"),
            role        = Role.reader_publisher
        ))

        val ana = userRepository.save(User(
            firstName   = "Ana", lastName = "García",
            email       = "ana@mail.com", username = "anita",
            avatar      = "//external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstatic-cse.canva.com%2Fblob%2F1760022%2F1600w-vkBvE1d_xYA.jpg&f=1&nofb=1&ipt=9bc786c4345fb4b5012539347e8c4a1c3c1789fd8faa3a8477f347c66ede40408",
            phone       = "1122334455",
            city        = "Buenos Aires",
            description = "Lectora ávida de clásicos",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2023, 3, 10),
            password    = hashPassword("1234"),
            role        = Role.reader
        ))

        val pedro = userRepository.save(User(
            firstName   = "Pedro", lastName = "Bello",
            email       = "pedro@mail.com", username = "pedrito",
            avatar      = "https://randomuser.me/api/portraits/men/1.jpg",
            phone       = "1122337755",
            city        = "Buenos Aires",
            description = "Lector ávido de clásicos",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2023, 3, 15),
            password    = hashPassword("1234"),
            role        = Role.reader
        ))

        val enzo = userRepository.save(User(
            firstName   = "Enzo", lastName = "Francescoli",
            email       = "enzo@mail.com", username = "enzito",
            avatar      = "https://randomuser.me/api/portraits/men/2.jpg",
            phone       = "1122334455",
            city        = "Buenos Aires",
            description = "Lector de ficción",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2023, 3, 19),
            password    = hashPassword("1234"),
            role        = Role.reader
        ))

        val marcos = userRepository.save(User(
            firstName   = "Marcos", lastName = "Pérez",
            email       = "marcos@mail.com", username = "marquitos99",
            avatar      = "https://randomuser.me/api/portraits/men/3.jpg",
            phone       = "1144556677",
            city        = "Rosario",
            description = "Fan de la ciencia ficción",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2022, 7, 15),
            password    = hashPassword("1234"),
            role        = Role.reader
        ))

        val lucia = userRepository.save(User(
            firstName   = "Lucía", lastName = "Fernández",
            email       = "lucia@mail.com", username = "luciaf",
            avatar      = "https://randomuser.me/api/portraits/women/1.jpg",
            phone       = "1166778899",
            city        = "Córdoba",
            description = "Coleccionista y lectora",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2024, 1, 5),
            password    = hashPassword("1234"),
            role        = Role.reader_publisher
        ))

        val diego = userRepository.save(User(
            firstName   = "Diego", lastName = "Romero",
            email       = "diego@mail.com", username = "dieguitoR",
            avatar      = "https://randomuser.me/api/portraits/men/4.jpg",
            phone       = "1188990011",
            city        = "Mendoza",
            description = "Publica y lee drama",
            bibliokarmas= 0,
            createdAt   = LocalDate.of(2021, 11, 20),
            password    = hashPassword("1234"),
            role        = Role.reader_publisher
        ))

        // ============================================================
        // BOOKS
        // ============================================================

        val gonzaBook1 = bookRepository.save(CommonBook(
            title = "1984", image = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1657781256i/61439040.jpg",
            description = "Distopía clásica de George Orwell", genre = Genre.science_fiction,
            author = "George Orwell", pageCount = 328, isbn13 = "978-0452284234",
            language = Language.english, publisher = "Signet Classics",
            publicationDate = LocalDate.of(1949, 6, 8), condition = BookCondition.very_good, ownerId = gonza.id!!
        ))

        val gonzaBook2 = bookRepository.save(DedicatedBook(
            title = "To Kill a Mockingbird", image = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1553383690i/2657.jpg",
            description = "Clásico sobre justicia y prejuicio", genre = Genre.classic_literature,
            author = "Harper Lee", pageCount = 281, isbn13 = "978-0061120084",
            language = Language.english, publisher = "Harper Perennial",
            publicationDate = LocalDate.of(1960, 7, 11), condition = BookCondition.excellent, ownerId = gonza.id!!
        ))

        val gonzaBook3 = bookRepository.save(CollectibleBook(
            title = "The Catcher in the Rye", image = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1398034300i/5107.jpg",
            description = "Holden Caulfield y su mirada del mundo", genre = Genre.classic_literature,
            author = "J.D. Salinger", pageCount = 214, isbn13 = "978-0316769480",
            language = Language.english, publisher = "Little, Brown and Company",
            publicationDate = LocalDate.of(1951, 7, 16), condition = BookCondition.good, ownerId = gonza.id!!
        ))

        val elAleph = bookRepository.save(CommonBook(
            title = "El Aleph", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%2Fid%2FOIP.bJE2g7_HVFBc403Q7CiF7QHaLT%3Fpid%3DApi&f=1&ipt=09cd3386a3619b120400097da05fe7c124dbc34a4af00eb719ee30cc4c178506&ipo=images",
            description = "Cuentos fantásticos de Borges", genre = Genre.classic_literature,
            author = "Jorge Luis Borges", pageCount = 224, isbn13 = "978-0060935286",
            language = Language.spanish, publisher = "Emecé",
            publicationDate = LocalDate.of(1949, 1, 1), condition = BookCondition.good, ownerId = ana.id!!
        ))

        val cienAnios = bookRepository.save(CommonBook(
            title = "Cien años de soledad", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.k4wW1YD2XUZt5E2fLsmHMgHaJ-%3Fpid%3DApi&f=1&ipt=d26615694c9113815ea935e1f836b3bfa61e76b23bf0c18c46e32c1e1bdbaa83&ipo=images",
            description = "Saga de los Buendía", genre = Genre.drama,
            author = "Gabriel García Márquez", pageCount = 471, isbn13 = "978-0060883287",
            language = Language.spanish, publisher = "Sudamericana",
            publicationDate = LocalDate.of(1967, 5, 30), condition = BookCondition.very_good, ownerId = marcos.id!!
        ))

        val donQuijote = bookRepository.save(CommonBook(
            title = "Don Quijote de la Mancha", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.polifemo.com%2Fstatic%2Fimg%2Fportadas%2F_visd_0000JPG01EME.jpg&f=1&nofb=1&ipt=1e9a24243c853967f9350d6b9cac6f6fd5a3b8e540d75388cd3d4015b06bc9e8",
            description = "El ingenioso hidalgo", genre = Genre.classic_literature,
            author = "Miguel de Cervantes", pageCount = 863, isbn13 = "978-8437604947",
            language = Language.spanish, publisher = "Cátedra",
            publicationDate = LocalDate.of(1605, 1, 16), condition = BookCondition.regular, ownerId = lucia.id!!
        ))

        val laMetamorfosis = bookRepository.save(CommonBook(
            title = "La metamorfosis", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%2Fid%2FOIP.RSCpbSl4vIJxO74QrSfLRwHaLM%3Fpid%3DApi&f=1&ipt=5e0588206534c763eb89c55c3be10e00999aa7f39d8b67f1a12a5ae0533828b0",
            description = "Gregor Samsa despierta convertido en insecto", genre = Genre.drama,
            author = "Franz Kafka", pageCount = 96, isbn13 = "978-8420689878",
            language = Language.spanish, publisher = "Alianza",
            publicationDate = LocalDate.of(1915, 10, 15), condition = BookCondition.excellent, ownerId = diego.id!!
        ))

        val crimen = bookRepository.save(CommonBook(
            title = "Crimen y castigo", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%2Fid%2FOIP.V9OyndtvlsmlPzTNgcJUywHaKS%3Fpid%3DApi&f=1&ipt=3d2b5520daa07e7b39c7810f5244d281e6bd5d226fcb05bd4f865fd653c0425c&ipo=images",
            description = "Raskolnikov y su dilema moral", genre = Genre.drama,
            author = "Fiódor Dostoievski", pageCount = 671, isbn13 = "978-8420654531",
            language = Language.spanish, publisher = "Alianza",
            publicationDate = LocalDate.of(1866, 1, 1), condition = BookCondition.good, ownerId = ana.id!!
        ))

        val elPrincipito = bookRepository.save(CommonBook(
            title = "El Principito", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fhttp2.mlstatic.com%2FD_NQ_NP_964427-MLU53306591782_012023-O.webp&f=1&nofb=1&ipt=94adb6280478fdf6c9f5ae3a5c8ba93a5cb1534232a8876edfd7b67ffaa67c4a",
            description = "Un aviador conoce a un pequeño príncipe", genre = Genre.classic_literature,
            author = "Antoine de Saint-Exupéry", pageCount = 96, isbn13 = "978-8426132026",
            language = Language.french, publisher = "Salamandra",
            publicationDate = LocalDate.of(1943, 4, 6), condition = BookCondition.excellent, ownerId = marcos.id!!
        ))

        val orgullo = bookRepository.save(CommonBook(
            title = "Orgullo y prejuicio", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fm.media-amazon.com%2Fimages%2FI%2F81smOptGtLL.jpg&f=1&nofb=1&ipt=e084645ec5576bb97ffc01271299ab3d5657dae72bef6ecd8b53255c1626bcb1",
            description = "Elizabeth Bennet y Mr. Darcy", genre = Genre.romance,
            author = "Jane Austen", pageCount = 432, isbn13 = "978-0141439518",
            language = Language.english, publisher = "Penguin",
            publicationDate = LocalDate.of(1813, 1, 28), condition = BookCondition.very_good, ownerId = lucia.id!!
        ))

        val elHobbit = bookRepository.save(CommonBook(
            title = "El Hobbit", image = "/img/books/el-hobbit.jpg",
            description = "La aventura de Bilbo Bolsón", genre = Genre.science_fiction,
            author = "J.R.R. Tolkien", pageCount = 310, isbn13 = "978-8445071793",
            language = Language.spanish, publisher = "Minotauro",
            publicationDate = LocalDate.of(1937, 9, 21), condition = BookCondition.good, ownerId = diego.id!!
        ))

        val rayuela = bookRepository.save(DedicatedBook(
            title = "Rayuela", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%2Fid%2FOIP.Yu51gXhb5rpsx8Npu_ptAQAAAA%3Fpid%3DApi&f=1&ipt=45b67045320d54b38b8703e418c43170f20be70780776c3d53d3ab1843e2fd20&ipo=images",
            description = "Novela experimental de Cortázar", genre = Genre.classic_literature,
            author = "Julio Cortázar", pageCount = 600, isbn13 = "978-8420483204",
            language = Language.spanish, publisher = "Sudamericana",
            publicationDate = LocalDate.of(1963, 6, 28), condition = BookCondition.very_good, ownerId = ana.id!!
        ))

        val laHojarasca = bookRepository.save(DedicatedBook(
            title = "La hojarasca", image = "/img/books/la-hojarrasca.jpg",
            description = "Primera novela de García Márquez", genre = Genre.drama,
            author = "Gabriel García Márquez", pageCount = 144, isbn13 = "978-8439701654",
            language = Language.spanish, publisher = "Sudamericana",
            publicationDate = LocalDate.of(1955, 1, 1), condition = BookCondition.good, ownerId = marcos.id!!
        ))

        val cartaAMiHija = bookRepository.save(DedicatedBook(
            title = "Carta a mi hija", image = "/img/books/carta-a-mi-hija.jpg",
            description = "Maya Angelou — con dedicatoria personal", genre = Genre.self_help,
            author = "Maya Angelou", pageCount = 96, isbn13 = "978-0812979744",
            language = Language.english, publisher = "Random House",
            publicationDate = LocalDate.of(1998, 3, 1), condition = BookCondition.excellent, ownerId = lucia.id!!
        ))

        val laInsoportable = bookRepository.save(DedicatedBook(
            title = "La insoportable levedad del ser", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fhttp2.mlstatic.com%2FD_NQ_NP_955433-MLA75850492193_042024-O.webp&f=1&nofb=1&ipt=556448dea25534ce60c01fa5c579a13642930a60f548f27c49b80594f89f4211",
            description = "Dedicada a la memoria de Milan Kundera", genre = Genre.romance,
            author = "Milan Kundera", pageCount = 368, isbn13 = "978-8072604058",
            language = Language.spanish, publisher = "Tusquets",
            publicationDate = LocalDate.of(1984, 1, 1), condition = BookCondition.very_good, ownerId = diego.id!!
        ))

        val elAmor = bookRepository.save(DedicatedBook(
            title = "El amor en los tiempos del cólera", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.explicit.bing.net%2Fth%2Fid%2FOIP.ED0rPZaTjA82QPUP4PzybgAAAA%3Fpid%3DApi&f=1&ipt=64302121631f45fed61b2a264a0d161077c315d2e4f63a1547b00672afeb5eb8&ipo=images",
            description = "Dedicada a Carmen Balcells", genre = Genre.romance,
            author = "Gabriel García Márquez", pageCount = 348, isbn13 = "978-8439702002",
            language = Language.spanish, publisher = "Oveja Negra",
            publicationDate = LocalDate.of(1985, 12, 5), condition = BookCondition.good, ownerId = ana.id!!
        ))

        val ficciones = bookRepository.save(DedicatedBook(
            title = "Ficciones", image = "/img/books/ficciones.jpg",
            description = "Borges — con dedicatoria manuscrita", genre = Genre.classic_literature,
            author = "Jorge Luis Borges", pageCount = 174, isbn13 = "978-8420483143",
            language = Language.spanish, publisher = "Emecé",
            publicationDate = LocalDate.of(1944, 1, 1), condition = BookCondition.regular, ownerId = marcos.id!!
        ))

        val madameBovary = bookRepository.save(DedicatedBook(
            title = "Madame Bovary", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fm.media-amazon.com%2Fimages%2FI%2F61%2BF7ywtgpL._SL1500_.jpg&f=1&nofb=1&ipt=2df036c7e5c60db0a43680182f3a0bb882228b29421cadb76d92e2df62dafec2",
            description = "Dedicada a Marie-Anne Flaubert", genre = Genre.romance,
            author = "Gustave Flaubert", pageCount = 392, isbn13 = "978-0140449127",
            language = Language.french, publisher = "Penguin",
            publicationDate = LocalDate.of(1857, 4, 15), condition = BookCondition.good, ownerId = lucia.id!!
        ))

        val sobreElAmor = bookRepository.save(DedicatedBook(
            title = "Sobre el amor y la soledad", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fhttp2.mlstatic.com%2FD_NQ_NP_770275-MLM80821442219_112024-O.webp&f=1&nofb=1&ipt=e40ef274f537fb383233f360047777eb678575eebfe5216bb9eb5547d2f0787e",
            description = "Krishnamurti — ejemplar dedicado", genre = Genre.self_help,
            author = "Jiddu Krishnamurti", pageCount = 192, isbn13 = "978-8472453502",
            language = Language.spanish, publisher = "Kairos",
            publicationDate = LocalDate.of(1992, 1, 1), condition = BookCondition.excellent, ownerId = diego.id!!
        ))

        val ulises = bookRepository.save(CollectibleBook(
            title = "Ulises", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.ikDxmUwic8n1y2e_WFCAkgAAAA%3Fpid%3DApi&f=1&ipt=4300609f0fe8193624f96471dd71306c3e8a1c26fb4aad946605980602458a1a&ipo=images",
            description = "Edición de colección numerada — Joyce", genre = Genre.classic_literature,
            author = "James Joyce", pageCount = 1000, isbn13 = "978-0199543190",
            language = Language.english, publisher = "Oxford University Press",
            publicationDate = LocalDate.of(1922, 2, 2), condition = BookCondition.excellent, ownerId = ana.id!!
        ))

        val elNombre = bookRepository.save(CollectibleBook(
            title = "El nombre de la rosa", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fm.media-amazon.com%2Fimages%2FI%2F91Eo1T5KwzL._SL1500_.jpg&f=1&nofb=1&ipt=3f21875b52761d507fe0051aff6ef611b9b784629290c5a75be18dcb2c4c3b68",
            description = "Edición ilustrada de colección", genre = Genre.drama,
            author = "Umberto Eco", pageCount = 502, isbn13 = "978-8435014981",
            language = Language.spanish, publisher = "Lumen",
            publicationDate = LocalDate.of(1980, 1, 1), condition = BookCondition.excellent, ownerId = marcos.id!!
        ))

        val fahrenheit = bookRepository.save(CollectibleBook(
            title = "Fahrenheit 451", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fs2982.pcdn.co%2Fwp-content%2Fuploads%2F2012%2F09%2FFahrenheit-451-Simon-and-Schuster-classic-edition.jpeg.optimal.jpeg&f=1&nofb=1&ipt=b9ef4b54dcc58fae9a5366d7a50c6f29482f15f825a037d858827e5e186e7342",
            description = "Edición 70° aniversario", genre = Genre.science_fiction,
            author = "Ray Bradbury", pageCount = 256, isbn13 = "978-1451673319",
            language = Language.english, publisher = "Simon & Schuster",
            publicationDate = LocalDate.of(1953, 10, 19), condition = BookCondition.excellent, ownerId = lucia.id!!
        ))

        val neuromancer = bookRepository.save(CollectibleBook(
            title = "Neuromancer", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fsleazepaperbacks.com%2Fwp-content%2Fuploads%2F2024%2F10%2FImage_20241020_0029.jpg&f=1&nofb=1&ipt=7fd3367d6c23117de864e70bb0486563ba87565c79ed013c0fc8abf664c90421",
            description = "Edición especial numerada de colección", genre = Genre.science_fiction,
            author = "William Gibson", pageCount = 271, isbn13 = "978-0441569564",
            language = Language.english, publisher = "Ace Books",
            publicationDate = LocalDate.of(1984, 7, 1), condition = BookCondition.excellent, ownerId = diego.id!!
        ))

        val fundacion = bookRepository.save(CollectibleBook(
            title = "Fundación", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.Eq-Q-4oNy3e4zSxEwxO2DAHaLQ%3Fpid%3DApi&f=1&ipt=096953e6d29e1db228a8dc3d6873627bd320c43a9a8fe1bcecc3069cc7e85821&ipo=images",
            description = "Trilogía original — edición de colección Asimov", genre = Genre.science_fiction,
            author = "Isaac Asimov", pageCount = 244, isbn13 = "978-8445070642",
            language = Language.spanish, publisher = "Minotauro",
            publicationDate = LocalDate.of(1951, 5, 1), condition = BookCondition.excellent, ownerId = ana.id!!
        ))

        val dune = bookRepository.save(CollectibleBook(
            title = "Dune", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fimages-na.ssl-images-amazon.com%2Fimages%2FI%2FA1u%2B2fY5yTL.jpg&f=1&nofb=1&ipt=5c0acd444ff5db254fe966713baadb1becfa177c50d2d4db45b8f2f90a5c6184",
            description = "Edición ilustrada de lujo", genre = Genre.science_fiction,
            author = "Frank Herbert", pageCount = 412, isbn13 = "978-0593310324",
            language = Language.english, publisher = "Ace Books",
            publicationDate = LocalDate.of(1965, 8, 1), condition = BookCondition.excellent, ownerId = marcos.id!!
        ))

        val elDisenio = bookRepository.save(CollectibleBook(
            title = "El diseño de lo cotidiano", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fimgv2-1-f.scribdassets.com%2Fimg%2Fdocument%2F723744630%2Foriginal%2F019c055997%2F1720473213%3Fv%3D1&f=1&nofb=1&ipt=2452757eae90721140036bb893fab02e7109d2a56e4da8153b5a6b98778cea08",
            description = "Edición especial de colección — Norman", genre = Genre.design,
            author = "Donald Norman", pageCount = 368, isbn13 = "978-0465067107",
            language = Language.english, publisher = "L´Atlantique",
            publicationDate = LocalDate.of(1988, 1, 1), condition = BookCondition.very_good, ownerId = lucia.id!!
        ))

        val watchmen = bookRepository.save(CollectibleBook(
            title = "Watchmen", image = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn2.penguin.com.au%2Fcovers%2Foriginal%2F9781779527325.jpg&f=1&nofb=1&ipt=1c0dd8de0dd2939f74bffa5250d12045fcd7c134cbae7b6235d3d5101e0bf8b6",
            description = "Edición absoluta de colección", genre = Genre.science_fiction,
            author = "Alan Moore", pageCount = 416, isbn13 = "978-1401222667",
            language = Language.english, publisher = "DC Comics",
            publicationDate = LocalDate.of(1987, 9, 1), condition = BookCondition.excellent, ownerId = diego.id!!
        ))

        val allBooks: List<Book> = listOf(
            gonzaBook1, gonzaBook2, gonzaBook3, elAleph, cienAnios, donQuijote, laMetamorfosis,
            crimen, elPrincipito, orgullo, elHobbit, rayuela, laHojarasca, cartaAMiHija,
            laInsoportable, elAmor, ficciones, madameBovary, sobreElAmor, ulises, elNombre,
            fahrenheit, neuromancer, fundacion, dune, elDisenio, watchmen
        )

        // ============================================================
        // BOOKINGS
        // ============================================================

        val marcosBooking1 = bookingRepository.save(marcos.bookWithCount(elAleph,   LocalDate.of(2022, 1, 1),  LocalDate.of(2022, 1, 20)))
        val enzoBooking1   = bookingRepository.save(enzo.bookWithCount(elAleph,     LocalDate.of(2023, 1, 1),  LocalDate.of(2023, 1, 20)))
        val pedroBooking1  = bookingRepository.save(pedro.bookWithCount(elAleph,    LocalDate.of(2024, 1, 1),  LocalDate.of(2024, 1, 20)))
        bookingRepository.save(gonza.bookWithCount(elAleph,                         LocalDate.of(2025, 10, 10), LocalDate.now().plusDays(10)))

        val pedroBooking2  = bookingRepository.save(pedro.bookWithCount(ulises,     LocalDate.of(2024, 4, 10), LocalDate.of(2024, 5, 1)))
        bookingRepository.save(gonza.bookWithCount(rayuela,                         LocalDate.now().plusDays(3), LocalDate.now().plusDays(20)))
        val anaBooking1    = bookingRepository.save(ana.bookWithCount(cienAnios,    LocalDate.of(2024, 3, 1),  LocalDate.of(2024, 3, 20)))
        val marcosBooking2 = bookingRepository.save(marcos.bookWithCount(fahrenheit, LocalDate.of(2024, 1, 5),  LocalDate.of(2024, 1, 25)))
        bookingRepository.save(lucia.bookWithCount(dune,                            LocalDate.now().plusDays(1), LocalDate.now().plusDays(14)))
        val enzoBooking2   = bookingRepository.save(enzo.bookWithCount(orgullo,     LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 5)))
        val luciaBooking1  = bookingRepository.save(lucia.bookWithCount(elNombre,   LocalDate.of(2024, 5, 1),  LocalDate.of(2024, 5, 20)))
        val marcosBooking3 = bookingRepository.save(marcos.bookWithCount(neuromancer, LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 1)))
        val anaBooking2    = bookingRepository.save(ana.bookWithCount(laInsoportable, LocalDate.of(2024, 7, 1),  LocalDate.of(2024, 7, 20)))
        bookingRepository.save(gonza.bookWithCount(watchmen,                        LocalDate.now().plusDays(2), LocalDate.now().plusDays(10)))

        val gonzaBooking1 = bookingRepository.save(gonza.bookWithCount(laHojarasca, LocalDate.of(2024, 1, 1),  LocalDate.of(2024, 1, 20)))
        bookingRepository.save(gonza.bookWithCount(laHojarasca,                     LocalDate.of(2025, 1, 1),  LocalDate.of(2025, 1, 20)))
        bookingRepository.save(gonza.bookWithCount(cartaAMiHija,                    LocalDate.of(2024, 4, 10), LocalDate.of(2024, 5, 1)))
        bookingRepository.save(gonza.bookWithCount(ficciones,                       LocalDate.now().minusDays(4), LocalDate.now().plusDays(6)))
        bookingRepository.save(gonza.bookWithCount(elHobbit,                        LocalDate.now().minusDays(8), LocalDate.now().plusDays(1)))

        val pedroGonza  = bookingRepository.save(pedro.bookWithCount(gonzaBook2,    LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 15)))
        val marcosGonza = bookingRepository.save(marcos.bookWithCount(gonzaBook1,   LocalDate.of(2024, 2, 1),  LocalDate.of(2024, 2, 20)))
        val anaGonza    = bookingRepository.save(ana.bookWithCount(gonzaBook2,       LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 25)))
        bookingRepository.save(lucia.bookWithCount(gonzaBook3,                      LocalDate.now().minusDays(3), LocalDate.now().plusDays(7)))
        bookingRepository.save(diego.bookWithCount(gonzaBook1,                      LocalDate.now().minusDays(9), LocalDate.now().plusDays(2)))

        // ============================================================
        // REVIEWS
        // ============================================================

        marcosBooking1.review(5, "Una obra maestra absoluta, Borges en su máxima expresión.")
        pedroBooking2.review(4,  "Exigente como pocas, pero la recompensa es enorme.")
        enzoBooking1.review(5,   "Excelente libro, muy entretenido de principio a fin.")
        pedroBooking1.review(4,  "Muy buena lectura, la narrativa es fluida y fácil de seguir.")
        marcosBooking2.review(5, "Más vigente hoy que cuando fue escrita, imprescindible.")
        anaBooking1.review(4,    "El realismo mágico elevado a su máxima potencia.")
        luciaBooking1.review(5,  "Eco construye un thriller medieval con una erudición impresionante.")
        enzoBooking2.review(4,   "Austen es una maestra de la ironía social.")
        marcosBooking3.review(5, "Gibson inventó el futuro, esta edición numerada es un objeto de culto.")
        anaBooking2.review(4,    "Kundera mezcla filosofía y ficción de una manera magistral.")
        gonzaBooking1.review(5,  "Una obra maestra absoluta, Borges en su máxima expresión.")
        marcosGonza.review(5,    "Impactante de principio a fin, una lectura imprescindible.")
        anaGonza.review(4,       "Muy buena lectura, atrapante y muy bien escrita.")
        pedroGonza.review(4,     "Un clásico que vale la pena revisitar.")

        // ============================================================
        // CLICKS
        // ============================================================
        // Comentado para prueba limpia: sin clicks pre-sembrados, Redis arranca vacío

//        // Top 10 del ZSet — solo libros activos de publishers, garantiza ≥6 mostrables para cualquier usuario
//        gonzaBook1.click("anita",    "pedrito", "enzito", "marquitos99", "luciaf",  "dieguitoR") // 6
//        fahrenheit.click("gonza",    "anita",   "pedrito", "enzito",     "marquitos99", "dieguitoR") // 6
//        neuromancer.click("gonza",   "anita",   "pedrito", "enzito",     "marquitos99", "luciaf")    // 6
//        gonzaBook2.click("anita",    "pedrito", "enzito",  "marquitos99", "luciaf")                  // 5
//        elHobbit.click("gonza",      "anita",   "pedrito", "enzito",     "marquitos99")              // 5
//        orgullo.click("gonza",       "anita",   "pedrito", "enzito",     "marquitos99")              // 5
//        watchmen.click("gonza",      "anita",   "pedrito", "enzito",     "luciaf")                   // 5
//        gonzaBook3.click("anita",    "pedrito", "enzito",  "luciaf")                                 // 4
//        madameBovary.click("gonza",  "anita",   "pedrito", "enzito")                                 // 4
//        laInsoportable.click("gonza","anita",   "pedrito", "enzito")                                 // 4
//
//        // Resto de activos con pocos clicks — para demo completa
//        donQuijote.click("gonza",    "anita",   "pedrito")
//        laMetamorfosis.click("gonza","anita",   "pedrito")
//        cartaAMiHija.click("gonza",  "anita",   "pedrito")
//        sobreElAmor.click("gonza",   "anita",   "pedrito")
//        elDisenio.click("gonza",     "anita",   "pedrito")
//
//        // Inactivos — solo 1 click para demo
//        elAleph.click("gonza")
//        cienAnios.click("gonza")
//        rayuela.click("luciaf")
//        elNombre.click("luciaf")
//        crimen.click("gonza")
//        elPrincipito.click("gonza")
//        laHojarasca.click("gonza")
//        elAmor.click("gonza")
//        ficciones.click("gonza")
//        dune.click("gonza")
//        ulises.click("gonza")
//        fundacion.click("gonza")

        // ============================================================
        // PERSIST EMBEDDED bookedRanges + lastReviews + ranking
        // ============================================================

        val userMap = listOf(gonza, ana, pedro, enzo, marcos, lucia, diego).associateBy { it.id!! }

        allBooks.forEach { book ->
            val bookId    = book.id!!
            val avg       = reviewRepository.averageScoreByBookId(bookId)
            val latestTwo = reviewRepository.findLatestByBookId(
                bookId,
                org.springframework.data.domain.PageRequest.of(0, 2)
            )
            book.applyReviewsUpdate(latestTwo, avg)
            bookingRepository.updateBookRatingForBook(bookId, avg)

            val owner = userMap[book.ownerId]!!
            if (!owner.canPublish()) book.deactivate()
        }
        bookRepository.saveAll(allBooks)

        // Pre-populate Redis book:{id} keys so the home screen can be served from Redis on the first request
        // Comentado para pruebas: los book:{id} se cargan la primera vez que MongoDB sirve una página
//        allBooks.forEach { book ->
//            val owner        = userMap[book.ownerId]!!
//            val bookingCount = bookingRepository.countByBookIdAndCancelledFalse(book.id!!)
//            bookRankingRepository.updateBookCache(book, bookingCount, owner)
//        }

        userRepository.save(ana)
        userRepository.save(marcos)
        userRepository.save(lucia)
        userRepository.save(diego)
        userRepository.save(gonza)
        userRepository.save(pedro)
        userRepository.save(enzo)
    }
}
