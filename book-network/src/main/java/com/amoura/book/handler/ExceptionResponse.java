package com.amoura.book.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * Classe représentant la réponse d'exception pour les erreurs de l'application.
 *
 * Cette classe est utilisée pour encapsuler les informations relatives aux erreurs
 * rencontrées dans l'application et les retourner dans une réponse formatée en JSON.
 *
 * Annotations :
 * - @Getter, @Setter : Génèrent automatiquement les getters et setters pour les attributs de la classe grâce à Lombok.
 * - @Builder : Permet de construire des objets de cette classe en utilisant le pattern Builder.
 * - @AllArgsConstructor : Génère un constructeur avec tous les champs de la classe.
 * - @NoArgsConstructor : Génère un constructeur sans paramètres.
 * - @JsonInclude(JsonInclude.Include.NON_EMPTY) : Exclut les champs `null` ou vides lors de la sérialisation en JSON.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExceptionResponse {

    /**
     * Code d'erreur métier spécifique, utilisé pour identifier l'erreur dans le contexte métier.
     */
    private Integer businessErrorCode;

    /**
     * Description de l'erreur métier, expliquant la nature de l'erreur de manière détaillée.
     */
    private String businessErrorDescription;

    /**
     * Message d'erreur générique, décrivant l'erreur rencontrée.
     */
    private String error;

    /**
     * Ensemble des erreurs de validation, contenant les messages d'erreur pour les champs invalides.
     */
    private Set<String> validationErrors;

    /**
     * Dictionnaire des erreurs supplémentaires, avec les clés correspondant aux champs en erreur
     * et les valeurs représentant les messages d'erreur associés.
     */
    private Map<String, String> errors;
}