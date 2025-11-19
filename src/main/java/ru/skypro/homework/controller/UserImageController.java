package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users/me/image")
@RequiredArgsConstructor
@Tag(name = "Пользователь — изображение", description = "Загрузка и обновление аватара пользователя")
public class UserImageController {

    @Operation(summary = "Обновить аватар", description = "Загружает или обновляет фотографию профиля пользователя")
    @PatchMapping
    public void updateUserImage(@RequestPart("image") MultipartFile image) {
    }
}
