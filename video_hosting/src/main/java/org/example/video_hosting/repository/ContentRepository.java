package org.example.video_hosting.repository;

import org.example.video_hosting.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content,Long> {

    @Query("""
        SELECT c.category FROM Content c
        GROUP BY c.category
        ORDER BY COUNT(c.id) DESC
        LIMIT 5
    """)
    List<String> topCategories();
}
