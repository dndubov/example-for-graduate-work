package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role; // USER или ADMIN
    private String image;
}
