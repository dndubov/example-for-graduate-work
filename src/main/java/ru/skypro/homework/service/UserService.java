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

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static UserRepository userRepository;
    private static UserMappingService userMappingService;
    private static PasswordEncoder passwordEncoder;
    private static AuthenticationManager authenticationManager; // для проверки текущего пароля

    /**
     * Устанавливает путь для сохранения аватаров
     */
    static String avatarUploadPath = "uploads/avatars/";

    public UserService(UserRepository userRepository, UserMappingService userMappingService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
    }

    /**
     * Метод для получения текущего пользователя из контекста аутентификации
     * @return
     */
    static UserEntity getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // имя пользователя (email)
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static User getCurrentUser() {
        UserEntity userEntity = getCurrentUserEntity();
        return userMappingService.toUserDto(userEntity);
    }

    public static User updateUser(UpdateUser dto) {
        UserEntity userEntity = getCurrentUserEntity();
        userMappingService.updateUserEntity(dto, userEntity);
        UserEntity saved = userRepository.save(userEntity);
        return userMappingService.toUserDto(saved);
    }

    public static void updateUserImage(MultipartFile image) {
        UserEntity userEntity = getCurrentUserEntity();

        /**
         * Генерация уникального имени файла (например, с UUID)
         * Создает директорию, если её нет
         * Сохраняет файл
         * Удаляет старое изображение, если оно было
         * Логирует ошибку, если старого изображения нет, но не прерывает выполнение
         * Обновляет путь к изображению в сущности
         */
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(avatarUploadPath).resolve(fileName);

        try {

            Files.createDirectories(Paths.get(avatarUploadPath));

            Files.write(filePath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar image", e);
        }

        if (userEntity.getImage() != null) {
            Path oldImagePath = Paths.get(userEntity.getImage());
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {

                System.err.println("Could not delete old avatar: " + e.getMessage());
            }
        }

        userEntity.setImage(filePath.toString());
        userRepository.save(userEntity);
    }

    /**
     * Сервис установки текущего пароля
     * Проверить текущий пароль
     * Закодировать и установить новый пароль
     * @param dto
     */
    public static void setPassword(NewPassword dto) {
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

    /**
     * Метод проверяет наличие прав владельца объявления/комментария или прав админа
     * @param owner
     */
    public void checkOwnerOrAdmin(UserEntity owner) {
        UserEntity currentUser = getCurrentUserEntity();
        if (!owner.equals(currentUser) && !isAdmin()) {
            throw new RuntimeException("You do not have permission to perform this action.");
        }
    }

    /**
     * Метод проверки наличия прав администратора
     * @return
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
