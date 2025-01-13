package org.example.telegrambot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.telegrambot.entity.enums.Role;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(nullable = false,unique = true)
    private String chatId;
    @Enumerated(EnumType.STRING)
    private Role role;
}
