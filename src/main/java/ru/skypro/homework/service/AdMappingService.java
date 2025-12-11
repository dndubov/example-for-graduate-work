package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.UserEntity;

@Service
@RequiredArgsConstructor
public class AdMappingService {

    private final AdMapper adMapper;

    // Entity → короткое Ad DTO
    public Ad toAdDto(AdEntity entity) {
        Ad dto = adMapper.toDto(entity);

        // pk ← id
        if (entity.getId() != null) {
            dto.setPk(entity.getId().intValue());
        }

        // image ← красивый URL
        if (entity.getId() != null &&
                entity.getImage() != null &&
                !entity.getImage().isBlank()) {

            dto.setImage("/ads/" + entity.getId() + "/image");
        } else {
            dto.setImage(null);
        }

        return dto;
    }

    // Entity → ExtendedAd DTO
    public ExtendedAd toExtendedDto(AdEntity entity) {
        ExtendedAd dto = adMapper.toExtendedDto(entity);

        if (entity.getId() != null) {
            dto.setPk(entity.getId().intValue());
        }

        if (entity.getId() != null &&
                entity.getImage() != null &&
                !entity.getImage().isBlank()) {

            dto.setImage("/ads/" + entity.getId() + "/image");
        } else {
            dto.setImage(null);
        }

        return dto;
    }

    // CreateOrUpdateAd → новая сущность объявления
    public AdEntity toNewEntity(CreateOrUpdateAd dto, UserEntity author) {
        AdEntity entity = new AdEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setAuthor(author);
        return entity;
    }

    // Обновление существующего объявления полями из DTO
    public void updateEntity(CreateOrUpdateAd dto, AdEntity entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
    }
}
