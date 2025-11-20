package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и вход пользователей")
public class AuthController {

    @Operation(summary = "Вход пользователя", description = "Позволяет авторизовать пользователя с помощью username и password")
    @PostMapping("/login")
    public Login login(@RequestBody Login login) {
        return new Login();
    }

    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя с данными из Register DTO")
    @PostMapping("/register")
    public Register register(@RequestBody Register register) {
        return new Register();
    }
}
