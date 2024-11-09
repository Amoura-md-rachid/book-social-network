/**
 * Méthode principale de filtrage qui est appelée à chaque requête HTTP.
 * Elle vérifie la présence et la validité du token JWT dans les en-têtes de la requête.
 * <p>
 * Fonctionnement détaillé :
 * 1. **Exclusion des routes d'authentification** :
 *    - Si la requête est destinée à une route d'authentification (par exemple, `/api/v1/auth`),
 *      le filtre ignore la validation du token JWT et passe la requête au filtre suivant dans la chaîne.
 * <p>
 * 2. **Récupération de l'en-tête d'autorisation** :
 *    - Le filtre récupère l'en-tête `Authorization` de la requête HTTP. Cet en-tête doit commencer par "Bearer ",
 *      indiquant qu'il contient un token JWT.
 *    - Si l'en-tête est absent ou ne commence pas par "Bearer ", le filtre passe la requête au filtre suivant sans effectuer de validation.
 * <p>
 * 3. **Extraction et validation du token JWT** :
 *    - Le filtre extrait le token JWT en retirant le préfixe "Bearer ".
 *    - Ensuite, il utilise le service `JwtService` pour extraire l'email de l'utilisateur (username) à partir du token JWT.
 * <p>
 * 4. **Vérification de l'authentification dans le contexte de sécurité** :
 *    - Si l'email de l'utilisateur est extrait avec succès et que le contexte de sécurité (`SecurityContextHolder`)
 *      n'a pas encore d'authentification définie, le filtre poursuit la validation.
 *    - Le filtre charge les détails de l'utilisateur à partir du service `UserDetailsService`
 *      en utilisant l'email extrait du token.
 * <p>
 * 5. **Création d'un objet d'authentification** :
 *    - Si le token JWT est valide pour l'utilisateur récupéré, un objet `UsernamePasswordAuthenticationToken`
 *      est créé avec les informations de l'utilisateur.
 *    - Le filtre définit également les détails d'authentification à partir de la requête HTTP via
 *      `WebAuthenticationDetailsSource().buildDetails(request)`.
 *    - Le contexte de sécurité est mis à jour avec cet objet d'authentification, ce qui permet à l'utilisateur
 *      d'être authentifié pour la requête en cours.
 * <p>
 * 6. **Poursuite de la chaîne de filtres** :
 *    - Indépendamment du résultat de la validation (succès ou absence de validation),
 *      le filtre passe la requête et la réponse au filtre suivant dans la chaîne.
 *
 * @param request     l'objet HttpServletRequest représentant la requête HTTP
 * @param response    l'objet HttpServletResponse représentant la réponse HTTP
 * @param filterChain la chaîne de filtres permettant de poursuivre le traitement de la requête
 * @throws ServletException en cas d'erreur liée au traitement des filtres
 * @throws IOException      en cas d'erreur d'entrée/sortie
 */

