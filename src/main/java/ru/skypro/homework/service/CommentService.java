package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с комментариями к объявлениям.
 * <p>
 * Поддерживает добавление, обновление, удаление и
 * получение комментариев по идентификатору объявления.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;          // пока не используем, но пусть будет
    private final CommentMappingService commentMappingService;
    private final UserService userService;

    /** Текущий пользователь через UserService */
    private UserEntity getCurrentUserEntity() {
        return userService.getCurrentUserEntity();
    }

    /** Проверка: текущий пользователь — владелец объявления */
    public boolean isOwner(Long adId) {
        AdEntity ad = adRepository.findById(adId).orElse(null);
        if (ad == null) {
            return false;
        }
        UserEntity currentUser = getCurrentUserEntity();
        return ad.getAuthor().equals(currentUser);
    }

    /** Проверка прав администратора (делегируем в UserService) */
    private boolean isAdmin() {
        return userService.isAdmin();
    }

    /** Все комментарии к объявлению (новые сверху) */
    public Comments getCommentsByAdId(Long adId) {
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        List<CommentEntity> entities = commentRepository.findByAdOrderByCreatedAtDesc(ad);

        List<Comment> results = entities.stream()
                .map(commentMappingService::toDto)
                .collect(Collectors.toList());

        return new Comments(results.size(), results);
    }

    /** Добавить комментарий к объявлению от имени текущего пользователя */
    public Comment addComment(Long adId, CreateOrUpdateComment dto) {
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        UserEntity currentUser = getCurrentUserEntity();

        CommentEntity entity = commentMappingService.toEntity(dto, currentUser, ad);
        CommentEntity saved = commentRepository.save(entity);

        return commentMappingService.toDto(saved);
    }

    /** Обновить комментарий (владелец или админ) */
    public Comment updateComment(Long adId, Long commentId, CreateOrUpdateComment dto) {
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        entity.setText(dto.getText());
        CommentEntity updated = commentRepository.save(entity);

        return commentMappingService.toDto(updated);
    }

    /** Удалить комментарий (владелец или админ) */
    public void deleteComment(Long adId, Long commentId) {
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        commentRepository.delete(entity);
    }
}
