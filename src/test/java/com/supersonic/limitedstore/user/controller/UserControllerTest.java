package com.supersonic.limitedstore.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.user.entity.User;
import com.supersonic.limitedstore.domain.user.presentation.controller.UserController;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserLoginRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserSignupRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserUpdateRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserLoginResponseDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import com.supersonic.limitedstore.domain.user.service.UserService;
import com.supersonic.limitedstore.security.JwtTokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;



@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void 회원가입_성공() throws Exception{
        //given
        UserSignupRequestDto request = UserSignupRequestDto.builder()
            .email("test@test.com")
            .password("1q2w3e4r!")
            .nickname("tester")
            .build();

        ApiResponse<UserResponseDto> response = ApiResponse.ok(
            new UserResponseDto(UUID.randomUUID(), "test@test.com", "tester")
        );

        when(userService.signup(any())).thenReturn(response);

        //when & then
        mockMvc.perform(post("/v1/users/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("test@test.com"))
            .andExpect(jsonPath("$.data.nickname").value("tester"));

    }

    @Test
    void 회원가입_실패_이메일형식오류() throws Exception{
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
            .email("wrongEmail")
            .password("1q2w3e4r!")
            .nickname("tester")
            .build();

        mockMvc.perform(post("/v1/users/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 회원가입_실패_이메일없음() throws Exception{
        String json = """
            {
            "password": "1q2w3e4r!",
            "nickname": "tester"
            }
            """;

        mockMvc.perform(post("/v1/users/sign-up")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 회원가입_실패_닉네임없음() throws Exception {
        String json = """
        {
            "email": "test@test.com",
            "password": "1q2w3e4r!"
        }
    """;

        mockMvc.perform(post("/v1/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_성공() throws Exception{
        UserLoginRequestDto request = UserLoginRequestDto.builder()
            .email("test@test.com")
            .password("1q2w3e4r!")
            .build();

        ApiResponse<UserLoginResponseDto> response = ApiResponse.ok(
            UserLoginResponseDto.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .nickname("tester")
                .accessToken("jwt-token")
                .build()
        );

        when(userService.login(any())).thenReturn(response);

        mockMvc.perform(post("/v1/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("test@test.com"))
            .andExpect(jsonPath("$.data.accessToken").value("jwt-token"));
    }

    @Test
    void 로그인_실패_이메일형식오류() throws Exception{
        UserLoginRequestDto requestDto = UserLoginRequestDto.builder()
            .email("wrongEmail")
            .password("1q2w3e4r!")
            .build();

       mockMvc.perform(post("/v1/users/login")
           .contentType(MediaType.APPLICATION_JSON)
           .content(objectMapper.writeValueAsString(requestDto)))
           .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_실패_이메일없음() throws Exception{
        UserLoginRequestDto requestDto = UserLoginRequestDto.builder()
            .email("notfound@test.com")
            .password("1q2w3e4r!")
            .build();

        when(userService.login(any())).thenThrow(new CustomException(ErrorCode.NOT_FOUND_EMAIL));

        mockMvc.perform(post("/v1/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isNotFound());
    }

    @Test
    void 내정보조회_성공() throws Exception{
        String email = "test@test.com";

        User user = User.builder()
            .id(UUID.randomUUID())
            .email(email)
            .nickname("tester")
            .password("encodedPW")
            .build();

        when(userService.getUserByEmail(email)).thenReturn(user);

        mockMvc.perform(get("/v1/users/me")
            .requestAttr("email", email))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.nickname").value("tester"));
    }


    @Test
    void 내정보조회_실패_이메일없음() throws Exception{
        when(userService.getUserByEmail(any())).thenThrow(new CustomException(ErrorCode.NOT_FOUND_EMAIL));

        mockMvc.perform(get("/v1/users/me"))
            .andExpect(status().isNotFound());
    }

    @Test
    void 내정보수정_성공()  throws Exception{
        UserUpdateRequestDto requestDto = UserUpdateRequestDto.builder()
            .email("test@test.com")
            .nickname("updateTester")
            .build();

        ApiResponse<UserResponseDto> response = ApiResponse.ok(
            UserResponseDto.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .nickname("updateTester")
                .build()
        );

        when(userService.updateUser(any(), any())).thenReturn(response);

        mockMvc.perform(patch("/v1/users/me")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto))
            .requestAttr("email", "test@test.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("test@test.com"))
            .andExpect(jsonPath("$.data.nickname").value("updateTester"));
    }

    @Test
    void 내정보수정_실패_닉네임없음() throws Exception{
        String json = """
            {
            "email": "test@test.com"
            }
            """;

        mockMvc.perform(patch("/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 내정보수정_실패_닉네임중복() throws Exception{
        UserUpdateRequestDto requestDto = UserUpdateRequestDto.builder()
            .email("test@test.com")
            .nickname("dupNick")
            .build();

        when(userService.updateUser(any(), any())).thenThrow(new CustomException(ErrorCode.NICKNAME_DUPLICATED));

        mockMvc.perform(patch("/v1/users/me")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isConflict());
    }




    @Test
    void 회원삭제_성공()  throws Exception{
        String email = "test@test.com";

        mockMvc.perform(delete("/v1/users/me")
            .requestAttr("email", email))
            .andExpect(status().isOk());

        verify(userService).deleteUser(email);
    }

    @Test
    void 회원삭제_실패_이메일없음()  throws Exception{
        doThrow(new CustomException(ErrorCode.NOT_FOUND_EMAIL))
            .when(userService)
            .deleteUser(null);

        mockMvc.perform(delete("/v1/users/me"))
            .andExpect(status().isNotFound());
    }

    @Test
    void 회원삭제_실패_삭제된유저()  throws Exception{
        doThrow(new CustomException(ErrorCode.USER_DELETED))
            .when(userService)
            .deleteUser(any());

        mockMvc.perform(delete("/v1/users/me"))
            .andExpect(status().isForbidden());
    }


}
