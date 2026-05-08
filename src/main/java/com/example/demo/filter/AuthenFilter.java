package com.example.demo.filter;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Component
public class AuthenFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthenFilter(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.validateAT(token);
            log.info("Claims: {}", claims);

            String email = claims.get("email", String.class);
            String companyId = claims.get("companyId", String.class);
            String roles = claims.get("roles", String.class);
            String permissions = claims.get("permissions", String.class);

            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Add ROLE_ authorities (e.g. ROLE_EMPLOYER)
            if (roles != null && !roles.isBlank()) {
                Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .filter(r -> !r.isBlank())
                        .forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
            }

            // Add PERMISSION_ authorities (e.g. PERMISSION_JOB_CREATE)
            if (permissions != null && !permissions.isBlank()) {
                Arrays.stream(permissions.split(","))
                        .map(String::trim)
                        .filter(p -> !p.isBlank())
                        .forEach(p -> authorities.add(new SimpleGrantedAuthority("PERMISSION_" + p)));
            }

            CustomUserDetail customUserDetail = new CustomUserDetail(email, "", authorities, companyId);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetail,
                    null,
                    customUserDetail.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            response.setStatus(401);
            response.getWriter().write("Token expired");
        } catch (Exception e) {
            log.error("error : {}", e.getMessage());
            response.setStatus(401);
            response.getWriter().write("Invalid token");
        }
    }
}
