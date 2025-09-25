package com.andamiro.Dashboard.Fixture;

import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentCreateRequest;
import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentUpdateRequest;
import com.andamiro.Dashboard.Entity.Incident;
import com.andamiro.Dashboard.Entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class IncidentFixture {

    /*요청 DTO 객체 생성 */
    public static IncidentCreateRequest createIncidentCreateRequest(UUID userId) {
        return new IncidentCreateRequest(
                userId,
                "유류 유출",
                "기관실에서 기름이 유출됨",
                "OIL_SPILL",
                "부산항",
                LocalDateTime.now()
        );
    }

    public static IncidentUpdateRequest createIncidentUpdateRequest() {
        return new IncidentUpdateRequest(
                "수정된 제목",
                "수정된 설명 ~ ~",
                "울산항"
        );
    }

    /*엔티티 객체 생성 */
    //->ServiceTest에서 Incident 엔티티를 직접 만들어 써야 할 때 중복 줄이려고 추가한 것.
    public static Incident oilSpillIncident(User owner) {
        return Incident.create(
                owner,
                "유류 유출",
                "기관실에서 기름이 유출됨",
                Incident.IncidentType.OIL_SPILL,
                "부산항",
                LocalDateTime.now()
        );
    }
}
