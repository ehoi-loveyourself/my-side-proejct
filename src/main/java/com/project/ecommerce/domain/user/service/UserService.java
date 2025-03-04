package com.project.ecommerce.domain.user.service;

import com.project.ecommerce.domain.user.dto.UserDto;

public interface UserService {
    // 회원가입
    UserDto.UserResponse signUp(UserDto.SignUpRequest request);

    // 로그인
    UserDto.TokenResponse login(UserDto.LoginRequest request);

    // 회원 정보 조회
    UserDto.UserResponse currentUser(String email);

    // 회원 정보 수정

    // 비밀번호 수정
}
