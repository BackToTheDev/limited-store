package com.supersonic.limitedstore.domain.user.service;

import com.supersonic.limitedstore.common.config.SecurityConfig;
import com.supersonic.limitedstore.domain.user.entity.User;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserSignupRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import com.supersonic.limitedstore.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    public UserResponseDto signup(UserSignupRequestDto requestDto) {

        // 👉 이 부분은 나중에 구현 (중복 검사 / 비밀번호 암호화 / 저장)
        return null;
    }

    /**
     * 이메일 중복 체크
     */
    public boolean existsByEmail(String email) {
        // 👉 나중에 userRepository.existsByEmail(email) 추가
        return false;
    }

    /**
     * 닉네임 중복 체크
     */
    public boolean existsByNickname(String nickname) {
        // 👉 나중에 userRepository.existsByNickname(nickname)
        return false;
    }

    /**
     * 유저 단일 조회 (ID 기반)
     */
    public UserResponseDto getUserById(UUID userId) {
        // 👉 유저 조회 후 UserResponseDto 로 변환
        return null;
    }

    /**
     * 로그인은 보통 AuthService 에서 구현 (선택)
     */
    public User authenticate(String email, String rawPassword) {
        // 👉 나중에 비밀번호 검사 후 User 반환
        return null;
    }
}
