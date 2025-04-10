package org.example.video_hosting.repository;

import org.example.video_hosting.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content,Long> {
}
