package com.andamiro.Dashboard.Fixture;


import com.andamiro.Dashboard.Dto.ApproveRequest;
import com.andamiro.Dashboard.Dto.UserDTO.*;
import com.andamiro.Dashboard.Entity.User;
import com.andamiro.Dashboard.Util.TestEntityUtil;

import java.util.UUID;

public class UserFixture {
    public static User createTestUser(UUID id) {
        User user = User.create("test@example.com", "password123", "테스트유저", User.Role.OWNER);
        TestEntityUtil.forceSetId(user, "id", id);
        return user;
    }


    public static UserSignupRequest signupRequest() {
        return new UserSignupRequest(
                "test@example.com",
                "password123",
                "테스트유저",
                User.Role.OWNER
        );
    }

    public static UserLoginRequest loginRequest() {
        return new UserLoginRequest(
                "test@example.com",
                "password123"
        );
    }

    public static OwnerInfoRequest ownerInfoRequest() {
        return new OwnerInfoRequest(
                "테스트해운회사",
                "SHIP-1234",
                "010-1234-5678",
                "BIZ-98765"
        );
    }

    public static CrewInfoRequest crewInfoRequest(UUID ownerId) {
        return new CrewInfoRequest(
                ownerId.toString(),
                "선원"
        );
    }

    public static UpdateUserRequest updateUserRequest() {
        return new UpdateUserRequest(
                "새이름",
                "newpassword123"
        );
    }

    public static ApproveRequest approveRequest() {
        return new ApproveRequest(true);
    }
}
