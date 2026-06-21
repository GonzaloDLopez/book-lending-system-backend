# Testing

```bash
./gradlew clean test
```

JaCoCo genera reportes HTML, XML y CSV. GitHub Actions ejecuta la suite desde una instalación limpia.

La cobertura incluye dominio, servicios, controllers, autenticación, autorización de reseñas, reservas inválidas, Redis, GraphQL y KPI. Se priorizan reglas con impacto en seguridad y consistencia, no sólo el porcentaje total.
