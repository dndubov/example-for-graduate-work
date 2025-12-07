package ru.skypro.homework.service.impl;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.CustomUserDetailsManager;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {


    private final CustomUserDetailsManager manager;
    private final PasswordEncoder encoder;

    public AuthServiceImpl(CustomUserDetailsManager manager,
                           PasswordEncoder passwordEncoder) {
        this.manager = manager;
        this.encoder = passwordEncoder;
    }

    @Override
    public boolean login(String userName, String password) {
        if (!manager.userExists(userName)) {
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        return encoder.matches(password, userDetails.getPassword());
    }

    @Override
    public boolean register(Register register) {
        if (manager.userExists(register.getUsername())) {
            return false;
        }
        manager.createUser(
                User.builder()
                        .passwordEncoder(this.encoder::encode)
                        .password(register.getPassword())
                        .username(register.getUsername())
                        .roles(register.getRole().name())
                        .build());
        return true;
    }

    @Override
    public boolean changePassword(String name, String currentPassword, String newPassword) {
        if (!manager.userExists(name)) {
            return false; // Пользователь не найден
        }

        // Загружаем данные пользователя
        UserDetails userDetails = manager.loadUserByUsername(name);

        // Проверяем, совпадает ли текущий пароль с хешированным в БД
        if (!encoder.matches(currentPassword, userDetails.getPassword())) {
            return false; // Текущий пароль неверен
        }

        // Если пароль верен, обновляем его на новый
        // Создаем обновленный объект UserDetails
        UserDetails updatedUserDetails = User.builder()
                .username(userDetails.getUsername())
                .password(encoder.encode(newPassword)) // Хешируем новый пароль
                .authorities(userDetails.getAuthorities()) // Сохраняем старые права
                .build();

        // Обновляем пользователя в хранилище
        manager.updateUser(updatedUserDetails);

        return true; // Пароль успешно изменён
    }

    @Override
    public boolean setNewPassword(String email, String newPassword) {
        if (!manager.userExists(email)) {
            return false; // Пользователь не найден
        }

        // Загружаем текущие данные пользователя
        UserDetails userDetails = manager.loadUserByUsername(email);

        // Создаем обновленный объект UserDetails с новым паролем
        // Все остальные поля (username, authorities) остаются прежними
        UserDetails updatedUserDetails = User.builder()
                .username(userDetails.getUsername())
                .password(encoder.encode(newPassword)) // Хешируем новый пароль
                .authorities(userDetails.getAuthorities()) // Сохраняем старые права
                .build();

        // Обновляем пользователя в хранилище
        manager.updateUser(updatedUserDetails);

        return true;
    }


}
