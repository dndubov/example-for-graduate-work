package ru.skypro.homework.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности приложения.
 * Отвечает за настройку аутентификации, авторизации,
 * параметров сессии и базовой схемы HTTP-доступа.
 */

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ЯВНО выбираем нужный UserDetailsService
    @Bean
    public DaoAuthenticationProvider authProvider(
            @Qualifier("customUserDetailsService") UserDetailsService uds
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Настраивает цепочку фильтров Spring Security.
     * <p>
     * Здесь:
     * <ul>
     *     <li>отключается CSRF для REST-подхода;</li>
     *     <li>включается CORS;</li>
     *     <li>задаются правила доступа к эндпоинтам;</li>
     *     <li>используется stateless-схема без серверных сессий;</li>
     *     <li>включается HTTP Basic для аутентификации.</li>
     * </ul>
     *
     * @param http объект конфигурации HTTP-безопасности
     * @return настроенная цепочка фильтров
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .cors()         // CORS включён, конфиг берётся из corsConfigurationSource()
                .and()

                .httpBasic()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // разрешаем login
                .antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.POST, "/register").permitAll()
                .antMatchers(HttpMethod.GET, "/ads", "/ads/**").permitAll()
                .antMatchers(HttpMethod.GET, "/images/**").permitAll()
                .anyRequest().authenticated();

        return http.build();
    }

    /**
     * Глобальная CORS-конфигурация.
     * Разрешает обращение к API с фронтенда и
     * задаёт допустимые методы и заголовки.
     *
     * @return источник CORS-настроек
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Для диплома можно не ограничиваться конкретным Origin — пускаем всех
        // Если нужно жёстче: List.of("http://localhost:3000")
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
