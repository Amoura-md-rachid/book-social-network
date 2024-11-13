package com.amoura.book.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

/**
 * Enumération des codes d'erreur métier avec leurs descriptions et statuts HTTP associés.
 */
public enum BusinessErrorCodes {
    /**
     * Aucun code.
     */
    NO_CODE(0, NOT_IMPLEMENTED, "No code"),

    /**
     * Mot de passe actuel incorrect.
     */
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password is incorrect"),

    /**
     * Le nouveau mot de passe ne correspond pas.
     */
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new password does not match"),

    /**
     * Compte utilisateur verrouillé.
     */
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"),

    /**
     * Compte utilisateur désactivé.
     */
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled"),

    /**
     * Identifiants incorrects.
     */
    BAD_CREDENTIALS(304, FORBIDDEN, "Login and / or Password is incorrect");


    @Getter
    private final int code;

    @Getter
    private final String description;

    @Getter
    private final HttpStatus httpStatus;

    /**
     * Constructeur de l'énumération BusinessErrorCodes.
     *
     * @param code        Le code d'erreur.
     * @param status      Le statut HTTP associé à l'erreur.
     * @param description La description de l'erreur.
     */
    BusinessErrorCodes(int code, HttpStatus status, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = status;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

