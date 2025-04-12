package org.example.video_hosting.repository;

import org.example.video_hosting.entity.Complaint;
import org.example.video_hosting.entity.enums.ComplaintType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface ComplaintRepository extends JpaRepository<Complaint,Long> {

    Page<Complaint> findByCancelledAndDateAfterAndType(
            Boolean cancelled,
            LocalDate startDate,
            PageRequest pageRequest,
            ComplaintType type
    );
}
