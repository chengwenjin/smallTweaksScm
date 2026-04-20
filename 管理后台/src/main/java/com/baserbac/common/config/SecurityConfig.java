package com.baserbac.common.config;

import com.baserbac.security.AccessDeniedHandlerImpl;
import com.baserbac.security.AuthenticationEntryPointImpl;
import com.baserbac.security.JwtAuthenticationFilter;
import com.baserbac.security.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final PermissionInterceptor permissionInterceptor;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            // 设置会话管理为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许公开访问的接口
                .requestMatchers(
                    "/api/auth/**",           // 认证相关接口
                    "/api/dev/**",            // 开发工具接口
                    "/api/test/**",           // 测试接口
                    "/doc.html",              // Knife4j文档首页
                    "/swagger-ui/**",         // Swagger UI资源
                    "/v3/api-docs/**",        // OpenAPI文档
                    "/webjars/**"             // Knife4j静态资源
                ).permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)  // 401处理器
                .accessDeniedHandler(accessDeniedHandler)            // 403处理器
            )
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * 注册权限拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                    "/api/auth/captcha",     // 验证码
                    "/api/auth/login"        // 登录
                );
    }
}
