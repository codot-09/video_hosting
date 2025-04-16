package org.example.video_hosting.repository;

import org.example.video_hosting.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ViewRepository extends JpaRepository<View,Long> {
    @Query("""
        SELECT c.category FROM views v
        JOIN Content c ON v.contentId = c.id
        WHERE v.userId = :userId
        GROUP BY c.category
        ORDER BY COUNT(v.id) DESC
        LIMIT 1
    """)
    String findTopCategoryByUserId(Long userId);

    // 2. Berilgan user eng ko‘p videolarni ko‘rgan soat
    @Query("""
        SELECT EXTRACT(HOUR FROM v.viewTime) AS hour
        FROM views v
        WHERE v.userId = :userId
        GROUP BY hour
        ORDER BY COUNT(v.id) DESC
        LIMIT 1
    """)
    Integer findPeakViewHourByUserId(Long userId);

    // 3. Berilgan user eng ko‘p ko‘rgan content egasi (uploaderId)
    @Query("""
        SELECT c.uploader.id FROM views v
        JOIN Content c ON v.contentId = c.id
        WHERE v.userId = :userId
        GROUP BY c.uploader.id
        ORDER BY COUNT(v.id) DESC
        LIMIT 1
    """)
    Long findTopUploaderViewedByUserId(Long userId);

    List<View> findViewsOlderThan7Days();

    List<View> findByUserId(Long userId);
}
