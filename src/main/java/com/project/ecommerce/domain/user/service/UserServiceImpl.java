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
     *
     * @param request
     * @return 회원가입 후 회원 dto
     */
    @Transactional
    @Override
    public UserDto.UserResponse signUp(UserDto.SignUpRequest request) {
        /* 사용자 생성 전에 이메일 중복확인을 넣지 않은 이유
         * 해당 이메일로 가입한 유저는 있는지 없는지에 대한 추적이 될 수 있다는 피드백을 받음
         * 그런데 사실 실무에서는 이메일 중복확인 메서드가 따로 있을 것이기 때문에
         * 회원가입 로직에서 더블체크하는 정도로 역할 한다고만 생각함
         * */

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
        try {

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
        } catch (Exception e) {
            throw new UserException(UserErrorMessages.WRONG_LOGIN_INFO, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public UserDto.UserResponse currentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        return UserDto.UserResponse.of(user);
    }

    @Override
    @Transactional
    public UserDto.UserResponse updateUser(String email, UserDto.UpdateRequest request) {
        // 이메일로 회원을 찾음
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        // 수정해야 할 필드에 대해 유효성 검사
        if (isValidName(request.getName())) {
            // 이름 변경
            user.updateName(request.getName());
        }

        // 업데이트된 사용자 저장 -> JPA의 변경 감지(더티 체킹) 기능 덕분에 save() 호출 필요 없음!
//        User updatedUser = userRepository.save(user);

        return UserDto.UserResponse.of(user);
    }

    @Override
    @Transactional
    public void updatePassword(String email, UserDto.UpdatePasswordRequest request) {
        // 이메일로 회원을 찾음
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserException(UserErrorMessages.INCORRECT_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        // 바꾸려는 비밀번호가 현재 비밀번호와 같은지도 체크
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new UserException(UserErrorMessages.SAME_WITH_BEFORE_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        // 비밀번호 유효성 검사
        validatePassword(request.getNewPassword());

        // 새 비밀번호로 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new UserException(UserErrorMessages.MIN_PASSWORD_LENGTH_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }
}