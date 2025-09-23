package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.*;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    /* 00-05 수정 */
    @Operation(summary = "유저 정보 수정", description = "유저 정보 수정 (이름, 비밀번호 등)")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /* 00-06 조회 */
    @Operation(summary = "유저 정보 조회", description = "유저 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    /* 00-07 삭제 */
    @Operation(summary = "유저 삭제", description = "유저 삭제 (선주/선원 공통)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
