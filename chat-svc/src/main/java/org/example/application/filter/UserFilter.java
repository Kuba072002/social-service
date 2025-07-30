package org.example.application.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.domain.user.UserFacade;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserFilter extends OncePerRequestFilter {
    private final UserFacade userFacade;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER_ID_HEADER = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        if (request.getRequestURI().startsWith("/chats")) {
//            try {
//                String userIdHeader = request.getHeader(USER_ID_HEADER);
//                if (StringUtils.isBlank(userIdHeader)) {
//                    throw new ApplicationException(INVALID_USER_HEADER);
//                }
//                long userId = Long.parseLong(userIdHeader);
//                userFacade.validateUser(userId);
//            } catch (Exception ex) {
//                writeErrorResponse(response, ex);
//                return;
//            }
//        }
        filterChain.doFilter(request, response);
    }

//    private void writeErrorResponse(HttpServletResponse response, Exception ex) throws IOException {
//        ServiceResponse serviceResponse;
//        switch (ex) {
//            case ApplicationException e -> {
//                response.setStatus(INVALID_USER_HEADER.getStatus().value());
//                serviceResponse = new ServiceResponse(INVALID_USER_HEADER.getMessage());
//            }
//            case NumberFormatException e -> {
//                response.setStatus(INVALID_USER_HEADER.getStatus().value());
//                serviceResponse = new ServiceResponse(INVALID_USER_HEADER.getMessage());
//            }
//            case HttpClientErrorException cEx -> {
//                response.setStatus(cEx.getStatusCode().value());
//                serviceResponse = cEx.getResponseBodyAs(ServiceResponse.class);
//            }
//            case HttpServerErrorException sEx -> {
//                response.setStatus(sEx.getStatusCode().value());
//                serviceResponse = new ServiceResponse("Unknown server error.");
//            }
//            default -> {
//                response.setStatus(500);
//                serviceResponse = new ServiceResponse("Unknown server error.");
//            }
//        }
//        response.setContentType("application/json");
//        String body = objectMapper.writeValueAsString(serviceResponse);
//        response.getWriter().write(body);
//    }
}
