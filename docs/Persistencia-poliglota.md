# Persistencia políglota

BookLibre utiliza cada motor para el tipo de información que mejor representa.

## PostgreSQL

Usuarios y roles, reservas, reseñas y refresh tokens revocables.

## MongoDB

Catálogo de libros, rangos de reserva embebidos, datos derivados de reseñas y eventos de clic. Los documentos guardan `ownerId`, no una copia completa del usuario.

## Redis

Sorted Set global de popularidad, DTO mínimos de libros con TTL y leaderboard de bibliokarmas. Redis puede borrarse sin perder información definitiva.

## Consistencia

La reserva oficial vive en PostgreSQL. El rango de MongoDB optimiza disponibilidad. No hay una transacción ACID conjunta; una evolución productiva debería incorporar outbox y reconciliación asíncrona.
