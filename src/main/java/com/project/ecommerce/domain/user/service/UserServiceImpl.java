package com.project.ecommerce.domain.user.service;

import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.common.utils.JwtTokenProvider;
import com.project.ecommerce.domain.user.dto.UserDto;
import com.project.ecommerce.domain.user.entity.Role;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     * @param request
     * @return 회원가입 후 회원 dto
     */
    @Transactional
    @Override
    public UserDto.UserResponse signUp(UserDto.SignUpRequest request) {
        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.CUSTOMER)
                .build();

        try {
            User savedUser = userRepository.save(user);
            return UserDto.UserResponse.of(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserException(UserErrorMessages.DUPLICATED_EMAIL, HttpStatus.CONFLICT);
        }
    }

    @Override
    public UserDto.TokenResponse login(UserDto.LoginRequest request) {
        // Spring Security 의 인증 매니저를 사용하여 인증
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager
                .authenticate(authenticationToken);

        // 인증 정보를 기반으로 JWT 토큰 생성
        String token = jwtTokenProvider.createToken(authentication);

        // 사용자 정보 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        return UserDto.TokenResponse.builder()
                .token(token)
//                .user(UserDto.UserResponse.of(user)) // todo: 별도 api 로 분리
                .build();
    }

    @Override
    public UserDto.UserResponse currentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        return UserDto.UserResponse.of(user);
    }
}