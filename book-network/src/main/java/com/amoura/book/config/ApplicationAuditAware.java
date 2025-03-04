package com.amoura.book.config;

import com.amoura.book.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Classe d'implémentation de {@link AuditorAware} qui permet d'obtenir l'ID de l'utilisateur actuellement connecté
 * pour les opérations d'audit (création et modification des entités).
 */
public class ApplicationAuditAware implements AuditorAware<String> {

    /**
     * Cette méthode permet de récupérer l'ID de l'utilisateur actuellement authentifié.
     *
     * @return Un {@link Optional} contenant l'ID de l'utilisateur connecté en tant que {@code String},
     * ou {@code Optional.empty()} si l'utilisateur n'est pas authentifié.
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        // Récupération de l'authentification actuelle depuis le SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérification si l'utilisateur est authentifié et n'est pas anonyme
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // Récupération de l'objet User depuis le contexte de sécurité
        User userPrincipal = (User) authentication.getPrincipal();

        //  Convertir l'ID en String avant de le retourner
        return Optional.ofNullable(userPrincipal.getId())
                .map(String::valueOf);
    }
}
