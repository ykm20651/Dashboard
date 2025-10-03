package com.andamiro.Dashboard.Config;

import com.andamiro.Dashboard.Repository.IncidentRepository;
import com.andamiro.Dashboard.Repository.ReportRepository;
import com.andamiro.Dashboard.Repository.UserRepository;
import com.andamiro.Dashboard.Service.ReportService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ReportTestConfig {

    @Bean
    @Primary
    public ReportRepository reportRepository() {
        return Mockito.mock(ReportRepository.class);
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



}
