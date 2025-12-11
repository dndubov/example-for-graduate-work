package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для работы с объявлениями.
 * <p>
 * Отвечает за:
 * <ul>
 *     <li>создание объявлений и сохранение картинок;</li>
 *     <li>получение списка и деталей объявления;</li>
 *     <li>обновление и удаление объявлений;</li>
 *     <li>чтение и обновление файлов изображений с диска.</li>
 * </ul>
 */

@Service
@RequiredArgsConstructor
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserService userService;
    private final AdMappingService adMappingService;

    private static final String IMAGES_ROOT = "images";
    private static final String ADS_FOLDER = "ads";

    private static String saveAdImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path dir = Paths.get(IMAGES_ROOT, ADS_FOLDER);
        Path filePath = dir.resolve(fileName);

        try {
            Files.createDirectories(dir);
            Files.write(filePath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save ad image", e);
        }

        return ADS_FOLDER + "/" + fileName;
    }

    /**
     * Возвращает список всех объявлений в формате,
     * ожидаемом фронтенд-приложением.
     *
     * @return обёртка с количеством объявлений и их DTO
     */

    public Ads getAllAds() {
        List<AdEntity> entities = adRepository.findAll();
        List<Ad> ads = entities.stream()
                .map(adMappingService::toAdDto)
                .collect(Collectors.toList());
        return new Ads(ads.size(), ads);
    }

    public Ads getMyAds() {
        UserEntity currentUser = userService.getCurrentUserEntity();
        List<AdEntity> entities = adRepository.findByAuthor(currentUser);
        List<Ad> ads = entities.stream()
                .map(adMappingService::toAdDto)
                .collect(Collectors.toList());
        return new Ads(ads.size(), ads);
    }

    /**
     * Возвращает расширенную информацию по объявлению:
     * описание, контакты автора и ссылку на картинку.
     *
     * @param id идентификатор объявления
     * @return расширенное DTO объявления
     */

    public ExtendedAd getAdById(Long id) {
        AdEntity entity = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        return adMappingService.toExtendedDto(entity);
    }

    public void updateAdImage(Long adId, MultipartFile image) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        if (entity.getImage() != null) {
            try {
                Files.deleteIfExists(Paths.get(IMAGES_ROOT).resolve(entity.getImage()));
            } catch (IOException ignored) {}
        }

        String path = saveAdImage(image);
        entity.setImage(path);
        adRepository.save(entity);
    }

    /**
     * Создаёт новое объявление от имени текущего пользователя.
     * <p>
     * Сохраняет файл изображения на диск и прописывает путь
     * к нему в сущности объявления.
     *
     * @param dto   данные объявления (заголовок, описание, цена)
     * @param image файл изображения
     * @return созданное объявление в виде DTO
     */

    public Ad createAd(CreateOrUpdateAd dto, MultipartFile image) {
        UserEntity currentUser = userService.getCurrentUserEntity();

        String imagePath = saveAdImage(image);

        AdEntity entity = adMappingService.toNewEntity(dto, currentUser);
        entity.setImage(imagePath);

        AdEntity saved = adRepository.save(entity);
        return adMappingService.toAdDto(saved);
    }

    public Ad updateAd(Long adId, CreateOrUpdateAd dto) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        adMappingService.updateEntity(dto, entity);

        AdEntity updated = adRepository.save(entity);
        return adMappingService.toAdDto(updated);
    }

    public void deleteAd(Long adId) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        if (entity.getImage() != null) {
            try {
                Files.deleteIfExists(Paths.get(IMAGES_ROOT).resolve(entity.getImage()));
            } catch (IOException ignored) {}
        }

        adRepository.delete(entity);
    }

    /**
     * Читает с диска файл изображения для указанного объявления.
     *
     * @param adId идентификатор объявления
     * @return массив байт изображения
     */

    public byte[] getAdImage(Long adId) {
        AdEntity entity = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        String imagePath = entity.getImage();
        if (imagePath == null || imagePath.isBlank()) {
            throw new RuntimeException("Ad has no image");
        }

        Path filePath = Paths.get(IMAGES_ROOT).resolve(imagePath); // "images" + "ads/uuid_name.jpg"

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ad image", e);
        }
    }
}
