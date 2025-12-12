package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Получение и обновление информации о пользователях")
/**
 * Определяет методы работы с данными пользователя
 */
public class UserController {

    private static AuthService authService;

    @Operation(summary = "Сменить пароль пользователя")
    @PostMapping("/set_password")
    /**
     * Метод setPassword устанавливает пароль пользователя
     * Получаем имя текущего пользователя (email) из SecurityContext
     * Вызываем метод AuthService для изменения пароля
     * Возвращаем 400 Bad Request, если текущий пароль неверен
     */
    public ResponseEntity<?> setPassword(@RequestBody NewPassword dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean success = authService.changePassword(username, dto.getCurrentPassword(), dto.getNewPassword());

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получить информацию о текущем пользователе")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public static ResponseEntity<User> getCurrentUser() {
        User user = UserService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Обновить данные текущего пользователя")
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public static ResponseEntity<User> updateUser(@RequestBody UpdateUser dto) {
        User updatedUser = UserService.updateUser(dto);
        return ResponseEntity.ok(updatedUser);
    }
    @PatchMapping("/me/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserImage(@RequestParam("image") MultipartFile image) {
        UserService.updateUserImage(image);
        return ResponseEntity.ok().build();
    }
}
