package ru.skypro.homework.model;

import javax.persistence.*;
import lombok.*;

import java.util.List;

/**
 * JPA-сущность пользователя.
 * <p>
 * Хранит учётные данные, роль и профильную информацию,
 * используемую для аутентификации и отображения в клиенте.
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;      // логин пользователя в системе

    @Column(nullable = false)
    private String password;   // хэш пароля

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    // имя файла аватарки или путь
    private String image;

    @Column(nullable = false)
    private String role; // USER / ADMIN (можно Enum позже)

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdEntity> ads;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;
}
