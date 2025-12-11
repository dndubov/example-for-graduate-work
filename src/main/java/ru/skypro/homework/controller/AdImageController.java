package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.service.AdService;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Ad Images", description = "Получение изображений объявлений")
public class AdImageController {

    private final AdService adService;

    @Operation(
            summary = "Получить картинку объявления по ID",
            description = "Возвращает изображение объявления в формате JPEG"
    )
    @GetMapping(
            value = "/{id}/image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getImage(@PathVariable Long id) {
        return adService.getAdImage(id);
    }
}
