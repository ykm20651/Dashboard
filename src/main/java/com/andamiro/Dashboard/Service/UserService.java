package com.andamiro.Dashboard.Service;


import com.andamiro.Dashboard.Dto.ApproveRequest;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Entity.CrewMember;
import com.andamiro.Dashboard.Entity.Owner;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.CrewMemberRepository;
import com.andamiro.Dashboard.Repository.OwnerRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //얘가 하는 일은? - 사용자가 입력한 비밀번호(평문) 와 DB에 저장된 비밀번호(해시) 를 비교하는 객체.
    //Spring Security에서 제공하는 암호 해싱/검증 인터페이스dla.
    private final JwtTokenProvider jwtTokenProvider;  // ✅ 주입받아 사용
    private final OwnerRepository ownerRepository;
    private final CrewMemberRepository crewMemberRepository;

    /* 00-00 API 건 매핑 */
    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 없습니다."));

        // 2. 비밀번호 검증
        //passwordEncoder.matches(원문, 해시) → 사용자가 입력한 비번(평문)과 DB에 저장된 해시 비교.
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 3. JWT 토큰 생성 //DB에서 레코드 식별용 PK id를 여기서 끌고오는구나. userId와 User.Role 쌍으로 토큰 생성.
        String token = jwtTokenProvider.createToken(user.getId(), user.getRole());

        // 4. 응답 반환
        return new UserLoginResponse(token, "Bearer", user.getEmail(), user.getRole());
    }


    /* 00-01 API 건 매핑 */
    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        //1. 요청 DTO에 담긴 데이터 가지고, 엔티티 객체 만들어.
        //new 생성자가 아니라 static 팩토리 메서드로 객체 생성하도록 했음.
        User user = User.create(request.email(),  passwordEncoder.encode(request.password()), request.name(), request.role());

        //2. userRepository에 저장해.
        User saved = userRepository.save(user);
        //3. 이제 응답 DTO로 감싸서 반환해. - .stream().map()

        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getRole(),
                saved.isApproved()
        );
    }


    /* 00-02 API 건 매핑 */
    @Transactional
    public void addOwnerInfo(UUID userId, OwnerInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Owner owner = Owner.create(
                user,
                request.companyName(),
                request.shipRegId(),
                request.contactNumber(),
                request.businessNumber()
        );
        ownerRepository.save(owner);
    }

    /* 00-03 API 건 매핑 */
    @Transactional
    public void addCrewInfo(UUID userId, CrewInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Owner owner = ownerRepository.findById(UUID.fromString(request.assignedOwnerId()))
                .orElseThrow(() -> new IllegalArgumentException("소속 선주를 찾을 수 없습니다."));

        CrewMember crew = CrewMember.create(user, owner, request.position());
        crewMemberRepository.save(crew);
    }

    /* 00-04 API 건 매핑 */
    @Transactional
    public void approveUser(UUID id, ApproveRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (request.approved()) {
            user.approve();
        }
    }

    /* 00-05 API 건 매핑 */
    @Transactional
    public UserResponse updateUser(UUID userId, UUID id, UpdateUserRequest request) {
        // 보안 체크: 로그인한 사용자와 PathVariable이 일치하는지 검증
        if (!userId.equals(id)) {
            throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.name() != null && !request.name().isBlank()) {
            user.changeName(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.changePasswordHash(passwordEncoder.encode(request.password()));
        }

        return new UserResponse(user.getId(), user.getEmail(), user.getName(),
                user.getRole(), user.isApproved());
    }

    /* 00-06 API 건 매핑 */
    @Transactional
    public UserResponse getUser(UUID userId, UUID id) {
        if (!userId.equals(id)) {
            throw new IllegalArgumentException("본인만 조회할 수 있습니다.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new UserResponse(user.getId(), user.getEmail(), user.getName(),
                user.getRole(), user.isApproved());
    }

    /* 00-07 API 건 매핑 */
    @Transactional
    public void deleteUser(UUID userId, UUID id) {
        if (!userId.equals(id)) {
            throw new IllegalArgumentException("본인만 삭제할 수 있습니다.");
        }

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        userRepository.deleteById(id);
    }

}
