package com.supersonic.limitedstore.domain.user.presentation.controller;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.LoginRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserSignupRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import com.supersonic.limitedstore.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
    public ApiResponse<UserResponseDto> login(@RequestBody @Valid LoginRequestDto dto) {
        return userService.login(dto);
    }
}
