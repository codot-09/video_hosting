package org.example.video_hosting.repository;

import org.example.video_hosting.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface ComplaintRepository extends JpaRepository<Complaint,Long> {

    Page<Complaint> findByCancelledAndDateAfter(
            Boolean cancelled,
            LocalDate startDate,
            PageRequest pageRequest
    );
}
