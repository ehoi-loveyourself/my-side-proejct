package com.project.ecommerce.common.security;

import com.project.ecommerce.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SpringSecurity 의 기본 UserDetails 인터페이스를 확장하여 사용자 정의 추가 정보(사용자 id, 이름, email 등)를 포함시키는 클래스
 */
public class CustomUserDetails implements UserDetails {

    private final List<GrantedAuthority> authorities;
    private final String password;
    private final String name;
    @Getter
    private final Long userId;

    public CustomUserDetails(List<String> roles, String password, String name, Long userId) {
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        this.password = password;
        this.name = name;
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    // User 엔티티로부터 CustomUserDetails 객체를 만들어 생성하는 정적 팩토리 메서드
    public static CustomUserDetails fromUserEntity(User user) {
        List<String> roles = Collections.singletonList(user.getRole().name());

        return new CustomUserDetails(roles, user.getPassword(), user.getName(), user.getId());
    }
}