package com.amoura.book.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // Service pour manipuler les tokens JWT (extraction, validation, etc.)
    private final JwtService jwtService;

    private UserDetailsService userDetailsService;

    /**
     * Méthode principale de filtrage appelée à chaque requête HTTP.
     * Cette méthode valide les tokens JWT présents dans les en-têtes de requête HTTP pour authentifier les utilisateurs.
     * <p>
     * **Fonctionnalité** :
     * - Vérifie la présence et la validité du token JWT dans l'en-tête `Authorization` de la requête.
     * - Si le token est valide, l'utilisateur est authentifié et les informations d'authentification sont stockées dans le `SecurityContext`.
     * - Si le token est invalide ou absent, la requête est simplement passée au filtre suivant.
     *
     * **Paramètres** :
     * @param request     l'objet {@link HttpServletRequest} représentant la requête HTTP.
     * @param response    l'objet {@link HttpServletResponse} représentant la réponse HTTP.
     * @param filterChain l'objet {@link FilterChain} permettant de poursuivre le traitement de la requête dans la chaîne de filtres.
     *
     * **Exceptions** :
     * @throws ServletException si une erreur survient lors du traitement du filtre.
     * @throws IOException      si une erreur d'entrée/sortie se produit.
     *
     * **Étapes du filtrage** :
     * 1. **Vérification de la route d'authentification** :
     *    - Si la requête cible une route d'authentification (`/api/v1/auth`), le filtre ignore la validation du token JWT et passe directement au filtre suivant.
     *
     * 2. **Récupération de l'en-tête d'autorisation** :
     *    - L'en-tête `Authorization` est récupéré depuis la requête.
     *    - Si l'en-tête est absent ou ne commence pas par "Bearer ", la requête est passée au filtre suivant sans validation.
     *
     * 3. **Extraction et validation du token JWT** :
     *    - Le token JWT est extrait en supprimant le préfixe "Bearer ".
     *    - Le service `JwtService` est utilisé pour extraire l'email de l'utilisateur à partir du token.
     *
     * 4. **Vérification du contexte de sécurité** :
     *    - Si l'email de l'utilisateur est extrait avec succès et qu'aucune authentification n'est présente dans le `SecurityContext`,
     *      les détails de l'utilisateur sont récupérés à l'aide du `UserDetailsService`.
     *
     * 5. **Validation du token JWT** :
     *    - Le token est vérifié via le service `JwtService`.
     *    - Si le token est valide, un objet `UsernamePasswordAuthenticationToken` est créé et enregistré dans le `SecurityContext`.
     *      Cela permet de marquer l'utilisateur comme authentifié pour cette requête.
     *
     * 6. **Continuer la chaîne de filtres** :
     *    - La requête et la réponse sont passées au filtre suivant dans la chaîne, indépendamment du résultat de la validation.
     *
     * **Remarques** :
     * - L'utilisation de `OncePerRequestFilter` garantit que cette méthode est appelée une seule fois par requête HTTP.
     * - `SecurityContextHolder` permet de stocker les informations d'authentification de l'utilisateur et de les rendre accessibles dans l'application Spring Security.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Vérifie si la requête est destinée à une route d'authentification (exclue du filtrage JWT)
        if (request.getServletPath().contains("/api/v1/auth")) {
            // Passe le contrôle au filtre suivant dans la chaîne sans valider le token
            filterChain.doFilter(request, response);
            return;
        }

        // Récupère l'en-tête d'autorisation (Authorization) de la requête
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt; // Token JWT extrait de l'en-tête
        final String userEmail; // Email de l'utilisateur extrait du token JWT

        // Vérifie si l'en-tête d'autorisation est présent et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si l'en-tête est absent ou mal formé, passe le contrôle au filtre suivant sans valider
            filterChain.doFilter(request, response);
            return;
        }

        // Extraction du token JWT à partir de l'en-tête (en supprimant le préfixe "Bearer ")
        jwt = authHeader.substring(7);

        // Extraction de l'email de l'utilisateur à partir du token JWT
        userEmail = jwtService.extractUsername(jwt);

        // Vérifie si l'email de l'utilisateur est présent (extrait du token)
        // et si aucune authentification n'est déjà établie dans le SecurityContext
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Charge les détails de l'utilisateur à partir du UserDetailsService en utilisant l'email
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Vérifie si le token JWT est valide pour les détails de l'utilisateur récupérés
            if(jwtService.isTokenValid(jwt, userDetails)) {
                // Crée un objet UsernamePasswordAuthenticationToken pour représenter l'authentification de l'utilisateur
                // userDetails : Contient les informations de l'utilisateur (nom, email, etc.)
                // null : Le mot de passe n'est pas requis, car la validation est déjà effectuée via le token JWT
                // userDetails.getAuthorities() : Les rôles et permissions de l'utilisateur
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                // Ajoute des détails supplémentaires d'authentification à partir de la requête HTTP
                // `WebAuthenticationDetailsSource` permet d'inclure des informations supplémentaires comme l'adresse IP
                authenticationToken.setDetails(new WebAuthenticationDetailsSource());
                // Enregistre l'authentification dans le SecurityContext
                // Cela signifie que l'utilisateur est maintenant authentifié pour cette requête
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
