package org.example.application.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.domain.user.UserFacade;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.example.common.ChatApplicationError.INVALID_USER_HEADER;

@RequiredArgsConstructor
public class UserFilter extends OncePerRequestFilter {
    private final UserFacade userFacade;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            var userId = request.getHeader("userId");
            userFacade.validateUser(Long.valueOf(userId));
        } catch (Exception e){
            throw new ApplicationException(INVALID_USER_HEADER);
        }filterChain.doFilter(request, response);
    }
}
