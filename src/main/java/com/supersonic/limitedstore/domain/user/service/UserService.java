package com.supersonic.limitedstore.domain.user.service;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.user.entity.User;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserLoginRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserSignupRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserUpdateRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserLoginResponseDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import com.supersonic.limitedstore.domain.user.repository.UserRepository;
import com.supersonic.limitedstore.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    public ApiResponse<UserResponseDto> signup(UserSignupRequestDto dto) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 패스워드 인코딩
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // user 객체 생성
        User user = User.builder()
            .email(dto.getEmail())
            .password(encodedPassword)
            .nickname(dto.getNickname())
            .build();

        // 저장
        User saved = userRepository.save(user);

        // 응답 DTO로 변환
        return ApiResponse.ok(
            UserResponseDto.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .nickname(saved.getNickname())
                .build()
        );
    }

    public ApiResponse<UserLoginResponseDto> login(UserLoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_EMAIL));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.createToken(user.getEmail());

        return ApiResponse.ok(
            UserLoginResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .accessToken(token)
                .build()
        );
    }

    public ApiResponse<UserResponseDto> updateUser (UserUpdateRequestDto dto, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_EMAIL)
        );

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        if (userRepository.existsByNickname(dto.getNickname())
        && !user.getNickname().equals(dto.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
        }

        user.updateUser(dto);
        userRepository.save(user);

        return ApiResponse.ok(
            UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build()
        );
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_EMAIL));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        user.softDelete();
        userRepository.save(user);
    }



    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new CustomException(ErrorCode.NOT_FOUND_EMAIL)
        );

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        return user;
    }
}
