package com.supersonic.limitedstore.domain.user.presentation.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 1, max = 20)
    private String nickname;

}
