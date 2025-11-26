package com.stylisten.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                //endpoints publicos
                .requestMatchers(
                    "/api/v1/health",
                    "/api/v1/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                //precisam autenticar
                .requestMatchers("/api/v1/stylisten/**").authenticated()
                //so admin
                .requestMatchers("/api/v1/styles/**").hasRole("ADMIN")
                //td o resto precisa autenticar
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://app.stylisten.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// ========== JWT Configuration ==========
@Configuration
class JwtConfig {
    
    @Bean
    public io.jsonwebtoken.security.Keys jwtSigningKey(
        @org.springframework.beans.factory.annotation.Value("${jwt.secret}") String secret
    ) {
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes());
    }
}

// ========== WebClient Configuration ==========
@Configuration
class WebClientConfig {

    @Bean
    public org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder() {
        return org.springframework.web.reactive.function.client.WebClient.builder()
            .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, 
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
    }
}

// ========== Redis Configuration ==========
@Configuration
@org.springframework.cache.annotation.EnableCaching
class RedisConfig {

    @Bean
    public org.springframework.data.redis.cache.RedisCacheManager cacheManager(
        org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory
    ) {
        org.springframework.data.redis.cache.RedisCacheConfiguration config = 
            org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(java.time.Duration.ofHours(6))
                .disableCachingNullValues();

        return org.springframework.data.redis.cache.RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }

    @Bean
    public org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate(
        org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory
    ) {
        org.springframework.data.redis.core.RedisTemplate<String, Object> template = 
            new org.springframework.data.redis.core.RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());
        return template;
    }
}

// ========== OpenAPI Configuration ==========
@Configuration
class OpenApiConfig {

    @Bean
    public io.swagger.v3.oas.models.OpenAPI customOpenAPI() {
        return new io.swagger.v3.oas.models.OpenAPI()
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("Stylisten API")
                .version("1.0")
                .description("API para correlacionar hábitos musicais com estilos de moda")
                .contact(new io.swagger.v3.oas.models.info.Contact()
                    .name("Stylisten Team")
                    .email("contato@stylisten.com")))
            .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                .addList("Bearer Authentication"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("Bearer Authentication",
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}