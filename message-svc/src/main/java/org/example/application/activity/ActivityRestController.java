package org.example.application.activity;

import lombok.RequiredArgsConstructor;
import org.example.domain.activity.ActiveUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

import static org.example.common.Constants.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
public class ActivityRestController {
    private final ActiveUserService activeUserService;

    @GetMapping("/activity")
    public Map<Long, Boolean> getActivity(@RequestHeader(USER_ID_HEADER) Long userId, @RequestParam Set<Long> userIds) {
        return activeUserService.getActiveUsers(userIds);
    }
}
