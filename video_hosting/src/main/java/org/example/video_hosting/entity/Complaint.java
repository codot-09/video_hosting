package org.example.video_hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.video_hosting.entity.enums.ComplaintType;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintType type;

    @ManyToOne
    private User user;

    private boolean cancelled;

    private LocalDate date;
}
