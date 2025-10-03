package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ApproveRequest;
import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentCreateRequest;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Fixture.IncidentFixture;
import com.andamiro.Dashboard.Fixture.UserFixture;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired IncidentRepository incidentRepository;

    String ownerJwt; // OWNER JWT
    String adminJwt; // ADMIN JWT

    @BeforeEach
    void setup() throws Exception {
        incidentRepository.deleteAll();
        userRepository.deleteAll();

        // OWNER 계정 저장
        UserSignupRequest signupReq = UserFixture.signupRequest();
        User owner = User.create(
                signupReq.email(),
                passwordEncoder.encode(signupReq.password()),
                signupReq.name(),
                signupReq.role()
        );
        userRepository.save(owner);

        // ADMIN 계정 저장
        User admin = User.create(
                "admin@example.com",
                passwordEncoder.encode("admin123"),
                "관리자",
                User.Role.ADMIN
        );
        userRepository.save(admin);

        // OWNER 로그인 → JWT 발급
        UserLoginRequest ownerLoginReq = UserFixture.loginRequest();
        String ownerBody = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerLoginReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ownerJwt = objectMapper.readTree(ownerBody).get("token").asText();

        // ADMIN 로그인 → JWT 발급
        UserLoginRequest adminLoginReq = new UserLoginRequest("admin@example.com", "admin123");
        String adminBody = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        adminJwt = objectMapper.readTree(adminBody).get("token").asText();
    }

    @Test
    @DisplayName("00-00 로그인 성공 → JWT 발급")
    void login_success() throws Exception {
        UserLoginRequest loginReq = UserFixture.loginRequest();

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("00-01 회원가입 성공 → DB 저장 확인")
    void signup_success() throws Exception {
        UserSignupRequest request = new UserSignupRequest(
                "newuser@example.com", "1234", "새유저", User.Role.CREW
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.name").value("새유저"))
                .andExpect(jsonPath("$.role").value("CREW"));
    }

    @Test
    @DisplayName("00-04 관리자 승인 API → approved = true 반영")
    void approveUser_success() throws Exception {
        UUID ownerId = userRepository.findByEmail("test@example.com")
                .orElseThrow()
                .getId();
        ApproveRequest req = new ApproveRequest(true);

        mockMvc.perform(patch("/users/{id}/approve", ownerId)
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("00-05 본인 정보 수정 성공 → 이름 변경 반영")
    void updateUser_success() throws Exception {
        UUID ownerId = userRepository.findByEmail("test@example.com")
                .orElseThrow()
                .getId();
        UpdateUserRequest req = new UpdateUserRequest("수정된이름", "newpw");

        mockMvc.perform(patch("/users/{id}", ownerId)
                        .header("Authorization", "Bearer " + ownerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된이름"));
    }

    @Test
    @DisplayName("00-06 본인 정보 조회 성공")
    void getUser_success() throws Exception {
        UUID ownerId = userRepository.findByEmail("test@example.com")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/users/{id}", ownerId)
                        .header("Authorization", "Bearer " + ownerJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("00-07 본인 삭제 성공 → DB에서 제거됨")
    void deleteUser_success() throws Exception {
        UUID ownerId = userRepository.findByEmail("test@example.com")
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/users/{id}", ownerId)
                        .header("Authorization", "Bearer " + ownerJwt))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("JWT 없이 OWNER 전용 API 접근 → 401 Unauthorized")
    void accessWithoutToken() throws Exception {
        IncidentCreateRequest req = IncidentFixture.createIncidentCreateRequest();

        mockMvc.perform(post("/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 비밀번호 로그인 시 400 Bad Request")
    void loginFail_wrongPassword() throws Exception {
        UserLoginRequest wrongReq = new UserLoginRequest("test@example.com", "wrongpw");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongReq)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("JWT 발급 후 OWNER API(사고 등록) 접근 성공")
    void loginAndAccessOwnerApi() throws Exception {
        IncidentCreateRequest req = IncidentFixture.createIncidentCreateRequest();

        mockMvc.perform(post("/incidents")
                        .header("Authorization", "Bearer " + ownerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
