# Autenticación JWT

1. Login valida la contraseña con Argon2.
2. Se entrega un access token corto y un refresh token aleatorio.
3. PostgreSQL guarda sólo el hash del refresh token.
4. Cada renovación revoca el token anterior.
5. Una expiración absoluta obliga a iniciar sesión nuevamente.

El filtro verifica firma, expiración, formato y existencia del usuario. Tokens vencidos o corruptos responden `401`.

Los controllers obtienen el usuario desde `Authentication`; propiedad y roles se verifican en backend. `JWT_SECRET_KEY` se inyecta por ambiente y nunca debe usar el valor local en producción.
