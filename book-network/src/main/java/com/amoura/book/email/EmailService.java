package com.amoura.book.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

/**
 * Service pour l'envoi d'emails dans l'application.
 * Utilise JavaMailSender pour l'envoi d'emails MIME et Thymeleaf pour le rendu des templates d'email.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;

    // Injecte le moteur de templates Thymeleaf pour le rendu des emails HTML.
    private final SpringTemplateEngine templateEngine;

    /**
     * Envoie un email asynchrone avec un template HTML personnalisé.
     *
     * @param to             L'adresse email du destinataire
     * @param username       Le nom d'utilisateur (utilisé dans le contenu de l'email)
     * @param emailTemplate  Le template d'email à utiliser (si null, "confirm-email" par défaut)
     * @param confirmationUrl L'URL de confirmation à inclure dans l'email
     * @param activationCode Le code d'activation à inclure dans l'email
     * @param subject        Le sujet de l'email
     * @throws MessagingException Si une erreur survient lors de la création ou de l'envoi de l'email
     */
    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {

        // Sélection du nom du template d'email
        String templateName;
        if (emailTemplate == null) {
            // Utilise "confirm-email" comme template par défaut si aucun template n'est spécifié
            templateName = "confirm-email";
        } else {
            // Utilise le nom du template spécifié
            templateName = emailTemplate.getName();;
        }

        // Création d'un message MIME (email au format HTML)
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED, // Permet de gérer plusieurs parties (texte/HTML)
                UTF_8.name() // Encode le message en UTF-8
        );

        // Préparation des propriétés dynamiques à injecter dans le template Thymeleaf
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);             // Nom d'utilisateur
        properties.put("confirmationUrl", confirmationUrl); // URL de confirmation
        properties.put("activation_code", activationCode);  // Code d'activation

        // Création du contexte Thymeleaf pour injecter les variables dans le template
        Context context = new Context();
        context.setVariables(properties);

        // Définition de l'adresse email de l'expéditeur
        helper.setFrom("amoura.md.rachid@gmail.com");

        // Définition de l'adresse email du destinataire
        helper.setTo(to);

        // Définition du sujet de l'email
        helper.setSubject(subject);

        // Génération du contenu HTML de l'email à partir du template Thymeleaf
        String template = templateEngine.process(templateName, context);

        // Définition du contenu de l'email en HTML
        helper.setText(template, true);

        // Envoi de l'email
        mailSender.send(mimeMessage);

        // Log l'envoi de l'email pour le suivi
        log.info("Email envoyé avec succès à {}", to);
    }
}
