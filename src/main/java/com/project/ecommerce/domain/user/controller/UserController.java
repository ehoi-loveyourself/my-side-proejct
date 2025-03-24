package com.project.ecommerce.domain.user.controller;

import com.project.ecommerce.domain.user.dto.UserDto;
import com.project.ecommerce.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.project.ecommerce.common.utils.ResponseUtil.createSuccessResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signUp")
    public ResponseEntity<Map<String, Object>> signUp(@Valid @RequestBody UserDto.SignUpRequest request) {
        UserDto.UserResponse response = userService.signUp(request);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody UserDto.LoginRequest request) {
        UserDto.TokenResponse response = userService.login(request);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto.UserResponse response = userService.currentUser(userDetails.getUsername());

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateUser(@AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody UserDto.UpdateRequest request) {
        UserDto.UserResponse response = userService.updateUser(userDetails.getUsername(), request);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, Object>> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                              @Valid @RequestBody UserDto.UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);

        Map<String, Object> message = new HashMap<>();
        message.put("message", "비밀번호가 성공적으로 변경되었습니다.");

        return ResponseEntity.ok(message);
    }
}