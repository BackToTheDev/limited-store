package com.supersonic.limitedstore.user.service;

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
import com.supersonic.limitedstore.domain.user.service.UserService;
import com.supersonic.limitedstore.security.JwtTokenProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    UserService userService;

    @Test
    void 회원가입_성공() {
        //given
        String email = "test@test.com";
        UserSignupRequestDto dto = UserSignupRequestDto.builder()
                .email(email)
                    .password("1q2w3e4r!")
                        .nickname("슈퍼코딩")
                            .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(dto.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPw");

        User savedUser = User.builder()
            .id(UUID.randomUUID())
            .email(dto.getEmail())
            .password("encodedPw")
            .nickname(dto.getNickname())
            .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        //when
        ApiResponse<UserResponseDto> result = userService.signup(dto);

        //then
        assertEquals(200, result.getStatus());
        assertEquals("test@test.com", result.getData().getEmail());
        assertEquals("슈퍼코딩", result.getData().getNickname());

    }

    @Test
    void 회원가입_이메일중복_실패() {
        //given
        String email = "test@test.com";
        UserSignupRequestDto dto = UserSignupRequestDto.builder()
                .email(email)
                    .password("pw")
                        .nickname("슈퍼코딩")
                            .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        //when & then
        assertThrows(CustomException.class,
            () -> userService.signup(dto));
    }

    @Test
    void 회원가입_닉네임중복_실패() {
        //given
        String email = "test@test.com";
        UserSignupRequestDto dto = UserSignupRequestDto.builder()
                .email(email)
                    .nickname("슈퍼코딩")
                        .password("1q2w3e4r!")
                            .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByNickname(dto.getNickname())).thenReturn(true);

        //when & then
        assertThrows(CustomException.class,
            () -> userService.signup(dto));

    }

    @Test
    void 로그인_성공() {
        //given
        String email = "test@test.com";
        UserLoginRequestDto dto = UserLoginRequestDto.builder()
            .email(email)
            .password("1q2w3e4r!")
            .build();

        User user = User.builder()
            .email(email)
            .password("encodedPW")
            .nickname("tester")
            .build();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.createToken(any(), any())).thenReturn("token");

        //when
        ApiResponse<UserLoginResponseDto> result = userService.login(dto);

        //then
        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getData().getEmail()).isEqualTo(email);
        assertThat(result.getData().getAccessToken()).isEqualTo("token");
    }

    @Test
    void 로그인_실패_비밀번호불일치() {
        //given
        String email = "test@test.com";

        UserLoginRequestDto dto = UserLoginRequestDto.builder()
            .email(email)
            .password("wrongPw")
            .build();

        User user = User.builder()
            .email(email)
            .password("encodedPW")
            .nickname("tester")
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.getPassword(), user.getPassword())).thenReturn(false);

        //when & then
        assertThatThrownBy(() -> userService.login(dto))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.INVALID_PASSWORD.getMessage());
    }

    @Test
    void 로그인_실패_이메일없음() {
        //given
        UserLoginRequestDto dto = UserLoginRequestDto.builder()
            .email("notfound@test.com")
            .password("1q2w3e4r!")
            .build();

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.login(dto))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.NOT_FOUND_EMAIL.getMessage());
    }

    @Test
    void 내정보조회_성공() {
     //given
     String email = "test@test.com";

     User user = User.builder()
         .email(email)
         .nickname("tester")
         .password("encodedPW")
         .build();

     when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

     //when
        User result = userService.getUserByEmail(email);

        //then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getNickname()).isEqualTo("tester");
    }

    @Test
    void 내정보조회_실패_삭제된계정() {
    //given
    String email = "test@test.com";

    User deletedUser = User.builder()
        .email(email)
        .nickname("tester")
        .password("encodedPW")
        .build();
    deletedUser.softDelete();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(deletedUser));

    //when & then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.USER_DELETED.getMessage());
    }

    @Test
    void 내정보조회_실패_이메일없음() {
        //given
        String email = "test@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.NOT_FOUND_EMAIL.getMessage());
    }

    @Test
    void 회원정보수정_성공() {
        //given
        String email = "test@test.com";

        UserUpdateRequestDto dto = UserUpdateRequestDto.builder()
            .nickname("newNick")
            .build();

        User user = User.builder()
            .email(email)
            .password("pw")
            .nickname("oldNick")
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(dto.getNickname())).thenReturn(false);

        //when
        ApiResponse<UserResponseDto> result = userService.updateUser(dto, email);

        //then
        assertThat(result.getData().getNickname()).isEqualTo(dto.getNickname());
        verify(userRepository).save(any());
    }

    @Test
    void 회원정보수정_실패_닉네임중복() {
        //given
        String email = "test@test.com";

        UserUpdateRequestDto dto = UserUpdateRequestDto.builder()
            .nickname("duplicateNick")
            .build();

        User user = User.builder()
            .email(email)
            .password("pw")
            .nickname("oldNick")
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(dto.getNickname())).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> userService.updateUser(dto, email))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.NICKNAME_DUPLICATED.getMessage());
    }

    @Test
    void 회원삭제_성공() {
        String email = "test@test.com";

        User user = User.builder()
            .email(email)
            .password("pw")
            .nickname("tester")
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        //when
        userService.deleteUser(email);

        //then
        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).save(any());
    }

    @Test
    void 회원삭제_실패() {
        String email = "notfound@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.deleteUser(email))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.NOT_FOUND_EMAIL.getMessage());

    }
}
