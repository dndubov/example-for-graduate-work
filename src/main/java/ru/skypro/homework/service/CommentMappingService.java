package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;

@Service
@RequiredArgsConstructor
public class CommentMappingService {

    private final CommentMapper commentMapper;

    public Comment toDto(CommentEntity entity) {
        return commentMapper.toDto(entity);
    }

    public CommentEntity toEntity(CreateOrUpdateComment dto, UserEntity author, AdEntity ad) {
        CommentEntity entity = new CommentEntity();
        entity.setText(dto.getText());
        entity.setAuthor(author);
        entity.setAd(ad);
        entity.setCreatedAt(java.time.LocalDateTime.now());
        return entity;
    }
}
