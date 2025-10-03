package com.andamiro.Dashboard.Config;


import com.andamiro.Dashboard.Repository.CrewMemberRepository;
import com.andamiro.Dashboard.Repository.OwnerRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Security.JwtTokenProvider;
import com.andamiro.Dashboard.Service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration //테스트 전용 스프링 설정 클래스 어노테이션
public class UserTestConfig {

    /* @TestConfiguration + @Bean + @Primary를 붙여 놓은 이유는?
    1. 여기에 정의한 Bean들은 @SpringBootTest, @DataJpaTest 같은 테스트 실행할 때만 ApplicationContext에 올라감.
    (@SpringBootTest(classes = {IncidentService.class, IncidentTestConfig.class})
    (@DataJpaTest ->JPA 관련 Bean만 로딩 (Repository, EntityManager 등))

    2. @Primary로 우선순위 지정하여 같은 데이터 타입의 Bean이 올라가면 DI 주입 시 충돌이 일어나느데 이걸 해결함.
    따라서 main코드에 UserService 타입의 Bean등록이 되어있어도 테스트 환경에서 mock 객체를 반환하여
    테스트 전용으로 미리 준비한 mock 객체들이 들어감.
     */

    @Bean
    @Primary //Service 계층에서 Bean 등록을 위함.
    public UserRepository mockUserRepository()
    {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public OwnerRepository mockOwnerRepository()
    {
        return Mockito.mock(OwnerRepository.class);
    }

    @Bean
    @Primary
    public CrewMemberRepository mockCrewMemberRepository()
    {
        return Mockito.mock(CrewMemberRepository.class);
    }

    @Bean
    @Primary
    public JwtTokenProvider mockJwtTokenProvider()
    {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean
    @Primary
    public PasswordEncoder mockPasswordEncoder()
    {
        return Mockito.mock(PasswordEncoder.class);
    }


}
