package org.example.video_hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.video_hosting.entity.enums.ContentAuditory;
import org.example.video_hosting.entity.enums.ContentCategory;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "contents")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private ContentAuditory status;

    @ManyToOne
    private User uploader;

    private boolean banned;

    @CreationTimestamp
    private LocalDateTime upload_at;

    private Long messageId;

    private boolean boosted;

    @OneToOne
    private File thumbnail;

    @OneToOne
    private File contentFile;

    @ElementCollection
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    private ContentCategory category;
}
