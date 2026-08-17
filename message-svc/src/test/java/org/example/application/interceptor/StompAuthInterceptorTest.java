package org.example.application.interceptor;

import org.example.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.common.MessageApplicationError.INVALID_SUBSCRIPTION_DESTINATION;
import static org.example.common.MessageApplicationError.INVALID_TOKEN;

class StompAuthInterceptorTest {
    private final StompAuthInterceptor interceptor = new StompAuthInterceptor();
    private final MessageChannel channel = (message, timeout) -> true;

    @Test
    void shouldAllowUserMessagesQueueSubscription() {
        Message<?> message = subscribeMessage("123", "/user/queue/messages");

        assertThatCode(() -> interceptor.preSend(message, channel))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowUserErrorsQueueSubscription() {
        Message<?> message = subscribeMessage("123", "/user/queue/errors");

        assertThatCode(() -> interceptor.preSend(message, channel))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowOwnStatusTopicSubscription() {
        Message<?> message = subscribeMessage("123", "/topic/status.123");

        assertThatCode(() -> interceptor.preSend(message, channel))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectOtherUsersStatusTopicSubscription() {
        Message<?> message = subscribeMessage("123", "/topic/status.456");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(ApplicationException.class)
                .extracting(ex -> ((ApplicationException) ex).getApplicationError())
                .isEqualTo(INVALID_SUBSCRIPTION_DESTINATION);
    }

    @Test
    void shouldRejectDirectQueueSubscription() {
        Message<?> message = subscribeMessage("123", "/queue/messages");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(ApplicationException.class)
                .extracting(ex -> ((ApplicationException) ex).getApplicationError())
                .isEqualTo(INVALID_SUBSCRIPTION_DESTINATION);
    }

    @Test
    void shouldRejectSubscriptionWithoutUser() {
        Message<?> message = subscribeMessage(null, "/user/queue/messages");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(ApplicationException.class)
                .extracting(ex -> ((ApplicationException) ex).getApplicationError())
                .isEqualTo(INVALID_TOKEN);
    }

    @Test
    void shouldIgnoreNonSubscribeFrames() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setUser(() -> "123");
        accessor.setDestination("/queue/messages");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatCode(() -> interceptor.preSend(message, channel))
                .doesNotThrowAnyException();
    }

    private static Message<?> subscribeMessage(String userId, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        if (userId != null) {
            accessor.setUser(() -> userId);
        }
        accessor.setDestination(destination);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
