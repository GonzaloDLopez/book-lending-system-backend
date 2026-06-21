# Modelo de dominio

## Book

Documento MongoDB abstracto con tres subtipos: `CommonBook`, `DedicatedBook` y `CollectibleBook`. Cada subtipo calcula de forma distinta el bonus de bibliokarmas.

El libro conserva `ownerId`, estado de activación, baja lógica, promedio de reseñas, últimas reseñas y rangos reservados. `isAvailable(from, to)` determina si un rango se superpone con alguna reserva.

## Booking

Entidad PostgreSQL que representa una reserva y conserva una fotografía mínima del libro: identificador, propietario, título, autor e imagen. Esto permite listar el historial aunque el catálogo cambie.

Calcula duración y bibliokarmas, distingue reservas activas, próximas a vencer, devueltas y canceladas.

## Review

Entidad PostgreSQL única por lector y libro a nivel de servicio. Acepta puntuaciones de 1 a 5 y comentarios no vacíos. Sólo el lector de la última reserva devuelta puede crearla o editarla.

## User

Entidad PostgreSQL con datos personales, credenciales Argon2, bibliokarmas y rol:

- `reader`: puede reservar.
- `publisher`: puede publicar.
- `reader_publisher`: puede realizar ambas acciones.

## Datos derivados

MongoDB mantiene promedio y últimas reseñas para responder el detalle sin consultar todo PostgreSQL. `bookedRanges` acelera disponibilidad. Estas proyecciones no sustituyen las reservas y reseñas originales.
