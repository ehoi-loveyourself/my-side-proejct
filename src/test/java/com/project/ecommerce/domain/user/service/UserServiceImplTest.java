package com.project.ecommerce.domain.user.service;

import com.project.ecommerce.domain.user.dto.UserDto;
import com.project.ecommerce.domain.user.entity.Role;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private User user;
    private UserDto.SignUpRequest signUpRequest;

    @BeforeEach
    void setup() {
        user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("test user")
                .role(Role.CUSTOMER)
                .build();

        signUpRequest = UserDto.SignUpRequest.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("test user")
                .build();
    }

    @Test
    void 회원가입_후_객체반환_성공() throws Exception {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserDto.UserResponse response = userService.signUp(signUpRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(signUpRequest.getEmail());
        assertThat(response.getName()).isEqualTo(signUpRequest.getName());
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER.name());

        verify(userRepository).existsByEmail(signUpRequest.getEmail());
        verify(passwordEncoder).encode(signUpRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }
}