package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Получение и обновление информации о пользователях")
public class UserController {

    @Operation(summary = "Сменить пароль пользователя")
    @PostMapping("/set_password")
    public void setPassword(@RequestBody NewPassword newPassword) {
    }

    @Operation(summary = "Получить информацию о текущем пользователе")
    @GetMapping("/me")
    public User getUser() {
        return new User();
    }

    @Operation(summary = "Обновить данные текущего пользователя")
    @PatchMapping("/me")
    public UpdateUser updateUser(@RequestBody UpdateUser updateUser) {
        return new UpdateUser();
    }
}
