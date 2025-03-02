package com.project.ecommerce.common.utils;

import com.project.ecommerce.common.exception.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // application.yml 의 값을 테스트에서 직접 설정
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", "D0BUrCTCH2C0ima4cy7k5hPATiO90GU8dDw8mBj6LfAu07+BVkueaknyKqfQ6m+wKqCfVfNxaAnK2sJAhZlV4w==");
        ReflectionTestUtils.setField(jwtTokenProvider, "tokenValidInSeconds", 1800L); // 30분
        jwtTokenProvider.init();
    }

    @Test
    void 토큰_생성이_정상적으로_되는지() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // when
        String token = jwtTokenProvider.createToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void 생성된_토큰에서_인증정보를_올바르게_추출하는지() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.createToken(authentication);

        // when
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo("testUser");
        assertThat(resultAuth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void 토큰에서_사용자_이름을_정확히_추출하는지() throws Exception {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.createToken(authentication);

        // when
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // then
        assertThat(username).isEqualTo("testUser");
    }

    @Test
    void 만료된_토큰을_정확히_검증하는지() throws Exception {
        // given
        JwtTokenProvider zeroSecondTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(zeroSecondTokenProvider, "secretKey", "D0BUrCTCH2C0ima4cy7k5hPATiO90GU8dDw8mBj6LfAu07+BVkueaknyKqfQ6m+wKqCfVfNxaAnK2sJAhZlV4w==");
        ReflectionTestUtils.setField(zeroSecondTokenProvider, "tokenValidInSeconds", 1L);
        zeroSecondTokenProvider.init();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = zeroSecondTokenProvider.createToken(authentication);

        // when & then
        Thread.sleep(2000);
        assertThrows(JwtException.class, () -> {
           zeroSecondTokenProvider.validateToken(token);
        });
    }

    @Test
    void 유효하지_토큰을_정확히_검증하는지() throws Exception {
        // given
        String token = "invalid_token";

        // when & then
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.validateToken(token);
        });
    }
}