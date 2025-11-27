package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.model.CommentEntity;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "pk", source = "id")
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "authorImage", source = "author.image")
    @Mapping(
            target = "createdAt",
            expression = "java(entity.getCreatedAt() == null ? null : entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))"
    )
    Comment toDto(CommentEntity entity);
}
