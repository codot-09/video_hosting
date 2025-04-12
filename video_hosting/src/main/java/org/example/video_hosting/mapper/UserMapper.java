package org.example.video_hosting.mapper;


import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.payload.UserDto;
import org.example.video_hosting.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final UserRepository userRepository;

    public UserDto userToDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .email(user.getEmail())
                .followers(userRepository.getFollowers(user.getId()).size())
                .isPremium(user.isPremium())
                .profileImage(user.getProfileImage() != null ? user.getProfileImage().getId() : null)
                .build();
    }
}
