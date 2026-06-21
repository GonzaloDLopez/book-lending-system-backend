# Cluster MongoDB opcional

El repositorio conserva en `mongo-cluster/` un entorno experimental con sharding y replica sets. No forma parte del Compose principal ni se necesita para ejecutar BookLibre.

## Topología

- Dos routers `mongos`.
- Tres config servers.
- Dos shards configurados como replica sets.
- Árbitros para mantener mayoría de votos en el entorno local.

La colección `books` puede distribuirse con una shard key hashed. Esto reparte escrituras, aunque pierde localidad para consultas por rango.

## Decisión

El volumen actual no justifica sharding: agrega infraestructura y complejidad sin una mejora medible. El entorno principal usa MongoDB simple en desarrollo y un servicio administrado con replica set en producción.

El cluster queda como demostración de distribución, no como una necesidad del producto.
