package org.example.video_hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.video_hosting.entity.enums.CommentStatus;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private int likes;

    @ManyToOne
    private User author;

    @ManyToOne
    private Content content;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    private boolean deleted;
}
