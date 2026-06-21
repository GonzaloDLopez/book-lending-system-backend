# Caching y ranking con Redis

La clave `ranking` es un Sorted Set: el miembro es el identificador del libro y el score, su cantidad total de clics.

Cada visita se persiste primero en MongoDB y luego incrementa Redis. La clave `ranking:initialized` distingue un ranking completo de uno parcial. Cuando falta, el backend reconstruye el conjunto desde MongoDB. Una tarea periódica también reconcilia eventos perdidos durante caídas.

Las claves `book:{id}` guardan el DTO necesario para Home con TTL de una hora. Se invalidan cuando cambia el libro.

El backend consulta MongoDB cuando hay filtros no soportados, faltan libros o DTO, Redis está caído o se solicita otra página. Un cache miss agrega latencia, pero no cambia el resultado funcional.
