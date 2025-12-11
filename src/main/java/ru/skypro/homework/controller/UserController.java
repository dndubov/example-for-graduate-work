package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Контроллер для работы с профилем пользователя.
 * <p>
 * Содержит операции:
 * <ul>
 *     <li>получение данных текущего пользователя;</li>
 *     <li>обновление профиля;</li>
 *     <li>смена пароля;</li>
 *     <li>загрузка и получение аватара.</li>
 * </ul>
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
@Tag(name = "Пользователи", description = "Получение и обновление информации о пользователях")
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository; // добавили репозиторий

    /**
     * Изменяет пароль текущего пользователя при знании старого пароля.
     *
     * @param dto объект, содержащий старый и новый пароли
     * @return статус успешной или неуспешной смены пароля
     */

    @Operation(summary = "Сменить пароль пользователя")
    @PostMapping("/set_password")
    public ResponseEntity<?> setPassword(@RequestBody NewPassword dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean success = authService.changePassword(username,
                dto.getCurrentPassword(),
                dto.getNewPassword());

        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Возвращает данные текущего авторизованного пользователя.
     *
     * @return DTO с основной информацией о пользователе
     */

    @Operation(summary = "Получить информацию о текущем пользователе")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * Обновляет имя, фамилию и телефон текущего пользователя.
     *
     * @param dto объект с новыми значениями полей профиля
     * @return обновлённые данные пользователя
     */

    @Operation(summary = "Обновить данные текущего пользователя")
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateUser(@RequestBody UpdateUser dto) {
        User updatedUser = userService.updateUser(dto);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping(
            value = "/me/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateUserImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            System.out.println("updateUserImage: файл не получен или пустой");
            return ResponseEntity.badRequest().build();
        }

        System.out.println("updateUserImage: получен файл, size = " + file.getSize());
        userService.updateUserImage(file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить аватар пользователя по id")
    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
        // достаём пользователя
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String imagePath = user.getImage();
        if (imagePath == null || imagePath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // в БД путь хранится вроде "/images/users/....jpg"
            Path filePath = Paths.get(
                    imagePath.startsWith("/") ? imagePath.substring(1) : imagePath
            );

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = Files.readAllBytes(filePath);

            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = contentType != null
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity
                    .ok()
                    .contentType(mediaType)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
