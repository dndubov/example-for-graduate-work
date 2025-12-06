package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository; // для поиска автора объявления
    private final AdMappingService adMappingService;
    private final UserMappingService userMappingService; // для получения данных автора в ExtendedAd

    // Путь для сохранения изображений объявлений
    private final String adImageUploadPath = "uploads/ads/";

    // Метод для получения текущего пользователя
    private UserEntity getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Метод для проверки прав администратора
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    //Получает все объявления
    public static Ads getAllAds() {
        List<AdEntity> entities = adRepository.findAll();
        List<Ad> results = entities.stream()
                .map(adMappingService::toAdDto)
                .collect(Collectors.toList());
        return new Ads(results.size(), results); // предполагается, что Ads DTO имеет поля count и results
    }

    //Получает объявления текущего пользователя
    public static Ads getMyAds() {
        UserEntity currentUser = getCurrentUserEntity();
        List<AdEntity> entities = adRepository.findByAuthor(currentUser);
        List<Ad> results = entities.stream()
                .map(adMappingService::toAdDto)
                .collect(Collectors.toList());
        return new Ads(results.size(), results);
    }

    //Получает расширенную информацию об объявлении по ID
    public static ExtendedAd getAdById(Long id) {
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        return adMappingService.toExtendedDto(entity);
    }
    //Обновляет изображение объявления. Проверяет права доступа
    public static void updateAdImage(Long adId, MultipartFile image) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        UserEntity currentUser = getCurrentUserEntity();

        // Проверка прав: владелец или админ
        if (!entity.getAuthor().equals(currentUser) && !isAdmin()) {
            throw new RuntimeException("You do not have permission to update this ad's image.");
        }

        // Удалить старое изображение
        if (entity.getImage() != null) {
            Path oldImagePath = Paths.get(entity.getImage());
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                // Логировать ошибку
                System.err.println("Could not delete old ad image: " + e.getMessage());
            }
        }

        // Сохранить новое изображение
        String newPath = saveAdImage(image);
        entity.setImage(newPath);

        adRepository.save(entity);
    }

    // Вспомогательный метод для сохранения изображения
    private String saveAdImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null; // или бросить исключение, если изображение обязательно
        }

        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(adImageUploadPath).resolve(fileName);

        try {
            Files.createDirectories(Paths.get(adImageUploadPath));
            Files.write(filePath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ad image", e);
        }

        return filePath.toString();
    }

    //Создает новое объявление от имени текущего пользователя
    public static Ad createAd(CreateOrUpdateAd dto, MultipartFile image) {
        UserEntity currentUser = getCurrentUserEntity();

        // Сохранить изображение и получить путь
        String imagePath = saveAdImage(image);

        // Создать сущность объявления
        AdEntity entity = adMappingService.toNewEntity(dto, currentUser);
        entity.setImage(imagePath);

        AdEntity saved = adRepository.save(entity);
        return adMappingService.toAdDto(saved);
    }

    //Обновляет объявление. Проверяет, является ли текущий пользователь владельцем или администратором
    public static Ad updateAd(Long adId, CreateOrUpdateAd dto) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        UserEntity currentUser = getCurrentUserEntity();

        // Проверка прав: владелец или админ
        if (!entity.getAuthor().equals(currentUser) && !isAdmin()) {
            throw new RuntimeException("You do not have permission to update this ad.");
        }

        // Обновить поля сущности
        adMappingService.updateEntity(dto, entity);

        AdEntity updated = adRepository.save(entity);
        return adMappingService.toAdDto(updated);
    }

    //Удаляет объявление. Проверяет права доступа
    public static void deleteAd(Long adId) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        UserEntity currentUser = getCurrentUserEntity();

        // Проверка прав: владелец или админ
        if (!entity.getAuthor().equals(currentUser) && !isAdmin()) {
            throw new RuntimeException("You do not have permission to delete this ad.");
        }

        // Удалить файл изображения, если он есть
        if (entity.getImage() != null) {
            Path imagePath = Paths.get(entity.getImage());
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                // Логировать ошибку
                System.err.println("Could not delete ad image: " + e.getMessage());
            }
        }
        adRepository.delete(entity);
    }
}
