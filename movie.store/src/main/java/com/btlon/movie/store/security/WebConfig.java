package com.btlon.movie.store.security;

import com.btlon.movie.store.service.ImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final CurrentUserArgumentResolver currentUserResolver;
    private final ImageStorageService images;

    public WebConfig(AuthInterceptor authInterceptor, CurrentUserArgumentResolver currentUserResolver,
                     ImageStorageService images) {
        this.authInterceptor = authInterceptor;
        this.currentUserResolver = currentUserResolver;
        this.images = images;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = images.getDirectory().toUri().toString();
        if (!location.endsWith("/")) location += "/";

        // URL ảnh được ánh xạ trực tiếp tới thư mục upload trên máy chủ.
        registry.addResourceHandler("/api/uploads/images/**")
                .addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }
}
