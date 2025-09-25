package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest                // JPA 관련 Bean만 로딩 (Repository, EntityManager 등)
@ActiveProfiles("test")     // application-test.yml 적용 보장
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  //DataJpaTest → TestDatabaseAutoConfiguration → H2 등 임베디드 DB로 대체 시도
//MySQL 연결하고 싶음 → replace = NONE으로 지정하면 application-test.yml의 설정을 그대로 사용합니다.
public class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByCreatorId는 특정 사용자가 등록한 사건만 반환한다")
    void findByCreatorIdTest() {
        // given
        User owner1 = userRepository.save(User.create("a@test.com", "pw", "홍길동", User.Role.OWNER));
        User owner2 = userRepository.save(User.create("b@test.com", "pw", "김철수", User.Role.OWNER));

        incidentRepository.save(
                Incident.create(owner1, "유류 유출", "기관실 기름 유출", Incident.IncidentType.OIL_SPILL, "부산항", LocalDateTime.now())
        );
        incidentRepository.save(
                Incident.create(owner2, "화재", "기관실 화재", Incident.IncidentType.FIRE, "울산항", LocalDateTime.now())
        );

        // when
        List<Incident> result = incidentRepository.findByCreatorId(owner1.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("유류 유출");
        assertThat(result.get(0).getCreator().getName()).isEqualTo("홍길동");
    }
}
