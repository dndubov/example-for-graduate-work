package ru.skypro.homework.service.impl;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.CustomUserDetailsManager;

/**
 * Реализует логику регистрации и аутентификации пользователей.
 * <p>
 * Создаёт новых пользователей в системе Spring Security и
 * проверяет корректность логина/пароля при входе.
 */

@Service
public class AuthServiceImpl implements AuthService {

    private final CustomUserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    public AuthServiceImpl(CustomUserDetailsManager manager,
                           PasswordEncoder encoder,
                           UserRepository userRepository) {
        this.manager = manager;
        this.encoder = encoder;
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String userName, String password) {
        if (!manager.userExists(userName)) {
            return false;
        }

        UserDetails userDetails = manager.loadUserByUsername(userName);
        return encoder.matches(password, userDetails.getPassword());
    }

    /**
     * Регистрирует нового пользователя, если логин ещё не занят.
     *
     * @param register DTO с данными для регистрации
     * @return {@code true}, если пользователь успешно создан
     */

    @Override
    public boolean register(Register register) {
        System.out.println(">>> REGISTER CALLED");

        // Проверяем, что такой email уже есть в нашей БД
        if (userRepository.findByEmail(register.getUsername()).isPresent()) {
            return false;
        }

        // Проверяем, что такого пользователя ещё нет в security-хранилище
        if (manager.userExists(register.getUsername())) {
            return false;
        }

        // Создаём запись в таблице users
        UserEntity entity = new UserEntity();
        entity.setEmail(register.getUsername());
        entity.setPassword(encoder.encode(register.getPassword()));
        entity.setFirstName(register.getFirstName());
        entity.setLastName(register.getLastName());
        entity.setPhone(register.getPhone());
        entity.setRole("USER");

        userRepository.save(entity);

        // Создаём пользователя в Spring Security
        manager.createUser(
                User.builder()
                        .passwordEncoder(encoder::encode)
                        .password(register.getPassword())
                        .username(register.getUsername())
                        .roles("USER")
                        .build());

        return true;
    }

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        if (!manager.userExists(username)) {
            return false;
        }

        UserDetails userDetails = manager.loadUserByUsername(username);

        // Проверка старого пароля
        if (!encoder.matches(currentPassword, userDetails.getPassword())) {
            return false;
        }

        // Обновление пароля в security-хранилище
        UserDetails updated = User.builder()
                .username(username)
                .password(encoder.encode(newPassword))
                .authorities(userDetails.getAuthorities())
                .build();

        manager.updateUser(updated);

        // Обновляем пароль в нашей БД
        userRepository.findByEmail(username).ifPresent(entity -> {
            entity.setPassword(encoder.encode(newPassword));
            userRepository.save(entity);
        });

        return true;
    }

    @Override
    public boolean setNewPassword(String email, String newPassword) {

        if (!manager.userExists(email)) {
            return false;
        }

        UserDetails userDetails = manager.loadUserByUsername(email);

        UserDetails updated = User.builder()
                .username(email)
                .password(encoder.encode(newPassword))
                .authorities(userDetails.getAuthorities())
                .build();

        manager.updateUser(updated);

        // Обновляем пароль в нашей БД
        userRepository.findByEmail(email).ifPresent(entity -> {
            entity.setPassword(encoder.encode(newPassword));
            userRepository.save(entity);
        });

        return true;
    }
}
