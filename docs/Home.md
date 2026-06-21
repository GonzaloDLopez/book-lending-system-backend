# BookLibre Backend

Esta sección documenta el diseño vigente del backend. Está orientada a comprender decisiones, responsabilidades y límites del sistema; el README contiene la guía rápida de ejecución.

## Contenido

- [Arquitectura](Arquitectura.md)
- [Modelo de dominio](Modelo-de-dominio.md)
- [Persistencia políglota](Persistencia-poliglota.md)
- [Endpoints REST](Endpoints.md)
- [Autenticación JWT](Autenticacion-JWT.md)
- [Caching y ranking con Redis](Redis.md)
- [GraphQL y KPI](GraphQL-y-KPIs.md)
- [Docker y deploy](Docker-y-deploy.md)
- [Testing](Testing.md)
- [Cluster MongoDB opcional](Cluster-MongoDB.md)

## Principios

- PostgreSQL, MongoDB y Redis tienen responsabilidades explícitas.
- Redis es una optimización reconstruible, no una fuente de verdad.
- El usuario autenticado se obtiene del JWT, no de parámetros controlados por el cliente.
- Controllers y DataFetchers adaptan protocolos; las reglas viven en servicios, validadores y dominio.
- Los trade-offs conocidos se documentan en lugar de ocultarse.
