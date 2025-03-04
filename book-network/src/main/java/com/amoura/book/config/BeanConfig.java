/**
 * Configuration des beans pour la gestion de l'authentification et de l'encodage des mots de passe.
 *
 * Cette classe configure les composants nécessaires pour la sécurité de l'application en utilisant Spring Security,
 * notamment le fournisseur d'authentification (`AuthenticationProvider`), l'encodeur de mots de passe (`PasswordEncoder`)
 * et le gestionnaire d'authentification (`AuthenticationManager`).
 *
 * Les beans définis dans cette classe sont utilisés pour :
 * - Fournir un service d'authentification basé sur les données de la base de données (`DaoAuthenticationProvider`).
 * - Encoder les mots de passe des utilisateurs avec un algorithme de hachage sécurisé (`BCryptPasswordEncoder`).
 * - Gérer le processus d'authentification des utilisateurs (`AuthenticationManager`).
 *
 * Annotations :
 * - @Configuration : Indique que cette classe contient des méthodes de configuration de beans Spring.
 * - @AllArgsConstructor : Génère un constructeur avec tous les attributs de la classe grâce à Lombok, facilitant l'injection des dépendances.
 *
 * L'utilisation de ces configurations garantit une approche sécurisée et extensible pour la gestion
 * de l'authentification dans l'application.
 */

package com.amoura.book.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

    /**
     * Bean pour le gestionnaire d'authentification (AuthenticationManager).
     *
     * L'`AuthenticationManager` est utilisé pour traiter l'authentification des utilisateurs
     * en vérifiant les identifiants fournis. Il délègue l'authentification aux
     * `AuthenticationProvider` configurés, tels que le `DaoAuthenticationProvider` dans le cas
     * d'une authentification basée sur les identifiants en base de données.
     *
     * @param config l'instance `AuthenticationConfiguration` qui fournit le `AuthenticationManager`
     * @return une instance de `AuthenticationManager`
     * @throws Exception en cas d'erreur lors de l'obtention du `AuthenticationManager`
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Retourne le gestionnaire d'authentification configuré
        return config.getAuthenticationManager();
    }

    /**
     * Bean pour l'audit des entités (`AuditorAware<Integer>`).
     *
     * Ce bean est utilisé pour fournir une instance de `AuditorAware` à Spring Data JPA,
     * permettant de suivre l'utilisateur actuel pour les opérations d'audit sur les entités.
     * Les annotations d'audit telles que `@CreatedBy` et `@LastModifiedBy` utiliseront ce bean
     * pour remplir automatiquement les informations sur l'utilisateur responsable des modifications.
     *
     * ### Fonctionnement :
     * - Lors de l'enregistrement ou de la mise à jour d'une entité, Spring Data JPA interroge
     *   ce bean pour obtenir l'ID de l'utilisateur actuellement connecté.
     * - Si un utilisateur est authentifié, son ID est retourné et stocké dans les champs `createdBy`
     *   ou `lastModifiedBy` des entités.
     * - Si aucun utilisateur n'est authentifié, `Optional.empty()` est retourné, et aucun ID n'est défini.
     *
     * @return une instance de `AuditorAware<Integer>`, implémentée par la classe `ApplicationAuditAware`.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        // Retourne une nouvelle instance de `ApplicationAuditAware` pour gérer l'audit des entités
        return new ApplicationAuditAware();
    }

}
