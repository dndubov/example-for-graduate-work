package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;

@CrossOrigin("http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и вход пользователей")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Вход пользователя",
            description = "Позволяет авторизовать пользователя с помощью username и password")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        boolean success = authService.login(login.getUsername(), login.getPassword());
        if (success) {
            return ResponseEntity.ok().build();                    // 200 OK
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
    }

    @Operation(summary = "Регистрация пользователя",
            description = "Создает нового пользователя с данными из Register DTO")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Register register) {
        boolean success = authService.register(register);
        if (success) {
            return ResponseEntity.ok().build();                    // 200 OK
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409, если такой пользователь уже есть
    }
}
