package org.example.telegrambot.repository;

import org.example.telegrambot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByChatId(String chatId);

    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findByRole();

    long count();
}
