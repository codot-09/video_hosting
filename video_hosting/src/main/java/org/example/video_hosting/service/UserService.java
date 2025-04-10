package org.example.video_hosting.service;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.Complaint;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.enums.ComplaintType;
import org.example.video_hosting.mapper.UserMapper;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.request.UpdateProfileRequest;
import org.example.video_hosting.repository.ComplaintRepository;
import org.example.video_hosting.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ComplaintRepository complaintRepository;

    public ApiResponse<?> showProfile(User user){
        return ApiResponse.ok(ResponseSuccess.fetched("Foydalanuvchi ma'lumotlari"),userMapper.userToDto(user));
    }

    public ApiResponse<?> updateProfile(User user, UpdateProfileRequest updateProfileRequest){
        user.setUsername(updateProfileRequest.username());
        user.setEmail(updateProfileRequest.email());
        user.setBio(updateProfileRequest.bio());
        user.setPassword(passwordEncoder.encode(updateProfileRequest.password()));
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.updated("Foydalanuvchi ma'lumotlari"),userMapper.userToDto(user));
    }

    public ApiResponse<?> deleteProfile(User user){
        user.setActive(false);
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.deleted("Foydalanuvchi ma'lumotlari"));
    }

    public ApiResponse<?> banUser(Long userId, ComplaintType type){
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive()){
            return ApiResponse.error(ResponseError.notFound("Foydalanuvchi"));
        } else if (user.getComplaintsCount() >= 10) {
            LocalDate endDate = LocalDate.now().plusDays(7);

            user.setBanned(true);
            user.setBanEndDate(endDate);

            userRepository.save(user);
        }

        Complaint newComplaint = Complaint.builder()
                .type(type)
                .user(user)
                .build();

        complaintRepository.save(newComplaint);

        user.setComplaintsCount(user.getComplaintsCount() + 1);
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.operationDone("Shikoyat qabul qilindi"));
    }

    public ApiResponse<?> unBanUser(Long userId){
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive()){
            return ApiResponse.error(ResponseError.notFound("Foydalanuvchi"));
        }

        user.setBanned(false);
        user.setBanEndDate(null);
        user.setComplaintsCount(0);
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.operationDone("Shikoyat qabul qilindi"));
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void autoUnBan(){
        List<User> bannedUsers = userRepository.findAllByBanEndDate(LocalDate.now());

        for (User user : bannedUsers) {
            user.setBanned(false);
            user.setBanEndDate(null);
            userRepository.save(user);
        }
    }

    public ApiResponse<?> showBannedUsers(LocalDate startDate,LocalDate endDate){
        List<User> bannedUsers = userRepository.findAllByBannedTrueAndBanEndDateBetween(startDate,endDate).stream().toList();

        return ApiResponse.ok(ResponseSuccess.fetched("Bloklangan foydalanuvchilar"),bannedUsers);
    }

    public ApiResponse<?> searchUsers(String value){
        List<?> users = userRepository.findAllByUsernameContainingOrEmailContaining(value,value);

        return ApiResponse.ok(ResponseSuccess.fetched("Foydalanuvchilar"),users);
    }
}
