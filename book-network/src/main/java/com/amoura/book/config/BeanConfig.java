/**
 * Configuration des beans pour la gestion de l'authentification et de l'encodage des mots de passe.
 *
 * Cette classe configure les composants nécessaires pour l'authentification des utilisateurs
 * et l'encodage de leurs mots de passe dans l'application Spring Security.
 *
 * Annotations :
 * - @Configuration : Indique que cette classe contient des méthodes de configuration de beans Spring.
 * - @AllArgsConstructor : Génère un constructeur avec tous les attributs de la classe grâce à Lombok.
 */
package com.amoura.book.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@AllArgsConstructor
public class BeanConfig {

    // Service pour charger les informations des utilisateurs
    private final UserDetailsService userDetailsService;

    /**
     * Bean pour le fournisseur d'authentification (AuthenticationProvider).
     *
     * Ce bean configure le `DaoAuthenticationProvider` utilisé par Spring Security
     * pour authentifier les utilisateurs en utilisant une source de données personnalisée (DAO).
     *
     * @return AuthenticationProvider configuré pour utiliser le service UserDetailsService et l'encodeur de mot de passe
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Création d'un DaoAuthenticationProvider
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Définition du UserDetailsService utilisé pour charger les informations de l'utilisateur
        authProvider.setUserDetailsService(userDetailsService);

        // Définition de l'encodeur de mot de passe utilisé pour vérifier le mot de passe
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Bean pour l'encodeur de mots de passe (PasswordEncoder).
     *
     * Ce bean utilise l'encodeur `BCryptPasswordEncoder` pour hacher les mots de passe
     * des utilisateurs avant de les stocker en base de données et pour vérifier les mots de passe
     * lors de l'authentification.
     *
     * BCrypt est un algorithme de hachage sécurisé qui introduit un "sel" pour se protéger
     * contre les attaques par force brute et les attaques par dictionnaire.
     *
     * @return une instance de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Retourne un encodeur de mots de passe basé sur BCrypt
        return new BCryptPasswordEncoder();
    }
}
