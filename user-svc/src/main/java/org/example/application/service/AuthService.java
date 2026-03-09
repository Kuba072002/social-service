package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.ApplicationException;
import org.example.application.dto.SignInResponse;
import org.example.domain.entity.RefreshToken;
import org.example.domain.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.application.service.TokenService.ACCESS_TYPE;
import static org.example.application.service.TokenService.REFRESH_TYPE;
import static org.example.comon.UserApplicationError.INVALID_AUTH_DATA;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public SignInResponse authUser(String email, String password) {
        var user = userService.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new ApplicationException(INVALID_AUTH_DATA));

        var accessToken = tokenService.generate(user.getId(), user.getUserName(), ACCESS_TYPE);
        var refreshToken = tokenService.generate(user.getId(), user.getUserName(), REFRESH_TYPE);

        var expiredAt = tokenService.validateAndGetClaims(refreshToken, REFRESH_TYPE).getExpiration().getTime();
        userService.save(new RefreshToken(user.getId(), tokenService.hash(refreshToken), expiredAt));
        return new SignInResponse(userMapper.toUserDTO(user), accessToken, refreshToken);
    }

    @Transactional
    public SignInResponse refreshToken(String refreshToken) {
        var userIdStr = tokenService.validateAndGetClaims(refreshToken, REFRESH_TYPE).getSubject();
        var userId = getUserId(userIdStr);

        var user = userService.findById(userId)
                .orElseThrow(() -> new ApplicationException(INVALID_AUTH_DATA));
        var hashToken = tokenService.hash(refreshToken);
        var storedToken = userService.findStoredRefreshToken(userId, hashToken)
                .orElseThrow(() -> new ApplicationException(INVALID_AUTH_DATA));

        var newAccessToken = tokenService.generate(user.getId(), user.getUserName(), ACCESS_TYPE);
        var newRefreshToken = tokenService.generate(user.getId(), user.getUserName(), REFRESH_TYPE);

        storedToken.setHashToken(tokenService.hash(newRefreshToken));
        storedToken.setExpiredAt(tokenService.validateAndGetClaims(newRefreshToken, REFRESH_TYPE).getExpiration().getTime());
        userService.save(storedToken);

        return new SignInResponse(userMapper.toUserDTO(user), newAccessToken, newRefreshToken);
    }

    private static long getUserId(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new ApplicationException(INVALID_AUTH_DATA);
        }
    }
}
