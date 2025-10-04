package com.andamiro.Dashboard.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {

    /* 메인 페이지 */
    @GetMapping("/")
    public String index() {
        return "forward:/static/index.html";
    }

    /* 로그인 페이지 */
    @GetMapping("/login")
    public String login() {
        return "forward:/static/login.html";
    }

    /* 회원가입 페이지 */
    @GetMapping("/signup")
    public String signup() {
        return "forward:/static/signup.html";
    }

    /* 사고 목록 페이지 (01-01 API) */
    @GetMapping("/incidents")
    public String incidents() {
        return "forward:/static/incidents.html";
    }

    /* 사고 등록 페이지 (01-02 API) */
    @GetMapping("/incident-register")
    public String incidentRegister() {
        return "forward:/static/incident_register.html";
    }

    /* 사고 상세 페이지 (01-03 API) */
    @GetMapping("/incident-detail")
    public String incidentDetail() {
        return "forward:/static/incident_detail.html";
    }

    /* 보고서 페이지 (03-01~03-02 API) */
    @GetMapping("/report")
    public String report() {
        return "forward:/static/report.html";
    }

    /* 증거자료 페이지 (02-01~02-04 API) */
    @GetMapping("/evidence")
    public String evidence() {
        return "forward:/static/evidence.html";
    }

    /* 대응 가이드 페이지 (04-01~04-02 API) */
    @GetMapping("/response-guide")
    public String responseGuide() {
        return "forward:/static/response_guide.html";
    }
}
