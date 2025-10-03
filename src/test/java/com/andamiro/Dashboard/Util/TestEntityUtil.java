package com.andamiro.Dashboard.Util;

import java.lang.reflect.Field;

/*
* JPA는 DB에 실제 insert해야 레코드 식별용 PK로 선언한 @GeneratedValue 붙은 id가 삽입되기 시작함.
* 그래서 Service 단에서 테스트할 때, 현재 로그인된 사용자 userId와 엔티티 id를 비교할 때, 삽입할 수 있도록
* forceSetId 메서드를 만들었음.
 */

public class TestEntityUtil {

    public static void forceSetId(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("리플렉션으로 ID 세팅 실패", e);
        }
    }
}

