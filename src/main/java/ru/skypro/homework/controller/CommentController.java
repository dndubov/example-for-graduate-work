package ru.skypro.homework.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.service.CommentService;

@RestController
@RequestMapping("/ads/{adId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Comments> getComments(@PathVariable Long adId) {
        Comments comments = commentService.getCommentsByAdId(adId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable Long adId, @RequestBody CreateOrUpdateComment dto) {
        Comment comment = commentService.addComment(adId, dto);
        return ResponseEntity.ok(comment);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long adId,
            @PathVariable Long commentId,
            @RequestBody CreateOrUpdateComment dto) {
        Comment comment = commentService.updateComment(adId, commentId, dto);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long adId, @PathVariable Long commentId) {
        commentService.deleteComment(adId, commentId);
        return ResponseEntity.ok().build();
    }
}
