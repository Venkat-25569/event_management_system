    package com.events.event_management.config;

import com.events.event_management.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/pages/**").permitAll()
                .requestMatchers("/login.html", "/register.html", "/", "/index.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                
                // ✅ Rubrics HTML Pages - PUBLIC ACCESS
                .requestMatchers("/rubrics-test.html").permitAll()
                .requestMatchers("/rubric-builder.html").permitAll()
                .requestMatchers("/judge-scoring.html").permitAll()
                .requestMatchers("/live-scoreboard.html").permitAll()
                .requestMatchers("/rubrics-admin.html").permitAll()
                .requestMatchers("/student-rubrics.html").permitAll()
                
                // ✅ RUBRICS API ENDPOINTS - PUBLIC ACCESS
                .requestMatchers("/api/rubrics/**").permitAll()
                .requestMatchers("/api/participants/**").permitAll()
                .requestMatchers("/api/judges/**").permitAll()
                .requestMatchers("/api/scores/**").permitAll()
                .requestMatchers("/api/events/*/rubrics/**").permitAll()
                
                // Dashboard endpoints - role-based
                .requestMatchers("/api/dashboard/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/dashboard/hod/**").hasAnyAuthority("HOD", "ADMIN")
                .requestMatchers("/api/dashboard/coordinator/**").hasAnyAuthority("EVENT_COORDINATOR", "ADMIN")
                .requestMatchers("/api/dashboard/student/**").hasAnyAuthority("STUDENT", "ADMIN")
                
                // Events - role-based access
                .requestMatchers("/api/events/create").hasAnyAuthority("ADMIN", "EVENT_COORDINATOR")
                .requestMatchers("/api/events/*/delete").hasAuthority("ADMIN")
                .requestMatchers("/api/events/*/approve").hasAnyAuthority("ADMIN", "HOD", "EVENT_COORDINATOR")
                .requestMatchers("/api/events/*/reject").hasAnyAuthority("ADMIN", "HOD", "EVENT_COORDINATOR")
                .requestMatchers("/api/events/**").permitAll()  // 🔧 CHANGED: Allow public access to /api/events for attendance page
                
                // Clubs - ADMIN only for create/edit/delete
                .requestMatchers("/api/clubs/create").hasAuthority("ADMIN")
                .requestMatchers("/api/clubs/*/edit").hasAuthority("ADMIN")
                .requestMatchers("/api/clubs/*/delete").hasAuthority("ADMIN")
                .requestMatchers("/api/clubs/**").authenticated()
                
                // ✅ ATTENDANCE ENDPOINTS - AUTHENTICATED (but allow all authenticated users)
                .requestMatchers("/api/attendance/**").permitAll()  // 🔧 CHANGED: Allow public access for attendance
                .requestMatchers("/api/registrations/**").authenticated()
                .requestMatchers("/api/payments/**").authenticated()
                .requestMatchers("/api/qrcode/**").authenticated()
                .requestMatchers("/api/certificate/**").authenticated()
                .requestMatchers("/api/export/**").authenticated()
                
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}