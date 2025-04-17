package com.project.ecommerce.common.security;

import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * UserContext
 * 현재 인증된 사용자의 정보를 관리하고 접근하기 위한 사용자 정의 컴포넌트
 * Spring Security 를 사용하는 애플리케이션에서 현재 로그인한 사용자의 정보를 서비스 계층에서 쉽게 접근할 수 있도록 해주는 역할
 *
 * 이 클래스의 목적
 * 1. 의존성 분리
 * 서비스 코드에서 Spring Security 에 직접 의존하지 않고, 추상화된 인터페이스를 통해 사용자 정보에 접근할 수 있게 한다.
 * 2. 테스트 용이성
 * 테스트 시에 UserContext 를 모킹하여 다양한 사용자 상황을 시뮬레이션하기 쉽다.
 * 3. 보안 강화
 * 컨트롤러로부터 사용자 ID를 직접 전달받지 않고, 인증된 사용자 정보만 사용하여 보안을 강화한다.
 * 4. 코드 재사용
 * 여러 서비스에서 사용자 정보를 가져오는 코드를 반복해서 작성하지 않아도 된다.
 */
@Component
public class UserContext {

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * 사용자가 인증되지 않은 경우 UNAUTHORIZED 에러를 반환합니다.
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserException(UserErrorMessages.LOGIN_REQUIRED, HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUserId();
    }

    /**
     * 현재 인증된 사용자의 상세 정보를 반환합니다.
     */
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserException(UserErrorMessages.LOGIN_REQUIRED, HttpStatus.UNAUTHORIZED);
        }

        return (CustomUserDetails) authentication.getPrincipal();
    }

    /**
     * 사용자가 인증이 되었는지 확인합니다.
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}