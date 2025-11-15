package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.dto.NewPassword;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @PostMapping("/set_password")
    public void setPassword(@RequestBody NewPassword newPassword) {
    }

    @GetMapping("/me")
    public User getUser() {
        return new User();
    }

    @PatchMapping("/me")
    public UpdateUser updateUser(@RequestBody UpdateUser updateUser) {
        return new UpdateUser();
    }
}
