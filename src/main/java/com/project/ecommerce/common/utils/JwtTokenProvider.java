package com.project.ecommerce.common.utils;

import com.project.ecommerce.common.exception.JwtErrorMessages;
import com.project.ecommerce.common.exception.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token-validity-in-seconds:1800}")
    private long tokenValidInSeconds;

    private Key key;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("JWT Secret Key must not be null or empty");
        }

        if (tokenValidInSeconds <= 0) {
            throw new IllegalArgumentException("Token validity must not be greater than 0");
        }

        // secretKey를 디코딩해서 사용하기 (<- secretKey를 환경변수에서 미리 Base64로 인코딩해두기)
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);

        // JWT 서명(Signature) 키를 안전하게 생성하는 메서드
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }

    // JWT 토큰 생성
    public String createToken(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities() // 사용자의 권한 목록을 가져옴
                .stream()
                .map(GrantedAuthority::getAuthority) // 각 GrantedAuthority 객체에서 권한 문자열을 추출
                .collect(Collectors.toList()); // 여러 개의 권한을 쉼표로 연결하여 문자열로 변환

        // 현재 시간 (밀리초 단위)
        long now = new Date().getTime();

        // 토큰 만료 시간 설정
        Date validity = Date.from(Instant.now().plusSeconds(tokenValidInSeconds));

        // JWT 토큰 생성
        return Jwts.builder()
                .setSubject(authentication.getName()) // 사용자 정보 설정
                .claim("auth", authorities) // 사용자 권한을 "auth" 클레임에 추가
                .signWith(key, SignatureAlgorithm.HS512) // 서명 (HMAC SHA-512 알고리즘)
                .setExpiration(validity) // 만료 시간 설정
                .compact(); // JWT 문자열로 변환
    }

    /**
     * 토큰에서 Authentication 객체 추출
     * JWT에서 유저 정보 + 권한 정보를 가져와서 UsernamePasswordAuthenticationToken 생성
     * @param token
     * @return Authentication
     */
    public Authentication getAuthentication(String token) {
        try {
            // 1. JWT 파싱 (서명 검증 + 클레임 추출)
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key) // 서명을 검증할 key 설정
                    .build()
                    .parseClaimsJws(token) // JWT 토큰 파싱 + 검증 포함
                    .getBody(); // 클레임 추출

            // 2. "auth" 클레임에서 권한 정보를 가져와서 GrantedAuthority 리스트로 변환
            List<String> roles = claims.get("auth", List.class);
            Collection<? extends GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            // 3. User 객체 생성 (비밀번호 없이 UserDetails 역할 수행)
            // todo: 비밀번호 없이 principal 객체 개선하는 거 수정 (User 대신 CustomUserDetails 활용)
            UserDetails principal = new User(claims.getSubject(), "", authorities);

            // 4. Spring Security 의 Authentication 객체 반환
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } catch (ExpiredJwtException e) {
            throw new JwtException(JwtErrorMessages.EXPIRED_TOKEN);
        } catch (MalformedJwtException | IllegalArgumentException e) {
            throw new JwtException(JwtErrorMessages.INVALID_TOKEN);
        }
    }

    /**
     * 토큰 유효성 검증
     * @param token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // 서명을 검증할 key 설정
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new JwtException(JwtErrorMessages.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new JwtException(JwtErrorMessages.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new JwtException(JwtErrorMessages.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new JwtException(JwtErrorMessages.WRONG_TOKEN);
        }
    }

    // 토큰에서 username 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}