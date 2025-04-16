package org.example.video_hosting.repository;

import org.example.video_hosting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select f from User u join u.followers f where u.id = :userId")
    List<User> getFollowers(@Param("userId") Long userId);

    @Query("select u.username from User u order by u.followers.size desc limit 5")
    List<String> getTopUsersByFollowersCountLimit5();

    long countByPremiumTrue();

    long countByBannedTrue();

    List<View
}
