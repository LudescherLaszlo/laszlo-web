package com.mrwhite.config;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String allowedOrigin;
    public CorsConfig(@Value("${mrwhite.allowedOrigin:http://localhost:5173}") String allowedOrigin){this.allowedOrigin=allowedOrigin;}
    @Override public void addCorsMappings(CorsRegistry r){r.addMapping("/api/**").allowedOrigins(allowedOrigin.split(",")).allowedMethods("*");}
}
