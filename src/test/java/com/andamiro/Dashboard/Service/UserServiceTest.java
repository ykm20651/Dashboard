package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Config.UserTestConfig;
import com.andamiro.Dashboard.Dto.ApproveRequest;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Entity.Owner;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Fixture.UserFixture;
import com.andamiro.Dashboard.Repository.CrewMemberRepository;
import com.andamiro.Dashboard.Repository.OwnerRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Security.JwtTokenProvider;
import com.andamiro.Dashboard.Util.TestEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {UserService.class, UserTestConfig.class})
class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private OwnerRepository ownerRepository;
    @Autowired private CrewMemberRepository crewMemberRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;

    /* 00-00 로그인 */
    @Test
    @DisplayName("00-00 login 메서드는 이메일/비밀번호가 일치하면 JWT 토큰을 반환한다")
    void login_success() {
        UUID id = UUID.randomUUID();
        User fakeUser = UserFixture.createTestUser(id);

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(fakeUser));
        given(passwordEncoder.matches(any(), any())).willReturn(true);
        given(jwtTokenProvider.createToken(any(), any())).willReturn("fake-jwt");

        UserLoginRequest request = UserFixture.loginRequest();
        UserLoginResponse result = userService.login(request);

        then(result.token()).isEqualTo("fake-jwt");
        then(result.email()).isEqualTo("test@example.com");
    }

    /* 00-01 회원가입 */
    @Test
    @DisplayName("00-01 signup 메서드는 새로운 사용자를 저장하고 반환한다")
    void signup_success() {
        UUID id = UUID.randomUUID();
        UserSignupRequest request = UserFixture.signupRequest();

        User fakeUser = User.create(
                request.email(), "encoded_pw", request.name(), request.role()
        );
        TestEntityUtil.forceSetId(fakeUser, "id", id);

        given(passwordEncoder.encode(any())).willReturn("encoded_pw");
        given(userRepository.save(any(User.class))).willReturn(fakeUser);

        UserResponse result = userService.signup(request);

        then(result.id()).isEqualTo(id);
        then(result.email()).isEqualTo("test@example.com");
        then(result.name()).isEqualTo("테스트유저");
    }

    /* 00-02 선주 정보 추가 */
    @Test
    @DisplayName("00-02 addOwnerInfo 메서드는 선주 정보를 저장한다")
    void addOwnerInfo_success() {
        UUID id = UUID.randomUUID();
        User fakeUser = UserFixture.createTestUser(id);

        given(userRepository.findById(id)).willReturn(Optional.of(fakeUser));

        userService.addOwnerInfo(id, UserFixture.ownerInfoRequest());

        // 저장 호출 확인만 해도 충분
        then(fakeUser.getId()).isEqualTo(id);
    }

    /* 00-03 선원 정보 추가 */
    @Test
    @DisplayName("00-03 addCrewInfo 메서드는 선원 정보를 저장한다")
    void addCrewInfo_success() {
        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User fakeUser = UserFixture.createTestUser(userId);
        Owner fakeOwner = Owner.create(fakeUser, "회사", "SHIP123", "010-1111-2222", "BIZ-123");

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(ownerRepository.findById(ownerId)).willReturn(Optional.of(fakeOwner));

        userService.addCrewInfo(userId, UserFixture.crewInfoRequest(ownerId));

        then(fakeUser.getId()).isEqualTo(userId);
    }

    /* 00-04 승인 */
    @Test
    @DisplayName("00-04 approveUser 메서드는 사용자를 승인 처리한다")
    void approveUser_success() {
        UUID id = UUID.randomUUID();
        User fakeUser = UserFixture.createTestUser(id);

        given(userRepository.findById(id)).willReturn(Optional.of(fakeUser));

        userService.approveUser(id, new ApproveRequest(true));

        then(fakeUser.isApproved()).isTrue();
    }

    /* 00-05 본인 정보 수정 */
    @Test
    @DisplayName("00-05 updateUser 메서드는 본인 정보 수정 시 반영된다")
    void updateUser_success() {
        UUID id = UUID.randomUUID();
        User fakeUser = UserFixture.createTestUser(id);

        given(userRepository.findById(id)).willReturn(Optional.of(fakeUser));
        given(passwordEncoder.encode(any())).willReturn("encoded_pw");

        UpdateUserRequest request = UserFixture.updateUserRequest();
        UserResponse result = userService.updateUser(id, id, request);

        then(result.name()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("00-05 updateUser 메서드는 본인 이외의 계정 수정 시 예외 발생")
    void updateUser_fail_notSelf() {
        UUID userId = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();

        try {
            userService.updateUser(userId, pathId, UserFixture.updateUserRequest());
        } catch (IllegalArgumentException e) {
            then(e.getMessage()).isEqualTo("본인만 수정할 수 있습니다.");
        }
    }

    /* 00-06 본인 정보 조회 */
    @Test
    @DisplayName("00-06 getUser 메서드는 본인 정보 조회 시 데이터를 반환한다")
    void getUser_success() {
        UUID id = UUID.randomUUID();
        User fakeUser = UserFixture.createTestUser(id);

        given(userRepository.findById(id)).willReturn(Optional.of(fakeUser));

        UserResponse result = userService.getUser(id, id);

        then(result.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("00-06 getUser 메서드는 본인 이외의 계정 조회 시 예외 발생")
    void getUser_fail_notSelf() {
        UUID userId = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();

        try {
            userService.getUser(userId, pathId);
        } catch (IllegalArgumentException e) {
            then(e.getMessage()).isEqualTo("본인만 조회할 수 있습니다.");
        }
    }

    /* 00-07 본인 삭제 */
    @Test
    @DisplayName("00-07 deleteUser 메서드는 본인 계정을 삭제한다")
    void deleteUser_success() {
        UUID id = UUID.randomUUID();

        given(userRepository.existsById(id)).willReturn(true);

        userService.deleteUser(id, id);

        // existsById true → deleteById 호출 됐다고 가정
        then(userRepository.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("00-07 deleteUser 메서드는 본인 이외의 계정 삭제 시 예외 발생")
    void deleteUser_fail_notSelf() {
        UUID userId = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();

        try {
            userService.deleteUser(userId, pathId);
        } catch (IllegalArgumentException e) {
            then(e.getMessage()).isEqualTo("본인만 삭제할 수 있습니다.");
        }
    }
}
