package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private CommentRepository commentRepository;
    private AdRepository adRepository;
    private UserRepository userRepository;
    private CommentMappingService commentMappingService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, AdRepository adRepository, UserRepository userRepository, CommentMappingService commentMappingService, UserService userService) {
    }

    /**
     * Метод для получения текущего пользователя
     * @return current user
     */
    private UserEntity getCurrentUserEntity() {
        return UserService.getCurrentUserEntity();
    }

    /**
     * Метод для проверки прав владельца
     * @param adId
     * @return boolean Author == current user
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
     * @return boolean
     */
    private boolean isAdmin() {
        return userService.isAdmin();
    }

    /**
     * Получает все комментарии к объявлению, отсортированные по дате создания (новые сверху)
     * @param adId
     * @return Comments DTO
     */
    public Comments getCommentsByAdId(Long adId) {
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        List<CommentEntity> entities = commentRepository.findByAdOrderByCreatedAtDesc(ad);
        List<Comment> results = entities.stream()
                .map(commentMappingService::toDto)
                .collect(Collectors.toList());
        return new Comments(results.size(), results);
    }

    /**
     * Добавляет новый комментарий к объявлению от имени текущего пользователя
     * @param adId
     * @param dto
     * Создает сущность комментария
     * Сохраняет результат в БД
     * @return Comment Dto
     */
    public Comment addComment(Long adId, CreateOrUpdateComment dto) {
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        UserEntity currentUser = getCurrentUserEntity();

        CommentEntity entity = commentMappingService.toEntity(dto, currentUser, ad);
        CommentEntity saved = commentRepository.save(entity);
        return commentMappingService.toDto(saved);
    }

    /**
     * Обновляет комментарий. Проверяет, является ли текущий пользователь владельцем или администратором
     * @param adId
     * @param commentId
     * @param dto
     * @return Comment Dto
     */
    public Comment updateComment(Long adId, Long commentId, CreateOrUpdateComment dto) {
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());


        entity.setText(dto.getText());

        CommentEntity updated = commentRepository.save(entity);
        return commentMappingService.toDto(updated);
    }

    /**
     * Удаляет комментарий. Проверяет права доступа
     * В случае ошибки поиска выводит текст ошибки
     * @param adId
     * @param commentId
     */
    public void deleteComment(Long adId, Long commentId) {
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        userService.checkOwnerOrAdmin(entity.getAuthor());

        commentRepository.delete(entity);
    }


}
