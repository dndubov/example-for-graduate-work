package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository; // Может понадобиться для getCurrentUserEntity в UserService

    @Mock
    private CommentMappingService commentMappingService;

    @Mock
    private UserService userService; // Мокируем для проверки прав

    private CommentService commentService;

    private UserEntity testUser;
    private UserEntity adminUser;
    private AdEntity testAd;
    private CommentEntity testComment;
    private Comment testCommentDto;
    private CreateOrUpdateComment updateDto;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, adRepository, userRepository, commentMappingService, userService);

        testUser = UserEntity.builder().id(1L).email("user@example.com").role("USER").build();
        adminUser = UserEntity.builder().id(99L).email("admin@example.com").role("ADMIN").build();
        testAd = AdEntity.builder().id(10L).title("Test Ad").build();
        testComment = CommentEntity.builder().id(100L).text("Original comment").author(testUser).ad(testAd).createdAt(LocalDateTime.now()).build();
        testCommentDto = new Comment();
        testCommentDto.setPk(100);
        updateDto = new CreateOrUpdateComment();
        updateDto.setText("Updated comment text");
    }

    // --- getCommentsByAdId ---

    @Test
    void getCommentsByAdId_WithValidAdId_ReturnsComments() {

        List<CommentEntity> entities = List.of(testComment);
        List<Comment> dtos = List.of(testCommentDto);
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd));
        when(commentRepository.findByAdOrderByCreatedAtDesc(testAd)).thenReturn(entities);
        when(commentMappingService.toDto(testComment)).thenReturn(testCommentDto);


        Comments result = commentService.getCommentsByAdId(10L);


        assertEquals(1, result.getCount());
        assertEquals(dtos, result.getResults());
        verify(adRepository).findById(10L);
        verify(commentRepository).findByAdOrderByCreatedAtDesc(testAd);
        verify(commentMappingService).toDto(testComment);
    }

    @Test
    void getCommentsByAdId_WithInvalidAdId_ThrowsRuntimeException() {

        when(adRepository.findById(999L)).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.getCommentsByAdId(999L));
        assertEquals("Ad not found", thrown.getMessage());
        verify(adRepository).findById(999L);
    }

    // --- addComment ---

    @Test
    void addComment_WithValidData_CreatesAndReturnsComment() {

        CreateOrUpdateComment createDto = new CreateOrUpdateComment();
        createDto.setText("New comment text");

        CommentEntity newCommentEntity = CommentEntity.builder().text("New comment text").author(testUser).ad(testAd).build();
        Comment createdCommentDto = new Comment(); // предположим DTO создан

        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd));
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(commentMappingService.toEntity(createDto, testUser, testAd)).thenReturn(newCommentEntity);
        when(commentRepository.save(newCommentEntity)).thenReturn(testComment);
        when(commentMappingService.toDto(testComment)).thenReturn(testCommentDto);

        Comment result = commentService.addComment(10L, createDto);

        assertEquals(testCommentDto, result);
        verify(adRepository).findById(10L);
        verify(userService).getCurrentUserEntity();
        verify(commentMappingService).toEntity(createDto, testUser, testAd);
        verify(commentRepository).save(newCommentEntity);
        verify(commentMappingService).toDto(testComment);
    }

    @Test
    void addComment_WithInvalidAdId_ThrowsRuntimeException() {

        CreateOrUpdateComment createDto = new CreateOrUpdateComment();
        createDto.setText("New comment text");

        when(adRepository.findById(999L)).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.addComment(999L, createDto));
        assertEquals("Ad not found", thrown.getMessage());
        verify(adRepository).findById(999L);
        verify(userService, never()).getCurrentUserEntity(); // Не должно вызываться
    }

    // --- updateComment ---

    @Test
    void updateComment_WithOwnerPermission_UpdatesAndReturnsComment() {

        when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd)); // Проверка принадлежности
        doNothing().when(userService).checkOwnerOrAdmin(testUser); // Мокаем проверку прав
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMappingService.toDto(testComment)).thenReturn(testCommentDto);


        Comment result = commentService.updateComment(10L, 100L, updateDto);

        assertEquals("Updated comment text", testComment.getText());
        assertEquals(testCommentDto, result);
        verify(commentRepository).findById(100L);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(testUser); // Проверка вызова
        verify(commentRepository).save(testComment);
        verify(commentMappingService).toDto(testComment);
    }

    @Test
    void updateComment_WithAdminPermission_UpdatesAndReturnsComment() {

        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        CommentEntity commentByOther = CommentEntity.builder().id(100L).text("Original comment").author(otherAuthor).ad(testAd).build();

        when(commentRepository.findById(100L)).thenReturn(Optional.of(commentByOther));
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd)); // Проверка принадлежности
        doNothing().when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, как будто текущий - админ
        when(commentRepository.save(commentByOther)).thenReturn(commentByOther);
        when(commentMappingService.toDto(commentByOther)).thenReturn(testCommentDto);


        Comment result = commentService.updateComment(10L, 100L, updateDto);


        assertEquals("Updated comment text", commentByOther.getText());
        assertEquals(testCommentDto, result);
        verify(commentRepository).findById(100L);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor); // Проверка вызова
        verify(commentRepository).save(commentByOther);
        verify(commentMappingService).toDto(commentByOther);
    }

    @Test
    void updateComment_WithoutPermission_ThrowsRuntimeException() {

        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        CommentEntity commentByOther = CommentEntity.builder().id(100L).text("Original comment").author(otherAuthor).ad(testAd).build();

        when(commentRepository.findById(100L)).thenReturn(Optional.of(commentByOther));
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd)); // Проверка принадлежности
        doThrow(new RuntimeException("You do not have permission to perform this action."))
                .when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, чтобы выбросить исключение


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.updateComment(10L, 100L, updateDto));
        assertEquals("You do not have permission to perform this action.", thrown.getMessage());
        verify(commentRepository).findById(100L);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor);
        verify(commentRepository, never()).save(any()); // Убедимся, что сохранение не произошло
    }

    @Test
    void updateComment_WithInvalidCommentId_ThrowsRuntimeException() {

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.updateComment(10L, 999L, updateDto));
        assertEquals("Comment not found", thrown.getMessage());
        verify(commentRepository).findById(999L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }

    @Test
    void updateComment_WithMismatchedAdId_ThrowsRuntimeException() {

        when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
        when(adRepository.findById(99L)).thenReturn(Optional.of(AdEntity.builder().id(99L).build())); // Другое объявление


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.updateComment(99L, 100L, updateDto));
        assertEquals("Comment does not belong to the specified ad.", thrown.getMessage());
        verify(commentRepository).findById(100L);
        verify(adRepository).findById(99L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }

    // --- deleteComment ---

    @Test
    void deleteComment_WithOwnerPermission_DeletesComment() {

        when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd)); // Проверка принадлежности
        doNothing().when(userService).checkOwnerOrAdmin(testUser); // Мокаем проверку прав

        assertDoesNotThrow(() -> commentService.deleteComment(10L, 100L));


        verify(commentRepository).findById(100L);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(testUser); // Проверка вызова
        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_WithAdminPermission_DeletesComment() {

        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        CommentEntity commentByOther = CommentEntity.builder().id(100L).text("Original comment").author(otherAuthor).ad(testAd).build();

        when(commentRepository.findById(100L)).thenReturn(Optional.of(commentByOther));
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd)); // Проверка принадлежности
        doNothing().when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, как будто текущий - админ


        assertDoesNotThrow(() -> commentService.deleteComment(10L, 100L));


        verify(commentRepository).findById(100L);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor); // Проверка вызова
        verify(commentRepository).delete(commentByOther);
    }

    @Test
    void deleteComment_WithoutPermission_ThrowsRuntimeException() {

        UserEntity otherAuthor = UserEntity.builder().id(2L).email("other@example.com").build();
        CommentEntity commentByOther = CommentEntity.builder().id(100L).text("Original comment").author(otherAuthor).ad(testAd).build();

        when(commentRepository.findById(100L)).thenReturn(Optional.of(commentByOther));
        when(adRepository.findById(10L)).thenReturn(Optional.of(testAd)); // Проверка принадлежности
        doThrow(new RuntimeException("You do not have permission to perform this action."))
                .when(userService).checkOwnerOrAdmin(otherAuthor); // Мокаем проверку прав, чтобы выбросить исключение


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.deleteComment(10L, 100L));
        assertEquals("You do not have permission to perform this action.", thrown.getMessage());
        verify(commentRepository).findById(100L);
        verify(adRepository).findById(10L);
        verify(userService).checkOwnerOrAdmin(otherAuthor);
        verify(commentRepository, never()).delete(any()); // Убедимся, что удаление не произошло
    }

    @Test
    void deleteComment_WithInvalidCommentId_ThrowsRuntimeException() {

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.deleteComment(10L, 999L));
        assertEquals("Comment not found", thrown.getMessage());
        verify(commentRepository).findById(999L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }

    @Test
    void deleteComment_WithMismatchedAdId_ThrowsRuntimeException() {

        when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
        when(adRepository.findById(99L)).thenReturn(Optional.of(AdEntity.builder().id(99L).build())); // Другое объявление


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> commentService.deleteComment(99L, 100L));
        assertEquals("Comment does not belong to the specified ad.", thrown.getMessage());
        verify(commentRepository).findById(100L);
        verify(adRepository).findById(99L);
        verify(userService, never()).checkOwnerOrAdmin(any()); // Проверка не должна вызываться
    }
}
