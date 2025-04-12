package org.example.video_hosting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalTime;

@Entity(name = "views")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentId;

    private Long userId;

    private boolean isLike;

    @CreationTimestamp
    private LocalTime viewTime;
}
