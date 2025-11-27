package com.supersonic.limitedstore.domain.user.presentation.dto.res;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String email;
    private String nickname;
}
