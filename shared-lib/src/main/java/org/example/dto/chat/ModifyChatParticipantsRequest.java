package org.example.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;

import java.util.Set;

public record ModifyChatParticipantsRequest(
        Set<Long> userIdsToAdd,
        Set<Long> userIdsToDelete
) {
    @AssertTrue
    @JsonIgnore
    public boolean oneSetIsNotEmpty() {
        return !userIdsToAdd.isEmpty() || !userIdsToDelete.isEmpty();
    }
}
