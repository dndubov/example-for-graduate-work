package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Бизнес-логика, связанная с пользователем.
 * <p>
 * Работает с текущим авторизованным пользователем,
 * изменяет его профиль и пароль, управляет аватаром.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMappingService userMappingService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ImageService imageService; // сейчас не используем, но пусть будет

    // Путь для сохранения аватаров
    private static final String AVATAR_UPLOAD_PATH = "uploads/avatars/";

    /** Вытянуть текущего пользователя из security-контекста и найти его в БД */
    public UserEntity getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /** DTO текущего пользователя для /users/me */
    public User getCurrentUser() {
        UserEntity userEntity = getCurrentUserEntity();
        return userMappingService.toUserDto(userEntity);
    }

    /** Обновление профиля /users/me (имя, фамилия, телефон) */
    public User updateUser(UpdateUser dto) {
        UserEntity userEntity = getCurrentUserEntity();
        userMappingService.updateUserEntity(dto, userEntity);
        UserEntity saved = userRepository.save(userEntity);
        return userMappingService.toUserDto(saved);
    }

    /** Обновление аватара пользователя */
    public void updateUserImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }

        UserEntity user = getCurrentUserEntity();

        try {
            // директория хранения
            Path uploadDir = Paths.get("images", "users");
            Files.createDirectories(uploadDir);

            // уникальное имя файла
            String original = image.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" +
                    (original == null ? "avatar" : original);

            Path filePath = uploadDir.resolve(fileName);

            // сохраняем файл
            Files.write(filePath, image.getBytes());

            // относительный путь для фронта
            String relativePath = "/images/users/" + fileName;

            // сохраняем в БД
            user.setImage(relativePath);
            userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения аватара", e);
        }
    }

    /** Смена пароля текущего пользователя */
    public void setPassword(NewPassword dto) {
        UserEntity userEntity = getCurrentUserEntity();

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            currentAuth.getName(), dto.getCurrentPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Current password is incorrect");
        }

        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        userEntity.setPassword(encodedNewPassword);
        userRepository.save(userEntity);
    }

    /** Проверка: текущий пользователь — владелец или админ */
    public void checkOwnerOrAdmin(UserEntity owner) {
        UserEntity currentUser = getCurrentUserEntity();
        if (!owner.equals(currentUser) && !isAdmin()) {
            throw new RuntimeException("You do not have permission to perform this action.");
        }
    }

    /** Есть ли у текущего пользователя роль ADMIN */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
