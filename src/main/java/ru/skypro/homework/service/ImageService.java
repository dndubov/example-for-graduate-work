package ru.skypro.homework.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Service
public class ImageService {

    @Value("${images.dir}")
    private String imagesDir;

    public String saveUserImage(Long userId, MultipartFile file) {
        return saveImage("users", userId.toString(), file);
    }

    public String saveAdImage(Long adId, MultipartFile file) {
        return saveImage("ads", adId.toString(), file);
    }

    public byte[] load(String folder, String fileName) {
        Path path = Paths.get(imagesDir, folder, fileName);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
    }

    // Приватные вспомогательные методы

    private String saveImage(String folder, String baseName, MultipartFile file) {
        String ext = getExtension(file.getOriginalFilename());
        if (ext == null || ext.isBlank()) {
            ext = "jpg";
        }

        try {
            Path dir = Paths.get(imagesDir, folder);
            Files.createDirectories(dir);

            Path target = dir.resolve(baseName + "." + ext);
            file.transferTo(target.toFile());

            // строка, которую будем хранить в БД (относительный путь)
            return folder + "/" + baseName + "." + ext;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save image",
                    e
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
