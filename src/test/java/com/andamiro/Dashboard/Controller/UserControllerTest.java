package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Config.UserTestConfig;
import com.andamiro.Dashboard.Dto.ApproveRequest;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Fixture.UserFixture;
import com.andamiro.Dashboard.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    /* 00-00 로그인 */
    @Test
    @DisplayName("00-00 로그인 성공")
    void login_success() throws Exception {
        UserLoginRequest request = UserFixture.loginRequest();
        UserLoginResponse response = new UserLoginResponse(
                "fake-jwt-token", "Bearer", request.email(), User.Role.OWNER
        );

        given(userService.login(any(UserLoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    /* 00-01 회원가입 */
    @Test
    @DisplayName("00-01 회원가입 성공")
    void signup_success() throws Exception {
        UserSignupRequest request = UserFixture.signupRequest();
        UserResponse response = new UserResponse(
                UUID.randomUUID(), request.email(), request.name(), request.role(), false
        );

        given(userService.signup(any(UserSignupRequest.class))).willReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.isApproved").value(false));
    }

    /* 00-02 선주 추가 정보 입력 */
    @Test
    @DisplayName("00-02 선주 추가 정보 입력 성공")
    void addOwnerInfo_success() throws Exception {
        UUID id = UUID.randomUUID();
        OwnerInfoRequest request = UserFixture.ownerInfoRequest();

        doNothing().when(userService).addOwnerInfo(eq(id), any(OwnerInfoRequest.class));

        mockMvc.perform(post("/users/{id}/owner-info", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /* 00-03 선원 추가 정보 입력 */
    @Test
    @DisplayName("00-03 선원 추가 정보 입력 성공")
    void addCrewInfo_success() throws Exception {
        UUID id = UUID.randomUUID();
        CrewInfoRequest request = UserFixture.crewInfoRequest(UUID.randomUUID());

        doNothing().when(userService).addCrewInfo(eq(id), any(CrewInfoRequest.class));

        mockMvc.perform(post("/users/{id}/crew-info", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /* 00-04 승인 */
    @Test
    @DisplayName("00-04 승인 성공")
    void approveUser_success() throws Exception {
        UUID id = UUID.randomUUID();
        ApproveRequest request = UserFixture.approveRequest();

        doNothing().when(userService).approveUser(eq(id), any(ApproveRequest.class));

        mockMvc.perform(patch("/users/{id}/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /* 00-05 본인 수정 */
    @Test
    @DisplayName("00-05 본인 수정 성공")
    void updateUser_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserRequest request = UserFixture.updateUserRequest();
        UserResponse response = new UserResponse(
                id, "test@example.com", request.name(), User.Role.CREW, true
        );

        given(userService.updateUser(Mockito.<UUID>any(), Mockito.<UUID>any(), Mockito.any(UpdateUserRequest.class)))
                .willReturn(response);

        mockMvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.role").value("CREW"))
                .andExpect(jsonPath("$.isApproved").value(true));
    }

    /* 00-06 본인 조회 */
    @Test
    @DisplayName("00-06 본인 조회 성공")
    void getUser_success() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse(
                id, "test@example.com", "조회유저", User.Role.OWNER, true
        );

        given(userService.getUser(Mockito.<UUID>any(), Mockito.<UUID>any())).willReturn(response);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("조회유저"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.isApproved").value(true));
    }

    /* 00-07 본인 삭제 */
    @Test
    @DisplayName("00-07 본인 삭제 성공")
    void deleteUser_success() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(userService).deleteUser(any(UUID.class), eq(id));

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());
    }
}
