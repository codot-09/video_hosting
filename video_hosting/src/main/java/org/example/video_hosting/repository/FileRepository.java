package org.example.video_hosting.repository;

import org.example.video_hosting.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File,Long> {

    File findByTelegramMessageId(Integer telegramMessageId);

    Optional<File> findByTelegramFileId(String telegramFileId);
}
