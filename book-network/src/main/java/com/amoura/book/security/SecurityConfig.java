/**
 * Classe de configuration de la sécurité pour l'application Spring Boot.
 * Cette classe configure la chaîne de filtres de sécurité pour gérer l'authentification
 * et l'autorisation des requêtes HTTP via Spring Security.
 * <p>
 * Annotations utilisées :
 * <p>
 * - @Configuration : Indique que cette classe définit des beans de configuration Spring.
 * - @EnableWebSecurity : Active les fonctionnalités de sécurité web de Spring Security.
 * - @EnableMethodSecurity : Permet d'activer la sécurité au niveau des méthodes, y compris
 *   la sécurité basée sur les annotations (@Secured, @PreAuthorize, etc.).
 *   Ici, `securedEnabled = true` permet d'utiliser l'annotation @Secured.
 * - @AllArgsConstructor : Génère un constructeur avec tous les attributs de la classe (injectés via Lombok).
 */
package com.amoura.book.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    // Filtre d'authentification JWT personnalisé pour intercepter les requêtes HTTP et valider les tokens JWT
    private JwtFilter jwtAuthFilter;

    // Fournisseur d'authentification personnalisé pour la gestion de l'authentification des utilisateurs
    private final AuthenticationProvider authenticationProvider;

    /**
     * Bean de configuration de la chaîne de filtres de sécurité.
     * Définit la gestion des sessions, les filtres de sécurité et les règles d'autorisation pour les requêtes HTTP.
     *
     * @param http l'objet HttpSecurity utilisé pour configurer la sécurité web
     * @return une instance de SecurityFilterChain avec la configuration de sécurité appliquée
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Active le support CORS (Cross-Origin Resource Sharing) avec la configuration par défaut.
                // CORS permet aux navigateurs de réaliser des requêtes vers des domaines différents.
                .cors(withDefaults())

                // Désactive la protection CSRF (Cross-Site Request Forgery).
                // Cette protection est désactivée car l'application utilise des tokens JWT (sans état).
                .csrf(AbstractHttpConfigurer::disable)

                // Configuration des autorisations pour différentes requêtes HTTP
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                        "/auth/**",                // Routes publiques pour l'authentification
                                        "/v2/api-docs",            // Documentation de l'API version 2
                                        "/v3/api-docs",            // Documentation de l'API version 3
                                        "/v3/api-docs/**",         // Routes sous-jacentes de l'API version 3
                                        "/swagger-resources",      // Ressources Swagger
                                        "/swagger-resources/**",   // Sous-routes des ressources Swagger
                                        "/configuration/ui",       // Configuration de l'interface utilisateur Swagger
                                        "/configuration/security", // Configuration de la sécurité Swagger
                                        "/swagger-ui/**",          // Interface utilisateur Swagger
                                        "/webjars/**",             // Fichiers statiques utilisés par Swagger
                                        "/swagger-ui.html"         // Page principale de Swagger UI
                                ).permitAll() // Autorise l'accès public (sans authentification) à ces routes

                                // Toutes les autres requêtes doivent être authentifiées
                                .anyRequest().authenticated()
                )

                // Gestion de la session pour l'application
                .sessionManagement(session ->
                        // Utilise une politique de gestion des sessions sans état (STATELESS),
                        // idéale pour les API REST utilisant des tokens JWT.
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Définition du fournisseur d'authentification personnalisé
                .authenticationProvider(authenticationProvider)

                // Ajoute le filtre d'authentification JWT avant le filtre d'authentification UsernamePassword
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Construit et retourne l'instance de SecurityFilterChain avec la configuration définie
        return http.build();
    }

}
