package com.supersonic.limitedstore.domain.user.presentation.controller;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.user.entity.User;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserLoginRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserSignupRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserUpdateRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserLoginResponseDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import com.supersonic.limitedstore.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ApiResponse<UserResponseDto> signup(@RequestBody @Valid UserSignupRequestDto dto) {
        return userService.signup(dto);
    }

    @PostMapping("/login")
    public ApiResponse<UserLoginResponseDto> login(@RequestBody @Valid UserLoginRequestDto dto) {
        return userService.login(dto);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        User user = userService.getUserByEmail(email);

        return ApiResponse.ok(
            UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build()
        );
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponseDto> updateMyInfo(@RequestBody @Valid UserUpdateRequestDto dto, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return userService.updateUser(dto, email);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> userDelete(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        userService.deleteUser(email);
        return ApiResponse.ok(null);
    }
}
