package org.example.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class ChatDetail {
    private Long chatId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String imageUrl;

    private Boolean isPrivate;
    private OffsetDateTime lastMessageAt;
    private OffsetDateTime lastReadAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long otherUser;

    public ChatDetail(Long chatId, String name, String imageUrl, Boolean isPrivate, Instant lastMessageAt, Instant lastReadAt, Long otherUser) {
        this.chatId = chatId;
        this.imageUrl = imageUrl;
        this.isPrivate = isPrivate;
        this.lastMessageAt = lastMessageAt.atOffset(ZoneOffset.UTC);
        this.lastReadAt = lastReadAt.atOffset(ZoneOffset.UTC);
        this.name = name;
        this.otherUser = otherUser;
    }

    public ChatDetail() {
    }

    public ChatDetail(
            Long chatId, Boolean isPrivate, Instant lastMessageAt,
            Instant lastReadAt, Long otherUser
    ) {
        this.chatId = chatId;
        this.isPrivate = isPrivate;
        this.lastMessageAt = lastMessageAt.atOffset(ZoneOffset.UTC);
        this.lastReadAt = lastReadAt.atOffset(ZoneOffset.UTC);
        this.otherUser = otherUser;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public OffsetDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(OffsetDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public OffsetDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(OffsetDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(Long otherUser) {
        this.otherUser = otherUser;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ChatDetail that = (ChatDetail) object;
        return Objects.equals(chatId, that.chatId) && Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl) && Objects.equals(isPrivate, that.isPrivate) && Objects.equals(lastMessageAt, that.lastMessageAt) && Objects.equals(lastReadAt, that.lastReadAt) && Objects.equals(otherUser, that.otherUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, name, imageUrl, isPrivate, lastMessageAt, lastReadAt, otherUser);
    }
}
