package com.amoura.book.file;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



/**
 * Service pour la gestion du stockage de fichiers.
 *
 * Cette classe fournit des méthodes pour sauvegarder des fichiers dans un répertoire spécifié.
 * Elle utilise des chemins de fichiers configurables et gère la création de répertoires si nécessaire.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    /**
     * Chemin de base pour le téléchargement des fichiers, configuré via les propriétés de l'application.
     */
    @Value("${application.file.uploads.photos-output-path}")
    private String fileUploadPath;

    /**
     * Sauvegarde un fichier dans un sous-répertoire spécifique à l'utilisateur.
     *
     * @param sourceFile Le fichier à sauvegarder.
     * @param userId L'ID de l'utilisateur pour lequel le fichier est sauvegardé.
     * @return Le chemin complet où le fichier a été sauvegardé, ou null en cas d'échec.
     */
    public String saveFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String userId
    ) {
        final String fileUploadSubPath = "users" + File.separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    /**
     * Télécharge un fichier dans un sous-répertoire spécifié.
     *
     * @param sourceFile Le fichier à télécharger.
     * @param fileUploadSubPath Le sous-répertoire où le fichier doit être téléchargé.
     * @return Le chemin complet où le fichier a été sauvegardé, ou null en cas d'échec.
     */
    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadSubPath
    ) {
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);

        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                log.warn("Failed to create the target folder: " + targetFolder);
                return null;
            }
        }
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File saved to: " + targetFilePath);
            return targetFilePath;
        } catch (IOException e) {
            log.error("File was not saved", e);
        }
        return null;
    }

    /**
     * Obtient l'extension d'un fichier à partir de son nom.
     *
     * @param fileName Le nom du fichier.
     * @return L'extension du fichier en minuscules, ou une chaîne vide si aucune extension n'est trouvée.
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}