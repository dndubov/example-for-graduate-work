package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMappingService userMappingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private UserService userService;

    private UserEntity testUser;
    private User testUserDto;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMappingService, passwordEncoder, authenticationManager);

        testUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .phone("+7 (999) 123-45-67")
                .image("uploads/avatars/test_avatar.jpg")
                .role("USER")
                .build();

        testUserDto = new User();
        testUserDto.setId(1);
        testUserDto.setEmail("test@example.com");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setPhone("+7 (999) 123-45-67");
        testUserDto.setImage("uploads/avatars/test_avatar.jpg");
        testUserDto.setRole("USER");
    }

    // --- getCurrentUser ---

    @Test
    void getCurrentUser_WithAuthenticatedUser_ReturnsUserDto() {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMappingService.toUserDto(testUser)).thenReturn(testUserDto);


        User result = userService.getCurrentUser();


        assertEquals(testUserDto, result);
        verify(userRepository).findByEmail("test@example.com");
        verify(userMappingService).toUserDto(testUser);
    }

    @Test
    void getCurrentUser_WithNonExistentUser_ThrowsRuntimeException() {

        Authentication auth = new TestingAuthenticationToken("nonexistent@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.getCurrentUser());
        assertEquals("User not found", thrown.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    // --- updateUser ---

    @Test
    void updateUser_WithValidData_UpdatesAndReturnsUserDto() {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateUser updateDto = new UpdateUser();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setPhone("+7 (888) 765-43-21");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(userMappingService).updateUserEntity(updateDto, testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMappingService.toUserDto(testUser)).thenReturn(testUserDto);


        User result = userService.updateUser(updateDto);


        assertEquals("Jane", testUser.getFirstName());
        assertEquals("Smith", testUser.getLastName());
        assertEquals("+7 (888) 765-43-21", testUser.getPhone());
        assertEquals(testUserDto, result);
        verify(userRepository).save(testUser);
        verify(userMappingService).toUserDto(testUser);
    }

    @Test
    void updateUser_WithNonExistentUser_ThrowsRuntimeException() {

        Authentication auth = new TestingAuthenticationToken("nonexistent@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateUser updateDto = new UpdateUser();
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.updateUser(updateDto));
        assertEquals("User not found", thrown.getMessage());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    // --- updateUserImage ---

    @Test
    void updateUserImage_WithValidImage_SavesAndUpdatesUserEntity() throws IOException {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String uploadDir = "test_uploads";
        String oldImagePath = "uploads/avatars/test_avatar.jpg";
        String newImageName = "new_avatar.jpg";
        byte[] imageContent = "fake_image_content".getBytes(); // Просто байты для теста
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));


        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        userService.avatarUploadPath = uploadDir; // Изменяем путь для теста
        assertDoesNotThrow(() -> userService.updateUserImage(mockFile));


        ArgumentCaptor<UserEntity> userEntityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userEntityCaptor.capture());
        UserEntity savedUser = userEntityCaptor.getValue();
        assertTrue(savedUser.getImage().contains(uploadDir)); // Проверяем, что путь содержит правильную директорию
        assertTrue(savedUser.getImage().contains(newImageName)); // Проверяем, что путь содержит новое имя файла


        Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) { /* ignore */ }
                });
        Files.delete(uploadPath);
    }

    @Test
    void updateUserImage_WithIOException_ThrowsRuntimeException() throws IOException {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String newImageName = "new_avatar.jpg";
        byte[] imageContent = "fake_image_content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("image", newImageName, "image/jpeg", imageContent);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    // --- setPassword ---

    @Test
    void setPassword_WithValidCurrentPassword_UpdatesPassword() {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        NewPassword newPasswordDto = new NewPassword();
        newPasswordDto.setCurrentPassword("current_plain_password");
        newPasswordDto.setNewPassword("new_plain_password");

        String encodedNewPassword = "encoded_new_password";
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("new_plain_password")).thenReturn(encodedNewPassword);
        doNothing().when(authenticationManager).authenticate(any());


        assertDoesNotThrow(() -> userService.setPassword(newPasswordDto));

        assertEquals(encodedNewPassword, testUser.getPassword());
        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode("new_plain_password");
    }

    @Test
    void setPassword_WithInvalidCurrentPassword_ThrowsRuntimeException() {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        NewPassword newPasswordDto = new NewPassword();
        newPasswordDto.setCurrentPassword("wrong_password");
        newPasswordDto.setNewPassword("new_plain_password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.setPassword(newPasswordDto));
        assertEquals("Current password is incorrect", thrown.getMessage());

        assertEquals("encoded_password", testUser.getPassword());
        verify(userRepository, never()).save(any());
    }

    @Test
    void setPassword_WithNonExistentUser_ThrowsRuntimeException() {

        Authentication auth = new TestingAuthenticationToken("nonexistent@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        NewPassword newPasswordDto = new NewPassword();
        newPasswordDto.setCurrentPassword("current_password");
        newPasswordDto.setNewPassword("new_plain_password");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());


        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.setPassword(newPasswordDto));
        assertEquals("User not found", thrown.getMessage());
    }

    // --- checkOwnerOrAdmin ---

    @Test
    void checkOwnerOrAdmin_WithCurrentUserAsOwner_DoesNotThrow() {

        Authentication auth = new TestingAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));


        assertDoesNotThrow(() -> userService.checkOwnerOrAdmin(testUser));
    }

    @Test
    void checkOwnerOrAdmin_WithCurrentUserAsAdmin_DoesNotThrow() {

        Authentication auth = new TestingAuthenticationToken("admin@example.com", null, "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserEntity otherUser = UserEntity.builder().id(2L).email("other@example.com").build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(UserEntity.builder().id(99L).email("admin@example.com").role("ADMIN").build()));

        assertDoesNotThrow(() -> userService.checkOwnerOrAdmin(otherUser));
    }

    @Test
    void checkOwnerOrAdmin_WithDifferentUserAndNotAdmin_ThrowsRuntimeException() {

        Authentication auth = new TestingAuthenticationToken("user1@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserEntity user1 = UserEntity.builder().id(1L).email("user1@example.com").build();
        UserEntity user2 = UserEntity.builder().id(2L).email("user2@example.com").build();

        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.checkOwnerOrAdmin(user2));
        assertEquals("You do not have permission to perform this action.", thrown.getMessage());
    }
}