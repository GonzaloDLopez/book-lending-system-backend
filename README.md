# BookLibre Backend

API de una plataforma comunitaria para publicar, descubrir y reservar libros entre usuarios. El backend combina persistencia relacional y documental, autenticación JWT, caché distribuida y un dashboard GraphQL.

## Funcionalidades

- Registro, login y renovación de sesión con JWT.
- Perfiles con roles de lector y publicador.
- Alta, edición, baja lógica y búsqueda de libros.
- Reservas con control de disponibilidad y concurrencia por libro.
- Reseñas de préstamos finalizados.
- Ranking de popularidad y caché de Home en Redis.
- Dashboard de estadísticas mediante GraphQL.

## Tecnologías

- Kotlin 2.3 y Java 21
- Spring Boot 3.5
- Spring Security y JWT
- PostgreSQL con Spring Data JPA
- MongoDB con Spring Data MongoDB
- Redis
- Netflix DGS / GraphQL
- Gradle, JUnit 5, Kotest, MockK y JaCoCo

## Arquitectura

```text
HTTP / GraphQL
      |
Controllers / DataFetchers
      |
Services y Validators
      |
PostgreSQL | MongoDB | Redis
```

PostgreSQL conserva usuarios, reservas, reseñas y refresh tokens. MongoDB almacena el catálogo y el historial de clics. Redis acelera la portada por popularidad y el leaderboard; nunca es la fuente de verdad.

## Ejecución local

Requisitos: JDK 21, PostgreSQL, MongoDB y Redis.

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080`. Swagger se publica en `/swagger-ui/index.html` y GraphiQL en `/graphiql`.

Para levantar el sistema completo, cloná el frontend como carpeta hermana con el nombre
`appointment-booking-system-frontend` y ejecutá el Compose incluido en este repositorio:

```bash
docker compose up --build
```

El backend queda expuesto en `http://localhost:9000`.

## Configuración

Los perfiles disponibles son `local`, `docker` y `prod`. Las variables necesarias para producción están documentadas en [.env.example](.env.example).

Nunca deben versionarse credenciales reales. En particular, `JWT_SECRET_KEY` debe contener una clave aleatoria de al menos 32 caracteres.

## API

Los casos de uso transaccionales se exponen mediante REST. El dashboard usa `POST /graphql` para consultar en una sola operación:

- Tasa de conversión de libros populares.
- Leaderboard de bibliokarmas.
- Promedio de calificaciones por tipo de libro.
- Salud del catálogo.
- Actividad reciente.

## Redis

El ranking usa un Sorted Set cuyo score es la cantidad de clics. MongoDB conserva cada clic y permite reconstruir el ranking. Una tarea periódica reconcilia Redis con MongoDB para recuperar eventos perdidos durante una caída.

Los datos mínimos de los libros se guardan con TTL. Si faltan datos, hay filtros complejos, Redis no está disponible o no alcanza para completar la primera página, el Home consulta MongoDB.

## Calidad

```bash
./gradlew clean test
```

El workflow de GitHub ejecuta la suite y genera un reporte JaCoCo. Las pruebas incluyen dominio, servicios, controllers, Redis, GraphQL y reglas de autorización.

## Decisiones y límites

- GraphQL se incorporó como experiencia académica; REST habría sido suficiente para el alcance actual.
- MongoDB y PostgreSQL no comparten una transacción distribuida. PostgreSQL serializa las reservas por libro y MongoDB mantiene una proyección de disponibilidad.
- Redis es descartable y reconstruible; una falla degrada rendimiento, no disponibilidad funcional.
- El entorno principal usa una instancia simple de MongoDB. La arquitectura académica con sharding se documenta en `docs/Cluster-MongoDB.md`, pero no forma parte del despliegue del portfolio.

## Equipo

Proyecto desarrollado por Rodrigo Casco, Gonzalo Lopez, Nahuel García, Santiago Zolla y Jonathan Gomez Ciranna para Persistencia de la Información, UNSAM.

La documentación técnica ampliada se encuentra en [`docs/`](docs/Home.md).

El cliente web está disponible en [appointment-booking-system-frontend](https://github.com/GonzaloDLopez/appointment-booking-system-frontend).
