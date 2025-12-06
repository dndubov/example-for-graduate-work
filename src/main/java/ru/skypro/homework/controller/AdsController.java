package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdService;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Объявления", description = "CRUD операции с объявлениями и комментариями")
public class AdsController {

    @Operation(summary = "Получить все объявления")
    @GetMapping
    public ResponseEntity<Ads> getAllAds() {
        Ads ads = AdService.getAllAds();
        return ResponseEntity.ok(ads);
    }

    @Operation(summary = "Создать новое объявление")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> createAd(
            @RequestPart("properties") CreateOrUpdateAd dto,
            @RequestPart("image") MultipartFile image) {
        Ad ad = AdService.createAd(dto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ad);
    }

    @Operation(summary = "Получить объявление по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAd> getAd(@PathVariable Long id) {
        ExtendedAd ad = AdService.getAdById(id);
        return ResponseEntity.ok(ad);
    }

    @Operation(summary = "Удалить объявление по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id) {
        AdService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Обновить объявление по ID")
    @PatchMapping("/{id}")
    public ResponseEntity<Ad> updateAd(@PathVariable Long id, @RequestBody CreateOrUpdateAd dto) {
        Ad ad = AdService.updateAd(id, dto);
        return ResponseEntity.ok(ad);
    }

    @Operation(summary = "Получить все объявления текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<Ads> getMyAds() {
        Ads ads = AdService.getMyAds();
        return ResponseEntity.ok(ads);
    }

    @Operation(summary = "Обновить изображение объявления по ID")
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAdImage(@PathVariable Long id, @RequestPart("image") MultipartFile image) {
        AdService.updateAdImage(id, image);
        return ResponseEntity.ok().build();
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
