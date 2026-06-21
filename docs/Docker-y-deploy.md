# Docker y deploy

El entorno principal usa PostgreSQL, MongoDB simple y Redis. Los health checks evitan iniciar el backend antes que los motores.

```bash
docker compose up --build
docker compose down
```

## Perfiles

- `local`: servicios en localhost.
- `docker`: nombres internos `postgres`, `mongo` y `redis`.
- `prod`: URLs y credenciales por variables de entorno.

El despliegue probado separa frontend y backend y utiliza servicios administrados. MongoDB Atlas aporta réplica administrada; el sharding local se conserva como ejemplo opcional, pero no se justifica para este volumen.

`.env.example` enumera las variables requeridas sin publicar secretos.
