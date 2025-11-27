package com.supersonic.limitedstore.domain.user.presentation.dto.req;

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
public class LoginRequestDto {
    @Email
    @Size(min = 10, max = 100)
    private String email;

    @NotBlank
    @Size(min = 10, max = 100)
    private String password;

}
