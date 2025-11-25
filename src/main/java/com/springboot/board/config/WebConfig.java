package com.springboot.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * WebConfig 클래스는 CORS 설정을 위한 구성 클래스
 * 이 클래스는 WebMvcConfigurer 인터페이스를 구현하여
 * 웹 애플리케이션의 CORS 정책을 정의
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 매핑을 추가하는 메서드
     * 
     * @param registry CORS 설정을 위한 CorsRegistry 객체
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 CORS 설정 적용
                .allowedOrigins(
                        "http://43.202.10.10:3000/", // Next.js 개발 서버
                        "http://43.202.10.10:8080/", // Spring Boot 개발 서버,
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://korea-sky-planner.com",
                        "http://www.korea-sky-planner.com",
                        "https://korea-sky-planner.com",
                        "https://www.korea-sky-planner.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 인증 정보 허용
                .maxAge(3600); // preflight 캐시 시간 (1시간)
    }
}