package com.example.demo.filter;

import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.example.demo.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class AuthenFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
//    private final AuthService authService;
    private final UserDetailsService userDetailsService;
    public AuthenFilter(JWTUtil jwtUtil, UserDetailsService userDetailsService){
        this.jwtUtil= jwtUtil;
//        this.authService= authService;
        this.userDetailsService= userDetailsService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader= request.getHeader("Authorization");
        if(authHeader ==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        String token= authHeader.substring(7);
        try{
            Claims claims= jwtUtil.validateAT(token);
            log.info("Claims: {}",claims);
            String email= claims.getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//COnfig authen user của spring cung cấp nhiều method bảo mật hơn
            UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

//            Thêm thông tin về địa chir IP của user vào authen
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request,response);
        }
        catch (ExpiredJwtException e){
            response.setStatus(401);
            response.getWriter().write("Token expired");
        }
        catch (Exception e){
            log.error("error : {}",e.getMessage());
            response.setStatus(401);
            response.getWriter().write("Invalid token");
        }
    }
}
