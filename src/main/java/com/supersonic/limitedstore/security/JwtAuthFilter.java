package com.supersonic.limitedstore.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal (HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 헤더가 없거나 제대로 안 왔으면 그 다음 필터 진행
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 제거
        String token = authHeader.substring(7);

        // 토큰이 유효한 경우에만 인증 정보 생성
        if (jwtTokenProvider.validate(token)) {

            Claims claims = jwtTokenProvider.getClaims(token);

            String email = claims.get("email", String.class);
            String memberId = claims.get("memberId", String.class);

            request.setAttribute("email", email);
            request.setAttribute("memberId", memberId);

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, null);

            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 스프링 시큐리티에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
