package org.example.video_hosting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.video_hosting.entity.enums.URole;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String phoneNumber;

    private String password;

    private String email;

    @Column(unique = true,nullable = false)
    private String username;

    private String bio;

    @CreationTimestamp
    private LocalDate created_at;

    private boolean active;

    private int complaintsCount;

    private boolean banned;

    private LocalDate banEndDate;

    private Integer code;

    private boolean isPremium;

    private LocalDate premiumExpireDate;

    @Enumerated(EnumType.STRING)
    private URole role = URole.USER;

    @OneToOne
    private File profileImage;

    @ManyToMany
    Set<User> followers;

    @ManyToMany
    private Set<Content> savedContents;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
