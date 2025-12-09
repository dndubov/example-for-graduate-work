package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.UserEntity;

@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final UserMapper userMapper;

    /**
     * Трансформация Entity → User DTO
     * @param entity
     * @return
     */
    public User toUserDto(UserEntity entity) {
        return userMapper.toDto(entity);
    }

    /**
     * Трансформация User DTO → Entity (если понадобится)
     * @param dto
     * @return
     */
    public UserEntity toUserEntity(User dto) {
        return userMapper.toEntity(dto);
    }

    /**
     * Обновление UserEntity по UpdateUser DTO
     * @param dto
     * @param entity
     */
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
