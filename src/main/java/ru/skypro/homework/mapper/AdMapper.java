package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.model.AdEntity;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AdMapper {

    // короткое объявление для списка
    @Mapping(source = "author.id", target = "author")
    @Mapping(source = "image", target = "image")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "title", target = "title")
    Ad toDto(AdEntity entity);

    // расширенное объявление
    @Mapping(source = "id", target = "pk")
    @Mapping(source = "author.firstName", target = "authorFirstName")
    @Mapping(source = "author.lastName", target = "authorLastName")
    @Mapping(source = "author.phone", target = "phone")
    @Mapping(source = "image", target = "image")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    ExtendedAd toExtendedDto(AdEntity entity);
}
