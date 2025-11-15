package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdsController {

    @GetMapping
    public Ads getAllAds() {
        return new Ads();
    }

    @PostMapping
    public Ad addAd(@RequestPart("properties") CreateOrUpdateAd ad,
                    @RequestPart("image") MultipartFile image) {
        return new Ad();
    }

    @GetMapping("/{id}")
    public ExtendedAd getAds(@PathVariable int id) {
        return new ExtendedAd(); // пустой объект
    }

    @DeleteMapping("/{id}")
    public void removeAd(@PathVariable int id) {
    }

    @PatchMapping("/{id}")
    public Ad updateAds(@PathVariable int id, @RequestBody CreateOrUpdateAd ad) {
        return new Ad();
    }

    @GetMapping("/me")
    public Ads getAdsMe() {
        return new Ads();
    }

    @PatchMapping("/{id}/image")
    public void updateImage(@PathVariable int id, @RequestPart("image") MultipartFile image) {
    }

    // ================= Комментарии =================

    @GetMapping("/{id}/comments")
    public Comments getComments(@PathVariable int id) {
        return new Comments();
    }

    @PostMapping("/{id}/comments")
    public Comment addComment(@PathVariable int id, @RequestBody CreateOrUpdateComment comment) {
        return new Comment();
    }

    @PatchMapping("/{adId}/comments/{commentId}")
    public Comment updateComment(@PathVariable int adId, @PathVariable int commentId,
                                 @RequestBody CreateOrUpdateComment comment) {
        return new Comment();
    }

    @DeleteMapping("/{adId}/comments/{commentId}")
    public void deleteComment(@PathVariable int adId, @PathVariable int commentId) {
    }
}
