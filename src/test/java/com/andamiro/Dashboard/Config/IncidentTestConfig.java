package com.andamiro.Dashboard.Config;

import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Service.IncidentService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration //테스트 전용 설정 클래스임을 알린다.
public class IncidentTestConfig {
    //여기서 incident관련 Service, Controller 테스트 진행할 때 @MockitoBean을 쓰면 매번 Application Context를 갈아치우니까
    //@TestConfiguration + Bean,primary로 아예 Context에 빈 주입 생성 당시 가짜 객체를 주입하기.

    @Bean //스프링 컨테이너가 뜰 때, 이 메서드 실행 → 리턴값을 컨테이너에 넣어줌. 그래서 @Autowired나 생성자 주입으로 이 객체를 받아 쓸 수 있게 됨. -> IncidentRepository 타입의 Bean이 등록됨.
    @Primary //스프링 컨테이너에 IncidentRepository 타입 Bean이 여러 개 있을 수 있음. -> 우선순위 먼저 부여.
    public IncidentRepository mockIncidentRepository() {
        //Mockito 라이브러리가 제공하는 정적 메서드. IncidentRepository의 가짜 구현체(Mock 객체)를 만들어서 리턴함.
        //실제 DB와 연결하지 않아 DB 의존성을 덜어내고, given(), willReturn(). . 같은 코드로 지정할 수 있음.
        return Mockito.mock(IncidentRepository.class);
    }

    @Bean //스프링 컨텍스트에 Mock 객체를 Bean으로 등록.
    @Primary
    public UserRepository mockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public IncidentService mockIncidentService() {
        return Mockito.mock(IncidentService.class);
    }




}
