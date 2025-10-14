package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.*;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Security.CustomPrincipal;
import com.andamiro.Dashboard.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "회원가입 및 사용자 관리 API")
public class UserController {

    private final UserService userService;

    /* 00-00 API 건 매핑 */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인 후 JWT 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }


    /* 00-01 API 건 매핑 */
    @Operation(summary = "공통 회원가입", description = "모든 사용자(선주/선원) 기본 정보 등록")
    @PostMapping
    public ResponseEntity<UserResponse> signup(@RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        return ResponseEntity.status(201).body(response);
    }

    /* 00-02 선주 추가 정보 입력 */
    @Operation(summary = "선주 추가 정보 입력", description = "선주 회원가입 시 추가 정보 입력")
    @PostMapping("/{id}/owner-info")
    public ResponseEntity<Void> addOwnerInfo(@PathVariable UUID id, @RequestBody OwnerInfoRequest request) {
        userService.addOwnerInfo(id, request);
        return ResponseEntity.status(201).build();
    }

    /* 00-03 선원 추가 정보 입력 */
    @Operation(summary = "선원 추가 정보 입력", description = "선원 회원가입 시 추가 정보 입력")
    @PostMapping("/{id}/crew-info")
    public ResponseEntity<Void> addCrewInfo(@PathVariable UUID id, @RequestBody CrewInfoRequest request) {
        userService.addCrewInfo(id, request);
        return ResponseEntity.status(201).build();
    }

    /* 00-04 승인 */
    @Operation(summary = "승인 여부 업데이트", description = "관리자가 선주/선원을 승인 처리")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable UUID id, @RequestBody ApproveRequest request) {
        userService.approveUser(id, request);
        return ResponseEntity.ok().build();
    }

    /* 00-05 본인 정보 수정 */
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal CustomPrincipal principal, @PathVariable UUID id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(principal.getId(), id, request));
    }

    /* 00-06 본인 정보 조회 */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보 반환")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomPrincipal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(principal.getId(),id));
    }

    /* 00-07 본인 삭제 (회원 탈퇴) */
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자 계정 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal CustomPrincipal principal, @PathVariable UUID id) {
        userService.deleteUser(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
