package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsManager implements UserDetailsManager {

    private final UserRepository userRepository;
    /**
     * для преобразования User (Spring Security) в UserEntity
     */
    private final UserMappingService userMappingService;
    private final PasswordEncoder passwordEncoder;

    @Override
    /**
     * Метод создает пользователя
     * Преобразует Spring Security User в UserEntity
     * Преобразуем роль пользователя из Authorities в роль пользователя в соответствии с моделью БД, например, ROLE_USER -> USER
     */
    public void createUser(UserDetails user) {

        UserEntity entity = new UserEntity();
        entity.setEmail(user.getUsername());
        entity.setPassword(user.getPassword());
        String role = user.getAuthorities().iterator().next().getAuthority().substring(5);
        entity.setRole(role);

        userRepository.save(entity);
    }

    @Override
    /**
     * Обновление пользователя - через updateUser в UserService
     */
    public void updateUser(UserDetails user) {

        throw new UnsupportedOperationException("Unsupported Operation");
    }

    @Override
    /**
     * Удаление пользователя по Email
     */
    public void deleteUser(String username) {
        userRepository.deleteByEmail(username);
    }

    @Override
    /**
     * Изменение пароля - через AuthenticationManager в UserService
     */
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.findByEmail(username).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .build();
    }
}
