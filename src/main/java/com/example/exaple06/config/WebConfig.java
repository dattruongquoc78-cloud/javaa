package com.example.exaple06.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // SỬA: Trỏ đến đường dẫn tuyệt đối
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/exaple06/uploads/");
    }
}
