package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;

/**
 * Адаптер между нашей моделью пользователя и Spring Security.
 * <p>
 * Загружает пользователя из базы и преобразует его
 * в {@link org.springframework.security.core.userdetails.UserDetails}.
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по логину (email) для процессов аутентификации.
     *
     * @param *username логин пользователя
     * @return объект с данными для Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Преобразуем роль из строки в GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        // Создаем UserDetails (предоставленный Spring Security User)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // используем email как username
                .password(user.getPassword()) // уже захешированный пароль
                .authorities(List.of(authority)) // список ролей/прав
                .build();
    }
}
