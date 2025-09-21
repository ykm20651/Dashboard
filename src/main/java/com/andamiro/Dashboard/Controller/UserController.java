package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.*;
import com.andamiro.Dashboard.Dto.UserDTO.CrewInfoRequest;
import com.andamiro.Dashboard.Dto.UserDTO.OwnerInfoRequest;
import com.andamiro.Dashboard.Dto.UserDTO.UserResponse;
import com.andamiro.Dashboard.Dto.UserDTO.UserSignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "회원가입 및 사용자 관리 API")
public class UserController {

    /* 00-01 API 건 매핑 */
    @Operation(summary = "공통 회원가입", description = "모든 사용자(선주/선원) 기본 정보 등록")
    @PostMapping
    public ResponseEntity<UserResponse> signup(@RequestBody UserSignupRequest request) {
        // 구현은 Service에서
        return ResponseEntity.ok(new UserResponse(UUID.randomUUID(), request.email(), request.name(), "crew", false));
    }

    /* 00-02 API 건 매핑 */
    @Operation(summary = "선주 추가 정보 입력", description = "선주 회원가입 시 추가 정보 입력")
    @PostMapping("/{id}/owner-info")
    public ResponseEntity<Void> addOwnerInfo(@PathVariable UUID id, @RequestBody OwnerInfoRequest request) {
        return ResponseEntity.ok().build();
    }

    /* 00-03 API 건 매핑 */
    @Operation(summary = "선원 추가 정보 입력", description = "선원 회원가입 시 추가 정보 입력")
    @PostMapping("/{id}/crew-info")
    public ResponseEntity<Void> addCrewInfo(@PathVariable UUID id, @RequestBody CrewInfoRequest request) {
        return ResponseEntity.ok().build();
    }

    /* 00-04 API 건 매핑 */
    @Operation(summary = "승인 여부 업데이트", description = "관리자가 선주/선원을 승인 처리")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable UUID id, @RequestBody ApproveRequest request) {
        return ResponseEntity.ok().build();
    }

    /* 00-05 API 건 매핑 */
    @Operation(summary = "유저 정보 수정", description = "유저 정보 수정 (이름, 비밀번호 등)")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody UserSignupRequest request) {
        return ResponseEntity.ok(new UserResponse(id, request.email(), request.name(), "crew", true));
    }

    /* 00-06 API 건 매핑 */
    @Operation(summary = "유저 정보 조회", description = "유저 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(new UserResponse(id, "user@ship.com", "홍길동", "owner", true));
    }

    /* 00-07 API 건 매핑 */
    @Operation(summary = "유저 삭제", description = "유저 삭제 (선주/선원 공통)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}
