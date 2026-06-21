# Endpoints REST

Salvo autenticación, Swagger y GraphiQL, todos los endpoints requieren un access token.

## Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/login` | Inicia sesión |
| POST | `/auth/register` | Registra un usuario |
| POST | `/auth/refresh` | Rota el refresh token |

## Catálogo

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/filtered-bookings` | Busca libros disponibles con filtros y paginación |
| GET | `/books/{id}` | Obtiene un libro con propietario |
| GET | `/books/{id}/detail` | Obtiene el detalle visible del libro |
| GET | `/books/{id}/reviews` | Lista reseñas |
| POST | `/books` | Publica un libro |
| PUT | `/books/{id}` | Edita un libro propio |
| DELETE | `/books/{id}` | Elimina lógicamente un libro propio |
| GET | `/books/user` | Lista los libros del usuario autenticado |
| POST | `/books/{id}/click` | Registra una visita |
| GET | `/books/click-stats` | Estadísticas del publicador autenticado |

## Reservas y reseñas

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/books/{bookId}/confirm-booking` | Confirma una reserva válida |
| GET | `/my-bookings` | Lista préstamos recibidos o realizados |
| POST | `/bookings/{bookingId}/review` | Crea o actualiza la reseña del lector |

## Perfil

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/profile` | Perfil del usuario autenticado |
| GET | `/profile/summary` | Avatar y bibliokarmas |
| PUT | `/profile` | Actualiza el perfil y roles |

## GraphQL

`POST /graphql` requiere autenticación. `GET /graphiql` ofrece una interfaz de exploración. Los contratos están en `src/main/resources/schema`.
