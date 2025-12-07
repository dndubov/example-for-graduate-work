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
    private final UserMappingService userMappingService; // для преобразования User (Spring Security) в UserEntity
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createUser(UserDetails user) {
        // Преобразуем Spring Security User в UserEntity
        UserEntity entity = new UserEntity();
        entity.setEmail(user.getUsername());
        entity.setPassword(user.getPassword());
        // Извлекаем роль из authorities, например, ROLE_USER -> USER
        String role = user.getAuthorities().iterator().next().getAuthority().substring(5); // "ROLE_" prefix
        entity.setRole(role);

        userRepository.save(entity);
    }

    @Override
    public void updateUser(UserDetails user) {
        // Аналогично createUser
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteUser(String username) {
        userRepository.deleteByEmail(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // Изменение пароля через AuthenticationManager в UserService
        throw new UnsupportedOperationException("Not implemented");
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
