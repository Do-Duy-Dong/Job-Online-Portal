package com.example.demo.config;

import com.example.demo.filter.AuthenFilter;
import com.example.demo.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenFilter authenFilter;
    private final UserDetailsService userDetailsService;
    public SecurityConfig(AuthenFilter authenFilter,UserDetailsService userDetailsService){
        this.userDetailsService= userDetailsService;
        this.authenFilter = authenFilter;
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider= new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain authenFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth ->auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/file/look/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/employer/**").hasRole("EMPLOYER")

                        .anyRequest().authenticated())
//                .authenticationProvider(authenticationProvider)
                .addFilterBefore(authenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
