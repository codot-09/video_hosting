package org.example.video_hosting.repository;

import org.example.video_hosting.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByPhoneNumber(String phoneNumber);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhoneNumberOrUsername(String phoneNumber,String username);

    List<User> findAllByBanEndDate(LocalDate banEndDate);

    List<User> findAllByBannedTrueAndBanEndDateBetween(LocalDate startDate,LocalDate endDate);

    List<User> findAllByUsernameContainingOrEmailContaining(String value,String value2);
}
