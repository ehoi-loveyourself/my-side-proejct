package com.project.ecommerce.common.exception;

public class UserErrorMessages {
    public static final String DUPLICATED_EMAIL = "이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요";
    public static final String NOT_FOUND_USER = "사용자를 찾을 수 없습니다.";
    public static final String INCORRECT_PASSWORD = "현재 비밀번호가 일치하지 않습니다.";
    public static final String SAME_WITH_BEFORE_PASSWORD = "새로운 비밀번호는 현재 비밀번호와 달라야 합니다.";
    public static final String MIN_PASSWORD_LENGTH_ERROR = "비밀번호는 최소 8자 이상이어야 합니다.";
    public static final String WRONG_LOGIN_INFO = "아이디와 비밀번호를 확인하세요";
}