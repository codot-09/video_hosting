package org.example.video_hosting.mapper;


import org.example.video_hosting.entity.User;
import org.example.video_hosting.payload.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto userToDto(User user){
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .email(user.getEmail())
                .followers(user.getFollowers().size())
                .isPremium(user.isPremium())
                .profileImage(user.getProfileImage().getId())
                .build();
    }
}
