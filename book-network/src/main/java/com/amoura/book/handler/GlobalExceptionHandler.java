package com.amoura.book.handler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.HashSet;
import java.util.Set;

import static com.amoura.book.handler.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


/**
 * Gestionnaire global des exceptions pour l'application.
 *
 * Cette classe utilise l'annotation `@RestControllerAdvice` pour capturer et gérer
 * de manière centralisée les exceptions levées par les contrôleurs de l'application.
 * Elle permet de retourner des réponses standardisées et détaillées en cas d'erreurs,
 * facilitant ainsi le traitement et la lisibilité des erreurs côté client.
 *
 * Annotations :
 * - `@RestControllerAdvice` : Indique que cette classe intercepte les exceptions
 *   levées par les contrôleurs annotés avec `@RestController`.
 *
 * Méthodes de gestion des exceptions :
 * - Chaque méthode est annotée avec `@ExceptionHandler` pour gérer un type spécifique d'exception.
 * - Les réponses sont construites avec `ResponseEntity<ExceptionResponse>` pour fournir des détails sur l'erreur.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les exceptions `LockedException` (compte utilisateur verrouillé).
     *
     * @param exp Instance de `LockedException`.
     * @return Réponse HTTP avec le code d'erreur et une description de l'erreur.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Gère les exceptions `DisabledException` (compte utilisateur désactivé).
     *
     * @param exp Instance de `DisabledException`.
     * @return Réponse HTTP avec le code d'erreur et une description de l'erreur.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Gère les exceptions `BadCredentialsException` (identifiants incorrects).
     *
     * @return Réponse HTTP avec le code d'erreur et une description de l'erreur.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException() {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .error("Login and / or Password is incorrect")
                                .build()
                );
    }

    /**
     * Gère les exceptions `MessagingException` (erreurs liées aux services de messagerie).
     *
     * @param exp Instance de `MessagingException`.
     * @return Réponse HTTP avec une description de l'erreur.
     */
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }

    /**
     * Gère les exceptions de validation des arguments (`MethodArgumentNotValidException`).
     *
     * @param exp Instance de `MethodArgumentNotValidException`.
     * @return Réponse HTTP avec les erreurs de validation collectées.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
        Set<String> errors = new HashSet<>();
        exp.getBindingResult().getAllErrors().forEach(error -> {
            var errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .validationErrors(errors)
                                .build()
                );
    }

    /**
     * Gère les exceptions générales (`Exception`).
     *
     * @param exp Instance de `Exception`.
     * @return Réponse HTTP avec une description de l'erreur.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {
        exp.printStackTrace();
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal error, please contact the admin")
                                .error(exp.getMessage())
                                .build()
                );
    }
}

