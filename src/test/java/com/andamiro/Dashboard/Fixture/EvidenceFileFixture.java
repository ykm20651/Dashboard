package com.andamiro.Dashboard.Fixture;

import com.andamiro.Dashboard.Dto.EvidenceFileDTO.*;
import com.andamiro.Dashboard.Entity.EvidenceFile;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.andamiro.Dashboard.Util.TestEntityUtil.forceSetId;

public class EvidenceFileFixture {

    public static User createTestUser(UUID id) {
        User user = User.create("test@example.com", "pw", "테스터", User.Role.OWNER);
        forceSetId(user, "id", id); // 리플렉션 유틸로 ID 주입
        return user;
    }

    public static Incident createIncident(User user, UUID id) {
        Incident incident = Incident.create(
                user,
                "테스트 사고",
                "테스트 설명",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );
        forceSetId(incident, "id", id);
        return incident;
    }

    public static EvidenceFile createEvidenceFile(Incident incident, User uploader, UUID id) {
        EvidenceFile file = EvidenceFile.create(
                incident,
                uploader,
                "http://localhost/test.jpg",
                EvidenceFile.FileType.IMAGE
        );
        forceSetId(file, "id", id);
        return file;
    }

    public static EvidenceFileCreateRequest createRequest() {
        return new EvidenceFileCreateRequest(
                "http://localhost/test.jpg",
                "IMAGE",
                "테스트 파일입니다"
        );
    }

    public static EvidenceFileUpdateRequest updateRequest() {
        return new EvidenceFileUpdateRequest("수정된 설명");
    }

}
