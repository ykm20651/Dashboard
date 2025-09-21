package com.andamiro.Dashboard.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "owners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Owner {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    //@GeneratedValue(strategy = GenerationType.IDENTITY) -> 즉, Owner가 독립적으로 PK를 생성할 일이 없고, 항상 User의 id를 FK 겸 PK로 받아야해서 Generated 쓸 필요 없음. 공유 PK 방식에서는 ㅇㅇ.
    private UUID id;


    @OneToOne
    @MapsId
    @JoinColumn(name = "id", columnDefinition = "CHAR(36)") //name 속성은 현재 엔티티(Owner) 테이블에 만들어질 FK 컬럼 이름을 말함.
    //FK는 기본적으로 상대 엔티티의 PK를 참조한다.
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "ship_reg_id",unique = true, nullable = false)
    private String shipRegId;

    private boolean verified;

    @Column(name="contact_number", nullable = false)
    private String contactNumber;

    @Column(name="business_number", nullable = false)
    private String businessNumber;

    @OneToMany(mappedBy = "assignedOwner")
    private List<CrewMember> crewMembers = new ArrayList<>();

    public static Owner create(User user, String companyName, String shipRegId, String contactNumber, String businessNumber) {
        Owner owner = new Owner(); //이미 @NoArgsConstructor(access = AccessLevel.PROTECTED)로 막아뒀으니까 외부에서 new Owner() 직접 못 씀.
        owner.user = user; //넘겨질 때 User 엔티티 정보 다 넘겨가는데, SQL차원에서는 또 User 테이블의 PK인 id만 참조한다고 하네.
        owner.companyName = companyName;
        owner.shipRegId = shipRegId;
        owner.contactNumber = contactNumber;
        owner.businessNumber = businessNumber;
        return owner;
    }

    // ──────────────── 도메인 메서드 ────────────────
    public void verify() {
        this.verified = true;
    }
}
