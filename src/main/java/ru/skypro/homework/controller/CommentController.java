package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.service.CommentService;

@RestController
@RequestMapping("/ads/{adId}/comments")
@Tag(name = "Комментарии", description = "Операции с комментариями к объявлениям")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Получить все комментарии к объявлению")
    @GetMapping
    public ResponseEntity<Comments> getComments(@PathVariable Long adId) {
        Comments comments = commentService.getCommentsByAdId(adId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Добавить комментарий к объявлению")
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Long adId,
            @RequestBody CreateOrUpdateComment dto
    ) {
        Comment comment = commentService.addComment(adId, dto);
        return ResponseEntity.ok(comment);
    }

    @Operation(summary = "Обновить комментарий к объявлению")
    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long adId,
            @PathVariable Long commentId,
            @RequestBody CreateOrUpdateComment dto
    ) {
        Comment comment = commentService.updateComment(adId, commentId, dto);
        return ResponseEntity.ok(comment);
    }

    @Operation(summary = "Удалить комментарий к объявлению")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long adId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(adId, commentId);
        return ResponseEntity.ok().build();
    }
}
