/**
 * Implémentation du service `UserDetailsService` de Spring Security pour
 * charger les détails d'un utilisateur à partir de son email.
 *
 * Cette classe est utilisée par Spring Security pour récupérer les informations
 * de l'utilisateur afin d'authentifier et autoriser les requêtes.
 *
 * Annotations utilisées :
 * - @Service : Indique que cette classe est un composant Spring injectable et gérée par le conteneur Spring.
 * - @AllArgsConstructor : Génère un constructeur avec tous les attributs de la classe grâce à Lombok.
 * - @Transactional : Assure que la méthode `loadUserByUsername` est exécutée dans une transaction,
 *   garantissant ainsi la cohérence lors des opérations avec la base de données.
 */
package com.amoura.book.security;

import com.amoura.book.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // Référentiel d'utilisateur pour effectuer les opérations de recherche dans la base de données
    private final UserRepository userRepository;

    /**
     * Charge les détails de l'utilisateur à partir de son email.
     * Cette méthode est utilisée par Spring Security lors de l'authentification pour récupérer
     * l'utilisateur et vérifier ses informations (email, mot de passe, rôles).
     *
     * @param userEmail l'email de l'utilisateur à rechercher
     * @return un objet UserDetails contenant les informations de l'utilisateur
     * @throws UsernameNotFoundException si l'utilisateur n'est pas trouvé dans le référentiel
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        // Recherche de l'utilisateur dans le référentiel à partir de son email
        return userRepository.findByEmail(userEmail)
                // Si l'utilisateur n'est pas trouvé, lance une exception UsernameNotFoundException
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
    }
}
