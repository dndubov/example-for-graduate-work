package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.service.impl.AuthServiceImpl;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserDetailsManager manager;

    @Mock
    private PasswordEncoder encoder;

    private AuthServiceImpl authService;

    private Register registerDto;
    private NewPassword newPasswordDto;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl((CustomUserDetailsManager) manager, encoder);

        registerDto = new Register();
        registerDto.setUsername("newuser@example.com");
        registerDto.setPassword("plain_password");
        registerDto.setFirstName("New");
        registerDto.setLastName("User");
        registerDto.setPhone("+7 (111) 111-11-11");
        registerDto.setRole(Role.USER);

        newPasswordDto = new NewPassword();
        newPasswordDto.setCurrentPassword("current_plain_password");
        newPasswordDto.setNewPassword("new_plain_password");
    }

    // --- register ---

    @Test
    void register_WithNewUser_CreatesUser() {

        String encodedPassword = "encoded_password";
        when(manager.userExists("newuser@example.com")).thenReturn(false);
        when(encoder.encode("plain_password")).thenReturn(encodedPassword);
        doNothing().when(manager).createUser(any(org.springframework.security.core.userdetails.User.class));


        boolean result = authService.register(registerDto);


        assertTrue(result);
        verify(manager).userExists("newuser@example.com");
        verify(encoder).encode("plain_password");
        verify(manager).createUser(any(org.springframework.security.core.userdetails.User.class));
    }

    @Test
    void register_WithExistingUser_ReturnsFalse() {

        when(manager.userExists("newuser@example.com")).thenReturn(true);


        boolean result = authService.register(registerDto);


        assertFalse(result);
        verify(manager).userExists("newuser@example.com");
        verify(encoder, never()).encode(anyString()); // Кодирование не должно происходить
        verify(manager, never()).createUser(any()); // Создание не должно происходить
    }

    // --- login ---

    @Test
    void login_WithValidCredentials_ReturnsTrue() {

        String plainPassword = "plain_password";
        String encodedPassword = "encoded_password";
        UserDetails userDetails = User.builder()
                .username("existing@example.com")
                .password(encodedPassword)
                .authorities("ROLE_USER")
                .build();

        when(manager.userExists("existing@example.com")).thenReturn(true);
        when(manager.loadUserByUsername("existing@example.com")).thenReturn(userDetails);
        when(encoder.matches(plainPassword, encodedPassword)).thenReturn(true);


        boolean result = authService.login("existing@example.com", plainPassword);


        assertTrue(result);
        verify(manager).userExists("existing@example.com");
        verify(manager).loadUserByUsername("existing@example.com");
        verify(encoder).matches(plainPassword, encodedPassword);
    }

    @Test
    void login_WithNonExistentUser_ReturnsFalse() {

        when(manager.userExists("nonexistent@example.com")).thenReturn(false);


        boolean result = authService.login("nonexistent@example.com", "some_password");


        assertFalse(result);
        verify(manager).userExists("nonexistent@example.com");
        verify(manager, never()).loadUserByUsername(anyString()); // Загрузка не должна происходить
        verify(encoder, never()).matches(anyString(), anyString()); // Сравнение не должно происходить
    }

    @Test
    void login_WithInvalidPassword_ReturnsFalse() {

        String plainPassword = "wrong_password";
        String encodedPassword = "encoded_password";
        UserDetails userDetails = User.builder()
                .username("existing@example.com")
                .password(encodedPassword)
                .authorities("ROLE_USER")
                .build();

        when(manager.userExists("existing@example.com")).thenReturn(true);
        when(manager.loadUserByUsername("existing@example.com")).thenReturn(userDetails);
        when(encoder.matches(plainPassword, encodedPassword)).thenReturn(false);


        boolean result = authService.login("existing@example.com", plainPassword);

        assertFalse(result);
        verify(manager).userExists("existing@example.com");
        verify(manager).loadUserByUsername("existing@example.com");
        verify(encoder).matches(plainPassword, encodedPassword);
    }

    // --- setNewPassword ---

    @Test
    void setNewPassword_WithExistingUser_UpdatesPassword() {

        String email = "user@example.com";
        String newPasswordPlain = "new_plain_password";
        String newPasswordEncoded = "new_encoded_password";

        UserDetails existingUserDetails = User.builder()
                .username(email)
                .password("old_encoded_password")
                .authorities("ROLE_USER")
                .build();

        UserDetails expectedUpdatedUserDetails = User.builder()
                .username(email)
                .password(newPasswordEncoded)
                .authorities("ROLE_USER")
                .build();

        when(manager.userExists(email)).thenReturn(true);
        when(manager.loadUserByUsername(email)).thenReturn(existingUserDetails);
        when(encoder.encode(newPasswordPlain)).thenReturn(newPasswordEncoded);
        doNothing().when(manager).updateUser(expectedUpdatedUserDetails);

        boolean result = authService.setNewPassword(email, newPasswordPlain);


        assertTrue(result);
        verify(manager).userExists(email);
        verify(manager).loadUserByUsername(email);
        verify(encoder).encode(newPasswordPlain);
        verify(manager).updateUser(expectedUpdatedUserDetails);
    }

    @Test
    void setNewPassword_WithNonExistentUser_ReturnsFalse() {

        when(manager.userExists("nonexistent@example.com")).thenReturn(false);


        boolean result = authService.setNewPassword("nonexistent@example.com", "new_password");

        assertFalse(result);
        verify(manager).userExists("nonexistent@example.com");
        verify(manager, never()).loadUserByUsername(anyString()); // Загрузка не должна происходить
        verify(encoder, never()).encode(anyString()); // Кодирование не должно происходить
        verify(manager, never()).updateUser(any()); // Обновление не должно происходить
    }

    // --- changePassword ---

    @Test
    void changePassword_WithValidCurrentPassword_UpdatesPassword() {

        String email = "user@example.com";
        String currentPasswordPlain = "current_plain_password";
        String currentPasswordEncoded = "current_encoded_password";
        String newPasswordPlain = "new_plain_password";
        String newPasswordEncoded = "new_encoded_password";

        UserDetails existingUserDetails = User.builder()
                .username(email)
                .password(currentPasswordEncoded)
                .authorities("ROLE_USER")
                .build();

        UserDetails expectedUpdatedUserDetails = User.builder()
                .username(email)
                .password(newPasswordEncoded)
                .authorities("ROLE_USER")
                .build();

        when(manager.userExists(email)).thenReturn(true);
        when(manager.loadUserByUsername(email)).thenReturn(existingUserDetails);
        when(encoder.matches(currentPasswordPlain, currentPasswordEncoded)).thenReturn(true);
        when(encoder.encode(newPasswordPlain)).thenReturn(newPasswordEncoded);
        doNothing().when(manager).updateUser(expectedUpdatedUserDetails);


        boolean result = authService.changePassword(email, currentPasswordPlain, newPasswordPlain);


        assertTrue(result);
        verify(manager).userExists(email);
        verify(manager).loadUserByUsername(email);
        verify(encoder).matches(currentPasswordPlain, currentPasswordEncoded);
        verify(encoder).encode(newPasswordPlain);
        verify(manager).updateUser(expectedUpdatedUserDetails);
    }

    @Test
    void changePassword_WithInvalidCurrentPassword_ReturnsFalse() {

        String email = "user@example.com";
        String currentPasswordPlain = "wrong_current_password";
        String currentPasswordEncoded = "current_encoded_password";
        String newPasswordPlain = "new_plain_password";

        UserDetails existingUserDetails = User.builder()
                .username(email)
                .password(currentPasswordEncoded)
                .authorities("ROLE_USER")
                .build();

        when(manager.userExists(email)).thenReturn(true);
        when(manager.loadUserByUsername(email)).thenReturn(existingUserDetails);
        when(encoder.matches(currentPasswordPlain, currentPasswordEncoded)).thenReturn(false);


        boolean result = authService.changePassword(email, currentPasswordPlain, newPasswordPlain);


        assertFalse(result);
        verify(manager).userExists(email);
        verify(manager).loadUserByUsername(email);
        verify(encoder).matches(currentPasswordPlain, currentPasswordEncoded);
        verify(encoder, never()).encode(newPasswordPlain); // Новый пароль не должен кодироваться
        verify(manager, never()).updateUser(any()); // Обновление не должно происходить
    }

    @Test
    void changePassword_WithNonExistentUser_ReturnsFalse() {

        when(manager.userExists("nonexistent@example.com")).thenReturn(false);


        boolean result = authService.changePassword("nonexistent@example.com", "current", "new");


        assertFalse(result);
        verify(manager).userExists("nonexistent@example.com");
        verify(manager, never()).loadUserByUsername(anyString()); // Загрузка не должна происходить
        verify(encoder, never()).matches(anyString(), anyString()); // Сравнение не должно происходить
        verify(encoder, never()).encode(anyString()); // Кодирование не должно происходить
        verify(manager, never()).updateUser(any()); // Обновление не должно происходить
    }
}
