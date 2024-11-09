/**
 * Service de gestion des JSON Web Tokens (JWT).
 * <p>
 * Ce service contient les méthodes nécessaires pour générer, signer et valider les tokens JWT
 * utilisés dans l'application pour l'authentification des utilisateurs.
 * <p>
 * Annotations :
 * - @Service : Indique que cette classe est un composant Spring de type service, géré par le conteneur Spring.
 */
package com.amoura.book.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Durée de validité du token JWT (en millisecondes), injectée depuis les propriétés de configuration
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // Clé secrète utilisée pour signer les tokens, injectée depuis les propriétés de configuration
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Génère un token JWT sans claims supplémentaires.
     *
     * @param userDetails Les détails de l'utilisateur pour lesquels le token est généré.
     * @return Le token JWT généré.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token JWT avec des claims supplémentaires personnalisés.
     *
     * @param extraclaims Map contenant les claims supplémentaires à inclure dans le token.
     * @param userDetails Les détails de l'utilisateur pour lesquels le token est généré.
     * @return Le token JWT généré.
     */
    public String generateToken(
            Map<String, Object> extraclaims,
            UserDetails userDetails
    ) {
        // Extraction des rôles de l'utilisateur
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Construction et génération du token JWT
        return Jwts.builder()
                .setClaims(extraclaims)                                          // Ajout des claims personnalisés
                .setSubject(userDetails.getUsername())                          // Ajout du sujet (username)
                .setIssuedAt(new Date(System.currentTimeMillis()))              // Date d'émission
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Date d'expiration
                .claim("authorities", authorities)                              // Ajout des rôles de l'utilisateur
                .signWith(getSigningKey())                                      // Signature du token avec la clé secrète
                .compact();                                                     // Génération du token final
    }

    /**
     * Récupère la clé de signature utilisée pour signer le token JWT.
     *
     * @return La clé de signature HMAC.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);   // Décodage de la clé secrète en Base64
        return Keys.hmacShaKeyFor(keyBytes);                   // Création de la clé HMAC
    }

    /**
     * Vérifie si un token est valide pour un utilisateur donné.
     *
     * @param token Le token JWT à vérifier.
     * @param userDetails Les détails de l'utilisateur.
     * @return true si le token est valide, false sinon.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Extraction du nom d'utilisateur du token
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Vérifie si le token JWT est expiré.
     *
     * @param token Le token JWT à vérifier.
     * @return true si le token est expiré, false sinon.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token Le token JWT.
     * @return La date d'expiration du token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait le nom d'utilisateur (username) du token JWT.
     *
     * @param token Le token JWT.
     * @return Le nom d'utilisateur contenu dans le token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Méthode générique pour extraire une claim spécifique du token JWT.
     *
     * @param token Le token JWT.
     * @param claimsResolver Fonction de résolution de claim.
     * @param <T> Le type de la claim extraite.
     * @return La valeur de la claim extraite.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait toutes les claims du token JWT.
     *
     * @param token Le token JWT.
     * @return Les claims extraites du token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Définition de la clé de signature
                .build()
                .parseClaimsJws(token)
                .getBody();  // Récupération du corps des claims
    }
}
