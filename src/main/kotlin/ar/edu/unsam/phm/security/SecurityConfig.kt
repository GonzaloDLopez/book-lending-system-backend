package ar.edu.unsam.phm.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    lateinit var jwtAuthorizationFilter: JWTAuthorizationFilter

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .cors(Customizer.withDefaults())
            .csrf { it.disable() }
            .authorizeHttpRequests {
                // Endpoints públicos
                it.requestMatchers("/auth/login").permitAll()
                it.requestMatchers("/auth/register").permitAll()
                it.requestMatchers("/auth/refresh").permitAll()
                it.requestMatchers("/error").permitAll()
                it.requestMatchers("/swagger-ui/**").permitAll()
                it.requestMatchers("/v3/api-docs/**").permitAll()
                it.requestMatchers(HttpMethod.OPTIONS).permitAll()
                // GraphQL — endpoint de queries y UI de exploración
                it.requestMatchers("/graphiql", "/graphiql/**").permitAll()
                it.requestMatchers("/graphql").authenticated()

                // Solo lectores pueden reservar 
                it.requestMatchers(HttpMethod.POST, "/books/*/confirm-booking").hasAnyAuthority("reader", "reader_publisher")
                //it.requestMatchers(HttpMethod.POST, "/filtered-bookings/**").hasAnyAuthority("reader", "reader_publisher")

                // Solo el dueño del recurso puede ver sus reservas
                it.requestMatchers(HttpMethod.GET, "/users/*/my-bookings").authenticated()

                // Gestión de libros: cualquier usuario autenticado
                it.requestMatchers(HttpMethod.POST, "/books").authenticated()
                it.requestMatchers(HttpMethod.PUT, "/books/**").authenticated()
                it.requestMatchers(HttpMethod.DELETE, "/books/**").authenticated()
                it.requestMatchers(HttpMethod.GET, "/books/user").authenticated()
                it.requestMatchers(HttpMethod.GET, "/books/*").authenticated()

                // Cualquier autenticado
                it.anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling(Customizer.withDefaults())
            .build()
    }

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager =
        configuration.authenticationManager

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:5173",
                    "https://booklibre.onrender.com")
                    .allowedHeaders("*")
                    .allowedMethods("POST", "GET", "PUT", "DELETE")
                    .allowCredentials(true)
                    .exposedHeaders("WWW-Authenticate")
            }
        }
    }
}
