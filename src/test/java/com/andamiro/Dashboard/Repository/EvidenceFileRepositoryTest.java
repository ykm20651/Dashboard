package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.EvidenceFile;
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

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EvidenceFileRepositoryTest {

    @Autowired
    private EvidenceFileRepository evidenceFileRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByIncidentAndIsDeletedFalse는 특정 사건에 속한 삭제되지 않은 증거자료만 반환한다")
    void findByIncidentAndIsDeletedFalseTest() {
        // given
        User uploader = userRepository.save(
                User.create("uploader@test.com", "pw", "업로더", User.Role.CREW)
        );

        Incident incident = incidentRepository.save(
                Incident.create(
                        uploader,
                        "유류 유출",
                        "기관실에서 기름 유출",
                        Incident.IncidentType.OIL_SPILL,
                        "부산항",
                        LocalDateTime.now()
                )
        );

        // 삭제되지 않은 증거자료
        EvidenceFile file1 = evidenceFileRepository.save(
                EvidenceFile.create(incident, uploader, "http://alive.jpg", EvidenceFile.FileType.IMAGE)
        );

        // 삭제된 증거자료
        EvidenceFile file2 = evidenceFileRepository.save(
                EvidenceFile.create(incident, uploader, "http://deleted.jpg", EvidenceFile.FileType.IMAGE)
        );
        file2.delete();
        evidenceFileRepository.save(file2); // 삭제 플래그 반영

        // when
        List<EvidenceFile> result = evidenceFileRepository.findByIncidentAndIsDeletedFalse(incident);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileUrl()).isEqualTo("http://alive.jpg");
        assertThat(result.get(0).isDeleted()).isFalse();
    }
}
