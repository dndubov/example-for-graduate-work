package ru.skypro.homework.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;

@CrossOrigin("http://localhost:3000")
@RestController
public class AuthController {

    @PostMapping("/login")
    public Login login(@RequestBody Login login) {
        return new Login();
    }

    @PostMapping("/register")
    public Register register(@RequestBody Register register) {
        return new Register();
    }
}
