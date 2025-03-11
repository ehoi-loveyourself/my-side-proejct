package com.project.ecommerce.domain.user.intergration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ecommerce.domain.user.dto.UserDto;
import com.project.ecommerce.domain.user.entity.Role;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
public class UserIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("test user")
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(testUser);

        // 로그인하여 토큰 획득
        UserDto.LoginRequest loginRequest = UserDto.LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        String response = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(response).path("data").path("token").asText();
    }

    @Test
    void 회원가입_테스트_성공() throws Exception {
        // given
        UserDto.SignUpRequest request = UserDto.SignUpRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .name("new user")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.name").value("new user"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }
    
    @Test
    void 회원가입_테스트_실패_중복이메일() throws Exception {
        // given
        UserDto.SignUpRequest request = UserDto.SignUpRequest.builder()
                .email("test@example.com") // 이미 존재하는 이메일
                .password("password123")
                .name("duplicate user")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error.message").value("이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요"));
    }

    @Test
    void 로그인_테스트_성공() throws Exception {
        // given
        UserDto.LoginRequest request = UserDto.LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }
}