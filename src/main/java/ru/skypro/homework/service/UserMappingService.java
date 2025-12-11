package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.UserEntity;

/**
 * Сервис сопоставления сущности пользователя с DTO.
 * <p>
 * Инкапсулирует всю логику преобразования:
 * <ul>
 *     <li>из {@link UserEntity} в {@link ru.skypro.homework.dto.User};</li>
 *     <li>из DTO обновления профиля в изменённую сущность;</li>
 *     <li>подстановку URL аватара.</li>
 * </ul>
 */

@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final UserMapper userMapper;

    // Entity → User DTO
    public User toUserDto(UserEntity entity) {
        User dto = userMapper.toDto(entity);

        // URL на аватар отдаём только если путь к файлу в БД не пустой
        if (entity.getId() != null &&
                entity.getImage() != null &&
                !entity.getImage().isBlank()) {
            dto.setImage("/users/" + entity.getId() + "/image");
        } else {
            dto.setImage(null);
        }

        return dto;
    }

    // Обновление UserEntity по UpdateUser DTO
    public void updateUserEntity(UpdateUser dto, UserEntity entity) {
        if (dto.getFirstName() != null) {
            entity.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            entity.setLastName(dto.getLastName());
        }
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
    }
}
