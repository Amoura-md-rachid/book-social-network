package com.amoura.book.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilitaire pour la gestion des opérations de fichiers.
 *
 * Cette classe fournit des méthodes statiques pour lire des fichiers à partir d'un emplacement spécifié.
 */
@Slf4j
public class FileUtils {

    /**
     * Lit un fichier à partir d'un emplacement spécifié et retourne son contenu sous forme de tableau d'octets.
     *
     * @param fileUrl L'URL ou le chemin du fichier à lire.
     * @return Un tableau d'octets contenant le contenu du fichier, ou null si le fichier n'existe pas ou si une erreur se produit.
     */
    public static byte[] readFileFromLocation(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return null;
        }
        try {
            Path filePath = new File(fileUrl).toPath();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.warn("No file found in the path {}", fileUrl);
        }
        return null;
    }
}

