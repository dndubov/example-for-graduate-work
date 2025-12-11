package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.service.ImageService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Изображения", description = "Получение сохранённых изображений")
public class ImageController {

    private final ImageService imageService;

    @Operation(
            summary = "Получить изображение по пути",
            description = "Возвращает файл изображения по названию папки и имени файла"
    )
    @GetMapping(
            value = "/images/{folder}/{fileName}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/*"}
    )
    public byte[] getImage(@PathVariable String folder,
                           @PathVariable String fileName) {
        return imageService.load(folder, fileName);
    }
}
