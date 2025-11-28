package com.supersonic.limitedstore.domain.user.entity;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.entity.BaseEntity;
import com.supersonic.limitedstore.domain.user.presentation.dto.req.UserUpdateRequestDto;
import com.supersonic.limitedstore.domain.user.presentation.dto.res.UserResponseDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column()
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false,  unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    public void updateUser(UserUpdateRequestDto dto) {
        if (dto.getNickname() != null) {
            this.nickname = dto.getNickname();
        }
    }
}
