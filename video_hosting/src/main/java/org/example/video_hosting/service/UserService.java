package org.example.video_hosting.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.Complaint;
import org.example.video_hosting.entity.Content;
import org.example.video_hosting.entity.File;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.enums.ComplaintType;
import org.example.video_hosting.mapper.UserMapper;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.request.ReqComplaint;
import org.example.video_hosting.payload.request.UpdateProfileRequest;
import org.example.video_hosting.repository.ComplaintRepository;
import org.example.video_hosting.repository.ContentRepository;
import org.example.video_hosting.repository.FileRepository;
import org.example.video_hosting.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileRepository fileRepository;
    private final EmailService emailService;

    public ApiResponse<?> showProfile(User user){
        return ApiResponse.ok(ResponseSuccess.fetched("Foydalanuvchi ma'lumotlari"),userMapper.userToDto(user));
    }

    public ApiResponse<?> updateProfile(User user, UpdateProfileRequest updateProfileRequest){
        File image = fileRepository.findById(updateProfileRequest.newImageId()).orElse(null);
        if (image == null){
            return ApiResponse.error(ResponseError.notFound("Rasm"));
        }
        user.setUsername(updateProfileRequest.username());
        user.setBio(updateProfileRequest.bio());
        user.setPassword(passwordEncoder.encode(updateProfileRequest.password()));
        user.setProfileImage(image);

        userRepository.save(user);
        return ApiResponse.ok(ResponseSuccess.updated("Foydalanuvchi ma'lumotlari"),userMapper.userToDto(user));
    }

    public ApiResponse<?> connectEmail(User user,String email){
        Integer code = (int) ((Math.random() * 9000) + 1000);

        user.setCode(code);
        userRepository.save(user);

        emailService.sendEmail(
                email,
                "Qo'shimcha funksiyalar uchun emailingizni tasdiqlang !",
                "Tasdiqlash uchun kod: " + code
        );

        return ApiResponse.ok(ResponseSuccess.emailSent());
    }

    public ApiResponse<?> confirmEmail(User user,Integer code){
        if (user.getCode().equals(code)){
            user.setCode(null);
            userRepository.save(user);
            return ApiResponse.ok(ResponseSuccess.operationDone("Email tasdiqlandi"));
        }

        return ApiResponse.error(ResponseError.defaultError("Yaroqsiz kod"));
    }

    public ApiResponse<?> deleteProfile(User user){
        user.setActive(false);
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.deleted("Foydalanuvchi ma'lumotlari"));
    }

    public ApiResponse<?> searchUsers(String value){
        List<?> users = userRepository.findAllByUsernameContainingOrEmailContaining(value,value);
        if (users.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("Foydalanuvchilar"));
        }

        return ApiResponse.ok(ResponseSuccess.fetched("Foydalanuvchilar"),users);
    }

    public ApiResponse<?> getUserFollowers(Long userId){
        List<?> followers = userRepository.getFollowers(userId);
        if (followers.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("Obunachilar"));
        }

        return ApiResponse.ok(ResponseSuccess.fetched("Obunachilar"),followers);
    }
}
