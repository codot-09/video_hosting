package org.example.video_hosting.service;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.enums.URole;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.request.AuthRequest;
import org.example.video_hosting.payload.response.LoginResponse;
import org.example.video_hosting.repository.UserRepository;
import org.example.video_hosting.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<?> register(AuthRequest authRequest) {
        Optional<User> user = userRepository.findByUsername(authRequest.username());
        User user1 = userRepository.findByPhoneNumber(authRequest.phoneNumber());
        if (user.isPresent()) {
            return ApiResponse.error(ResponseError.alreadyExists(authRequest.username()));
        } else if (user1 != null) {
            return ApiResponse.error(ResponseError.alreadyExists(authRequest.phoneNumber()));
        }

        User user2 = User.builder()
                .phoneNumber(authRequest.phoneNumber())
                .username(authRequest.username())
                .password(passwordEncoder.encode(authRequest.password()))
                .active(true)
                .role(URole.USER)
                .build();

        userRepository.save(user2);
        return ApiResponse.ok(ResponseSuccess.registered());
    }

    public ApiResponse<?> login(AuthRequest authRequest){
        Optional<User> user = userRepository.findByPhoneNumberOrUsername(authRequest.phoneNumber(), authRequest.username());
        if (user.isEmpty() || !passwordEncoder.matches(authRequest.password(), user.get().getPassword())) {
            return ApiResponse.error(ResponseError.passwordDidNotMatch());
        } else if (!user.get().isActive()) {
            return ApiResponse.error(ResponseError.accessDenied());
        }

        String token = jwtProvider.generateToken(user.get().getPhoneNumber());

        return ApiResponse.ok(ResponseSuccess.login(),new LoginResponse(user.get().getId(),token,user.get().getRole()));
    }
}
