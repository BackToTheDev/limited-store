package com.supersonic.limitedstore.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supersonic.limitedstore.common.dto.ApiResponse;
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
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private UserService userService;

    @MockitoBean
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
    void 업데이트_성공()  throws Exception{
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


}
