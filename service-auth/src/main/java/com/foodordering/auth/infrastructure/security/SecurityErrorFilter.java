package com.foodordering.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.auth.infrastructure.exception.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Filter để bắt các lỗi xảy ra trong chuỗi filter của Spring Security
 * (Lỗi ở đây không được bắt bởi @RestControllerAdvice)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityErrorFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("[SECURITY-FILTER-ERROR] Lỗi trong chuỗi Security Filter: ", ex);
            
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi bảo mật/hệ thống: " + ex.getMessage(),
                    LocalDateTime.now()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
