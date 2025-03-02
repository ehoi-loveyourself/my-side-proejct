package com.project.ecommerce.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 접근승인되는_엔드포인트() throws Exception {
        // 회원가입
        mockMvc.perform(post("/api/v1/users/register"))
                .andExpect(status().isOk());

        // 로그인
        mockMvc.perform(post("/api/v1/users/login"))
                .andExpect(status().isOk());

        // h2-console
//        mockMvc.perform(get("/h2-console"))
//                .andExpect(status().isOk());
    }

    @Test
    void 승인되면_안되는_엔드포인트() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}