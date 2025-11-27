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

    // Маппинг: Entity → короткое Ad DTO
    public Ad toAdDto(AdEntity entity) {
        return adMapper.toDto(entity);
    }

    // Маппинг: Entity → ExtendedAd DTO
    public ExtendedAd toExtendedDto(AdEntity entity) {
        return adMapper.toExtendedDto(entity);
    }

    // Маппинг: CreateOrUpdateAd DTO → новая сущность объявления
    public AdEntity toNewEntity(CreateOrUpdateAd dto, UserEntity author) {
        AdEntity entity = new AdEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setAuthor(author);
        return entity;
    }

    // Маппинг: обновление существующего объявления полями из DTO
    public void updateEntity(CreateOrUpdateAd dto, AdEntity entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
    }
}
