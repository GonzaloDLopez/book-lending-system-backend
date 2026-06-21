# GraphQL y KPI

Netflix DGS expone el dashboard en `POST /graphql`. Cada métrica tiene schema, DataFetcher y servicio.

- `kpiTasaConversion`: relaciona clics y reservas.
- `kpiLeaderboardBibliokarmas`: top cacheado en Redis.
- `kpiPromedioCalificacionesPorTipo`: promedio por subtipo.
- `kpiCatalogHealth`: partición disjunta del catálogo.
- `recentActivityFeed`: altas y reservas recientes.

El frontend solicita todo en una operación autenticada y controla errores HTTP y GraphQL.

## Salud del catálogo

La prioridad es: reserva vigente, sin reservas, alguna finalizada y finalmente sólo futuras. Por eso los grupos no se superponen y siempre suman el total.

## Decisión

GraphQL no era indispensable: REST ya resolvía el producto. Se incorporó para experimentar schemas, resolvers y unión de fuentes, sin migrar artificialmente operaciones transaccionales.
