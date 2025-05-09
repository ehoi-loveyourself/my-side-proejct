package com.project.ecommerce.domain.user.dto;

import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = UserErrorMessages.MIN_PASSWORD_LENGTH_ERROR)
        private String password;

        @NotBlank(message = "이름은 필수입니다")
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserResponse {
        private Long userId;
        private String email;
        private String name;
        private String role;

        public static UserResponse of(User user) {
            return UserResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().toString())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TokenResponse {
        private String token;
//        private UserResponse user; // todo: user 정보는 별도 api 로 분리하는 것 고려
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @NotBlank(message = "이름은 공백일 수 없습니다.")
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePasswordRequest {

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        private String currentPassword;

        @NotBlank(message = "변경할 비밀번호는 필수입니다")
        @Size(min = 8, message = UserErrorMessages.MIN_PASSWORD_LENGTH_ERROR)
        private String newPassword;
    }
}