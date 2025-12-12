package ru.skypro.homework.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;


    @MockBean
    private AuthenticationManager authenticationManager;


    @MockBean
    private PasswordEncoder passwordEncoder;

    // --- getCurrentUser ---

    @Test
    void getCurrentUser_WithAuthenticatedUser_ReturnsUserDto() throws Exception {

        User expectedUserDto = new User(); // Инициализируйте как нужно
        expectedUserDto.setEmail("test@example.com");
        when(userService.getCurrentUser()).thenReturn(expectedUserDto);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getCurrentUser();
    }

    // --- updateUser ---

    @Test
    void updateUser_WithValidData_ReturnsUpdatedUserDto() throws Exception {

        UpdateUser updateDto = new UpdateUser();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Doe");
        updateDto.setPhone("+7 (999) 888-77-66");

        User updatedUserDto = new User(); // Инициализируйте как нужно
        updatedUserDto.setFirstName("Jane");
        updatedUserDto.setLastName("Doe");
        updatedUserDto.setPhone("+7 (999) 888-77-66");

        when(userService.updateUser(updateDto)).thenReturn(updatedUserDto);

        String jsonContent = "{ \"firstName\": \"Jane\", \"lastName\": \"Doe\", \"phone\": \"+7 (999) 888-77-66\" }";


        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(userService).updateUser(updateDto);
    }


    // --- setPassword ---

    @Test
    void setPassword_WithValidData_ReturnsOk() throws Exception {

        NewPassword newPasswordDto = new NewPassword();
        newPasswordDto.setCurrentPassword("old_password");
        newPasswordDto.setNewPassword("new_password");

        String jsonContent = "{ \"currentPassword\": \"old_password\", \"newPassword\": \"new_password\" }";

        doNothing().when(authService).setNewPassword("user@example.com", "new_password");


        when(authService.changePassword(anyString(), eq("old_password"), eq("new_password"))).thenReturn(true);


        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());

        when(authService.changePassword(anyString(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());

        verify(authService).changePassword(anyString(), eq("old_password"), eq("new_password"));
    }

    @Test
    void setPassword_WithInvalidCurrentPassword_ReturnsBadRequest() throws Exception {

        String jsonContent = "{ \"currentPassword\": \"wrong_password\", \"newPassword\": \"new_password\" }";

        when(authService.changePassword(anyString(), eq("wrong_password"), eq("new_password"))).thenReturn(false);


        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());

        verify(authService).changePassword(anyString(), eq("wrong_password"), eq("new_password"));
    }
}
