package com.supersonic.limitedstore.domain.user.presentation.dto.req;

import jakarta.validation.constraints.Email;
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

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 1, max = 20)
    private String nickname;

}
