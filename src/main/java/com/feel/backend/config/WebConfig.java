package com.feel.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:3000",
                "https://m-se0k.github.io",
                "https://m-se0k.github.io/FeeL_WEB"
                // 배포 후 서버 IP를 추가하세요: "http://YOUR_SERVER_IP:3000"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);

        // 업로드된 이미지 파일 접근을 위한 CORS 설정
        registry.addMapping("/uploads/**")
            .allowedOrigins(
                "http://localhost:3000",
                "https://m-se0k.github.io",
                "https://m-se0k.github.io/FeeL_WEB"
                // 배포 후 서버 IP를 추가하세요: "http://YOUR_SERVER_IP:3000"
            )
            .allowedMethods("GET", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일을 정적 리소스로 제공
        // 절대 경로로 설정하여 어디서든 접근 가능하도록 함
        String absolutePath = new java.io.File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:///" + absolutePath.replace("\\", "/") + "/");
    }
}
