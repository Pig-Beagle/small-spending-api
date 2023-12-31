package com.zzdd.smallspending.config;

import com.zzdd.smallspending.token.JwtAuthenticationFilter;
import com.zzdd.smallspending.token.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .httpBasic().disable()
                .cors().disable()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/user/*").permitAll()
                    .antMatchers("/chat/*").permitAll()
                    .antMatchers("/chat").authenticated()
                    .antMatchers("/post").authenticated()
                    .antMatchers("/post/list/*").permitAll()
                    .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .addFilterBefore(new JwtAuthenticationFilter(secretKey, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
