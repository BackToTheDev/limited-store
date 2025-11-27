package com.supersonic.limitedstore.domain.user.presentation.controller;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.user.entity.User;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.LoginRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserSignupRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.LoginResponseDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import com.supersonic.limitedstore.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto dto) {
        return userService.login(dto);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");

        if (email == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_EMAIL);
        }

        User user = userService.getUserByEmail(email);

        return ApiResponse.ok(
            UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build()
        );
    }
}
