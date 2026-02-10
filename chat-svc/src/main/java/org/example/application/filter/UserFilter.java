package org.example.application.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.example.ApplicationException;
import org.example.ServiceResponse;
import org.example.common.JsonUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.example.common.ChatApplicationError.INVALID_USER_HEADER;
import static org.example.common.Constants.USER_ID_HEADER;

@Component
@RequiredArgsConstructor
public class UserFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/chat-svc/internal")) {
            try {
                String userIdHeader = request.getHeader(USER_ID_HEADER);
                if (StringUtils.isBlank(userIdHeader)) {
                    throw new ApplicationException(INVALID_USER_HEADER);
                }
                Long.parseLong(userIdHeader);
            } catch (Exception ex) {
                writeErrorResponse(response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, Exception ex) throws IOException {
        ServiceResponse serviceResponse;
        switch (ex) {
            case ApplicationException ignored -> {
                response.setStatus(INVALID_USER_HEADER.getStatus().value());
                serviceResponse = new ServiceResponse(INVALID_USER_HEADER.getMessage());
            }
            case NumberFormatException ignored -> {
                response.setStatus(INVALID_USER_HEADER.getStatus().value());
                serviceResponse = new ServiceResponse(INVALID_USER_HEADER.getMessage());
            }
            default -> {
                response.setStatus(500);
                serviceResponse = new ServiceResponse("Unknown server error.");
            }
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = JsonUtils.writeToJson(serviceResponse);
        response.getWriter().write(body);
    }
}
