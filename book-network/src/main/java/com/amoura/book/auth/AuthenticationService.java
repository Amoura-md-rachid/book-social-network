package com.amoura.book.auth;

import com.amoura.book.email.EmailService;
import com.amoura.book.email.EmailTemplateName;
import com.amoura.book.role.RoleRepository;
import com.amoura.book.security.JwtService;
import com.amoura.book.user.Token;
import com.amoura.book.user.TokenRepository;
import com.amoura.book.user.User;
import com.amoura.book.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Service d'authentification pour gérer l'enregistrement des utilisateurs,
 * l'envoi d'emails de validation et la génération de jetons d'activation.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    // Repositories pour les entités User, Role et Token
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;

    // Services pour l'encodage des mots de passe, la gestion des JWT et l'envoi d'emails
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    /**
     * Enregistre un nouvel utilisateur en lui attribuant le rôle "USER" et en envoyant un email de validation.
     *
     * @param request Objet RegistrationRequest contenant les informations de l'utilisateur à enregistrer.
     */
    public void register(RegistrationRequest request) throws MessagingException {
        // Récupération du rôle "USER" depuis le repository
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Le rôle 'USER' n'a pas été initialisé"));

        // Création d'un nouvel utilisateur
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encodage du mot de passe
                .accountLocked(false)
                .enabled(false) // L'utilisateur n'est pas activé par défaut
                .roles(List.of(userRole)) // Attribution du rôle "USER"
                .build();

        // Sauvegarde de l'utilisateur en base de données
        userRepository.save(user);

        // Envoi de l'email de validation
        sendValidationEmail(user);
    }

    /**
     * Envoie un email de validation contenant un jeton d'activation à l'utilisateur.
     *
     * @param user Utilisateur à qui envoyer l'email de validation.
     */
    private void sendValidationEmail(User user) throws MessagingException {
        // Génération et sauvegarde du jeton d'activation
        var newToken = generateAndSaveActivationToken(user);

        // TODO: Ajouter l'envoi de l'email via EmailService
        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
                );
    }

    /**
     * Génère un jeton d'activation pour l'utilisateur et le sauvegarde en base de données.
     *
     * @param user Utilisateur pour lequel générer le jeton d'activation.
     * @return Le jeton d'activation généré sous forme de chaîne de caractères.
     */
    private String generateAndSaveActivationToken(User user) {
        // Génération d'un code d'activation à 6 chiffres
        String generatedToken = generateActivationCode(6);

        // Création d'une instance de Token
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15)) // Expiration dans 15 minutes
                .user(user)
                .build();

        // Sauvegarde du token en base de données
        tokenRepository.save(token);

        return generatedToken;
    }

    /**
     * Génère un code d'activation numérique de la longueur spécifiée.
     *
     * Cette méthode crée un code d'activation composé uniquement de chiffres
     * en utilisant un générateur de nombres aléatoires sécurisés (`SecureRandom`).
     * Le code d'activation est utilisé pour la validation des comptes utilisateurs.
     *
     * @param length Longueur du code d'activation à générer.
     * @return Un code d'activation numérique sous forme de chaîne de caractères.
     */
    private String generateActivationCode(int length) {
        // Caractères numériques autorisés dans le code d'activation
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();

        // Génération du code d'activation
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }



    /**
     * Authentifie un utilisateur et génère un jeton JWT en cas de succès.
     *
     * Cette méthode utilise le `AuthenticationManager` pour valider les
     * informations d'identification fournies par l'utilisateur. Si l'authentification
     * est réussie, elle génère un jeton JWT contenant les informations utilisateur.
     *
     * @param request Objet contenant les informations d'identification de l'utilisateur
     *                (email et mot de passe).
     * @return Un objet `AuthenticationResponse` contenant le jeton JWT généré.
     *
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authentification de l'utilisateur avec email et mot de passe
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Récupération de l'utilisateur authentifié
        var user = (User) auth.getPrincipal();

        // Création des claims JWT avec des informations utilisateur personnalisées
        var claims = new HashMap<String, Object>();
        claims.put("fullName", user.fullName());

        // Génération du jeton JWT
        var jwtToken = jwtService.generateToken(claims, user);

        // Retourne la réponse d'authentification contenant le jeton
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    /**
     * Active un compte utilisateur en utilisant un token d'activation.
     *
     * Cette méthode vérifie si le token d'activation est valide et s'il n'est pas expiré.
     * Si le token est valide, le compte utilisateur est activé (enabled).
     * Si le token est expiré, un nouvel email de validation est envoyé à l'utilisateur.
     *
     * @param token Le token d'activation à valider.
     * @throws MessagingException Si une erreur se produit lors de l'envoi de l'email.
     * @throws RuntimeException Si le token est invalide ou expiré.
     */
    @Transactional
    public void activateAccount(String token) throws MessagingException {
        // Recherche du token dans la base de données
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // Vérification de l'expiration du token
        if (LocalDateTime.now().isAfter(savedToken.getExpiredAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email address");
        }

        // Recherche de l'utilisateur lié au token
        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Activation du compte utilisateur
        user.setEnabled(true);
        userRepository.save(user);

        // Mise à jour de la validation du token
        savedToken.setValidatAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

}
