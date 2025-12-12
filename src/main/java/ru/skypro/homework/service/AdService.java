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

    private static AdRepository adRepository;
    private static UserRepository userRepository; // для поиска автора объявления
    private static AdMappingService adMappingService;
    private static UserMappingService userMappingService; // для получения данных автора в ExtendedAd
    private static UserService userService;

    // Путь для сохранения изображений объявлений
    static String adImageUploadPath = "uploads/ads/";

    public AdService(AdRepository adRepository, UserRepository userRepository, AdMappingService adMappingService, UserMappingService userMappingService, UserService userService) {
    }

    /**
     * Метод для получения текущего пользователя
     */
    private static UserEntity getCurrentUserEntity() {
        return UserService.getCurrentUserEntity();
    }

    /**
     * Метод для проверки владельца
     *
     * @param adId
     * @return Author == currentUser
     */
    public boolean isOwner(Long adId) {
        AdEntity ad = adRepository.findById(adId).orElse(null);
        if (ad == null) {
            return false;
        }
        UserEntity currentUser = getCurrentUserEntity();
        return ad.getAuthor().equals(currentUser);
    }

    /**
     * Метод для проверки прав администратора
     *
     * @return USER role
     */
    private static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Получает все объявления
     *
     * @return Ads DTO
     */
    public static Ads getAllAds() {
        List<AdEntity> entities = adRepository.findAll();
        List<Ad> results = entities.stream()
                .map(adMappingService::toAdDto)
                .collect(Collectors.toList());
        return new Ads(results.size(), results);
    }

    /**
     * Получает объявления текущего пользователя
     *
     * @return Ads DTO, автор текущий пользователь
     */
    public static Ads getMyAds() {
        UserEntity currentUser = getCurrentUserEntity();
        List<AdEntity> entities = adRepository.findByAuthor(currentUser);
        List<Ad> results = entities.stream()
                .map(adMappingService::toAdDto)
                .collect(Collectors.toList());
        return new Ads(results.size(), results);
    }

    /**
     * Получает расширенную информацию об объявлении по ID
     *
     * @param id
     * @return Ad DTO
     */
    public static ExtendedAd getAdById(Long id) {
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        return adMappingService.toExtendedDto(entity);
    }

    /**
     * Обновляет изображение объявления. Проверяет права доступа
     *
     * @param adId
     * @param image Удаляет старое изображение, выводит ошибку в случае неуспеха (если изображение отсутствует)
     *              Сохраняет новое изображение
     */
    public static void updateAdImage(Long adId, MultipartFile image) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        if (entity.getImage() != null) {
            Path oldImagePath = Paths.get(entity.getImage());
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                System.err.println("Could not delete old ad image: " + e.getMessage());
            }
        }

        String newPath = saveAdImage(image);
        entity.setImage(newPath);

        adRepository.save(entity);
    }

    /**
     * Метод для сохранения изображения
     *
     * @param image Захватывает изображение по адресу uploads/ads/
     *              Выводит ошибку в случае недоступности пути сохранения filePath
     * @return
     */
    private static String saveAdImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
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

    /**
     * Создает новое объявление от имени текущего пользователя
     *
     * @param dto
     * @param image Сохраняет изображение и получает путь
     *              Создает сущность объявления
     *              Сохраняет объявление в БД
     *              Выводит результат
     * @return
     */
    public static Ad createAd(CreateOrUpdateAd dto, MultipartFile image) {
        UserEntity currentUser = getCurrentUserEntity();


        String imagePath = saveAdImage(image);


        AdEntity entity = adMappingService.toNewEntity(dto, currentUser);
        entity.setImage(imagePath);

        AdEntity saved = adRepository.save(entity);
        return adMappingService.toAdDto(saved);
    }

    /**
     * Обновляет объявление. Проверяет, является ли текущий пользователь владельцем или администратором
     *
     * @param adId
     * @param dto  Обновляет поля сущности
     *             Сохраняет изменения в БД
     * @return
     */
    public static Ad updateAd(Long adId, CreateOrUpdateAd dto) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        adMappingService.updateEntity(dto, entity);

        AdEntity updated = adRepository.save(entity);
        return adMappingService.toAdDto(updated);
    }

    /**
     * Удаляет объявление. Проверяет права доступа
     * Выводит ошибку в случае отсутствия объявления
     * Удаляет файл изображения, если он есть, выводит ошибку, если изображение отсутствует
     * @param adId
     */
    public static void deleteAd(Long adId) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        if (entity.getImage() != null) {
            Path imagePath = Paths.get(entity.getImage());
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {

                System.err.println("Could not delete ad image: " + e.getMessage());
            }
        }
        adRepository.delete(entity);
    }

}
