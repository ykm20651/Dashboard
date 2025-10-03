package com.andamiro.Dashboard.Config;


import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ReportRepository;
import com.andamiro.Dashboard.Repository.ResponseGuideRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ResponseGuideTestConfig {
    @Bean
    @Primary
    public ResponseGuideRepository responseGuideRepository() {
        return Mockito.mock(ResponseGuideRepository.class);
    }


    @Bean
    @Primary
    public IncidentRepository incidentRepository() {
        return Mockito.mock(IncidentRepository.class);
    }

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public ReportRepository reportRepository() {
        return Mockito.mock(ReportRepository.class);
    }


}
