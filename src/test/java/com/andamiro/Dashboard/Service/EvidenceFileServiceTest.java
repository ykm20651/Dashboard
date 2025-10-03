package com.andamiro.Dashboard.Service;

import com.andamiro.Dashboard.Config.EvidenceFileTestConfig;
import com.andamiro.Dashboard.Dto.EvidenceFileDTO.*;
import com.andamiro.Dashboard.Entity.EvidenceFile;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Repository.EvidenceFileRepository;
import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Util.TestEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(classes = {EvidenceFileService.class, EvidenceFileTestConfig.class})
public class EvidenceFileServiceTest {

    @Autowired
    private EvidenceFileService evidenceFileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private EvidenceFileRepository evidenceFileRepository;

    /* 02-01 API 증거자료 목록 조회 */
    @Test
    @DisplayName("getEvidenceFiles 메서드는 특정 사고에 대한 증거자료 목록을 반환한다")
    void getEvidenceFilesTest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User fakeUser = User.create("test@example.com", "pw", "테스터", User.Role.OWNER);
        TestEntityUtil.forceSetId(fakeUser, "id", userId);

        Incident fakeIncident = Incident.create(
                fakeUser,
                "사고 제목",
                "사고 설명",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );
        TestEntityUtil.forceSetId(fakeIncident, "id", incidentId);

        EvidenceFile file = EvidenceFile.create(fakeIncident, fakeUser, "http://test.jpg", EvidenceFile.FileType.IMAGE);
        TestEntityUtil.forceSetId(file, "id", fileId);

        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(fakeIncident));
        given(evidenceFileRepository.findByIncidentAndIsDeletedFalse(fakeIncident)).willReturn(List.of(file));

        // when
        List<EvidenceFileResponse> result = evidenceFileService.getEvidenceFiles(incidentId);

        // then
        then(result).hasSize(1);
        then(result.get(0).incidentId()).isEqualTo(incidentId);
        then(result.get(0).fileUrl()).isEqualTo("http://test.jpg");
    }

    /* 02-02 API 증거자료 업로드 */
    @Test
    @DisplayName("uploadEvidenceFile 메서드는 요청을 받아 증거자료를 성공적으로 저장한다")
    void uploadEvidenceFileTest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User fakeUser = User.create("test@example.com", "pw", "테스터", User.Role.OWNER);
        TestEntityUtil.forceSetId(fakeUser, "id", userId);

        Incident fakeIncident = Incident.create(
                fakeUser,
                "사고 제목",
                "사고 설명",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );
        TestEntityUtil.forceSetId(fakeIncident, "id", incidentId);

        EvidenceFile file = EvidenceFile.create(fakeIncident, fakeUser, "http://test.jpg", EvidenceFile.FileType.IMAGE);
        TestEntityUtil.forceSetId(file, "id", fileId);

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(incidentRepository.findById(incidentId)).willReturn(Optional.of(fakeIncident));
        given(evidenceFileRepository.save(any(EvidenceFile.class))).willReturn(file);

        EvidenceFileCreateRequest req = new EvidenceFileCreateRequest("http://test.jpg", "IMAGE", "설명");

        // when
        EvidenceFileCreateResponse result = evidenceFileService.uploadEvidenceFile(incidentId, userId, req);

        // then
        then(result.incidentId()).isEqualTo(incidentId);
        then(result.uploaderId()).isEqualTo(userId);
        then(result.fileUrl()).isEqualTo("http://test.jpg");
    }

    /* 02-03 API 증거자료 삭제 */
    @Test
    @DisplayName("deleteEvidenceFile 메서드는 특정 증거자료를 삭제 상태로 만든다")
    void deleteEvidenceFileTest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User fakeUser = User.create("test@example.com", "pw", "테스터", User.Role.OWNER);
        TestEntityUtil.forceSetId(fakeUser, "id", userId);

        Incident fakeIncident = Incident.create(
                fakeUser,
                "사고 제목",
                "사고 설명",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );

        EvidenceFile file = EvidenceFile.create(fakeIncident, fakeUser, "http://test.jpg", EvidenceFile.FileType.IMAGE);
        TestEntityUtil.forceSetId(file, "id", fileId);

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(evidenceFileRepository.findById(fileId)).willReturn(Optional.of(file));

        // when
        evidenceFileService.deleteEvidenceFile(userId, fileId);

        // then
        then(file.isDeleted()).isTrue();
    }

    /* 02-04 API 증거자료 수정 */
    @Test
    @DisplayName("updateEvidenceFile 메서드는 특정 증거자료 설명을 수정한다")
    void updateEvidenceFileTest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User fakeUser = User.create("test@example.com", "pw", "테스터", User.Role.OWNER);
        TestEntityUtil.forceSetId(fakeUser, "id", userId);

        Incident fakeIncident = Incident.create(
                fakeUser,
                "사고 제목",
                "사고 설명",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );

        EvidenceFile file = EvidenceFile.create(fakeIncident, fakeUser, "http://test.jpg", EvidenceFile.FileType.IMAGE);
        TestEntityUtil.forceSetId(file, "id", fileId);

        given(userRepository.findById(userId)).willReturn(Optional.of(fakeUser));
        given(evidenceFileRepository.findById(fileId)).willReturn(Optional.of(file));

        EvidenceFileUpdateRequest req = new EvidenceFileUpdateRequest("수정된 설명");

        // when
        EvidenceFileUpdateResponse result = evidenceFileService.updateEvidenceFile(userId, fileId, req);

        // then
        then(result.description()).isEqualTo("수정된 설명");
        then(file.getDescription()).isEqualTo("수정된 설명");
    }
}
