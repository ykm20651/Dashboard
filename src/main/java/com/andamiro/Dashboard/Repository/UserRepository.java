package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    //Jpa 상속받은 각 Repository 인터페이스에서 따로 내가 만들 함수를 Impl로 구현하기 마련인데,
    // 추후 커스텀 쿼리 필요할 때 Impl 만들면 됨.
    boolean existsByEmail(String email);
}
