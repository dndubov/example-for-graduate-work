package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Объявления", description = "CRUD операции с объявлениями и комментариями")
public class AdsController {

    @Operation(summary = "Получить все объявления")
    @GetMapping
    public Ads getAllAds() {
        return new Ads();
    }

    @Operation(summary = "Создать новое объявление")
    @PostMapping
    public Ad addAd(@RequestPart("properties") CreateOrUpdateAd ad,
                    @RequestPart("image") MultipartFile image) {
        return new Ad();
    }

    @Operation(summary = "Получить объявление по ID")
    @GetMapping("/{id}")
    public ExtendedAd getAds(@PathVariable int id) {
        return new ExtendedAd();
    }

    @Operation(summary = "Удалить объявление по ID")
    @DeleteMapping("/{id}")
    public void removeAd(@PathVariable int id) {
    }

    @Operation(summary = "Обновить объявление по ID")
    @PatchMapping("/{id}")
    public Ad updateAds(@PathVariable int id, @RequestBody CreateOrUpdateAd ad) {
        return new Ad();
    }

    @Operation(summary = "Получить все объявления текущего пользователя")
    @GetMapping("/me")
    public Ads getAdsMe() {
        return new Ads();
    }

    @Operation(summary = "Обновить изображение объявления по ID")
    @PatchMapping("/{id}/image")
    public void updateImage(@PathVariable int id, @RequestPart("image") MultipartFile image) {
    }

    // ================= Комментарии =================

    @Operation(summary = "Получить все комментарии к объявлению")
    @GetMapping("/{id}/comments")
    public Comments getComments(@PathVariable int id) {
        return new Comments();
    }

    @Operation(summary = "Добавить комментарий к объявлению")
    @PostMapping("/{id}/comments")
    public Comment addComment(@PathVariable int id, @RequestBody CreateOrUpdateComment comment) {
        return new Comment();
    }

    @Operation(summary = "Обновить комментарий к объявлению")
    @PatchMapping("/{adId}/comments/{commentId}")
    public Comment updateComment(@PathVariable int adId, @PathVariable int commentId,
                                 @RequestBody CreateOrUpdateComment comment) {
        return new Comment();
    }

    @Operation(summary = "Удалить комментарий к объявлению")
    @DeleteMapping("/{adId}/comments/{commentId}")
    public void deleteComment(@PathVariable int adId, @PathVariable int commentId) {
    }
}
