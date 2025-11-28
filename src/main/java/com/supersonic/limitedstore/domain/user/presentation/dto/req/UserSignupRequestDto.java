package com.supersonic.limitedstore.domain.user.presentation.dto.req;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequestDto {

    @NotBlank
    @Email
    @Size(min = 8, max = 100)
    private String email;

    @NotBlank
    @Size(min = 5, max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String nickname;
}
