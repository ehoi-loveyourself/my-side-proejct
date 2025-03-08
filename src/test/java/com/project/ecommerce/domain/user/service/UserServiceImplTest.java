package com.project.ecommerce.domain.user.service;

import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.common.utils.JwtTokenProvider;
import com.project.ecommerce.domain.user.dto.UserDto;
import com.project.ecommerce.domain.user.entity.Role;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks private UserServiceImpl userService;

    private User user;
    private UserDto.SignUpRequest signUpRequest;
    private UserDto.LoginRequest loginRequest;
    private Authentication authentication;

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

        loginRequest = UserDto.LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    void 회원가입_후_객체반환_성공() throws Exception {
        // given
//        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserDto.UserResponse response = userService.signUp(signUpRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(signUpRequest.getEmail());
        assertThat(response.getName()).isEqualTo(signUpRequest.getName());
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER.name());

//        verify(userRepository).existsByEmail(signUpRequest.getEmail());
        verify(passwordEncoder).encode(signUpRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 이메일_중복검사_예외_테스트() {
        // given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> userService.signUp(signUpRequest))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining(UserErrorMessages.DUPLICATED_EMAIL);

        verify(passwordEncoder).encode(signUpRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 로그인_성공() throws Exception {
        // given
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtTokenProvider.createToken(any(Authentication.class))).thenReturn("jwt.token.here");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // when
        UserDto.TokenResponse response = userService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtTokenProvider).createToken(authentication);
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }
    
    @Test
    void 미존재회원_로그인_테스트_실패() throws Exception {
        // given
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtTokenProvider.createToken(any(Authentication.class))).thenReturn("jwt.token.here");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining(UserErrorMessages.NOT_FOUND_USER);

        // then
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtTokenProvider).createToken(authentication);
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    void 사용자_조회_테스트_성공() throws Exception {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // when
        UserDto.UserResponse response = userService.currentUser("test@example.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getName()).isEqualTo(user.getName());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void 사용자_조회_테스트_실패() throws Exception {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.currentUser("nonexisting@example.com"))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining(UserErrorMessages.NOT_FOUND_USER);

        verify(userRepository).findByEmail("nonexisting@example.com");
    }

    @Test
    void 회원정보_수정_테스트_성공() throws Exception {
        // given
        UserDto.UpdateRequest updateRequest = UserDto.UpdateRequest.builder()
                .name("Updated name")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // when
        UserDto.UserResponse response = userService.updateUser("test@example.com", updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated name");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class)); // JPA 더티체킹 기능으로 save()를 호출하지 않는지 검증
    }

    @Test
    void 회원정보_수정_테스트_변화없음() throws Exception {
        // given
        UserDto.UpdateRequest updateRequest = UserDto.UpdateRequest.builder()
                .name("")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        String originName = user.getName();

        // when
        UserDto.UserResponse response = userService.updateUser("test@example.com", updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isNotEqualTo("Updated name");
        assertThat(response.getName()).isEqualTo(originName);

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class)); // JPA 더티체킹 기능으로 save()를 호출하지 않는지 검증
    }
}