package org.example;

import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static java.util.Objects.isNull;

@Component
public class AuthContextInterceptor implements WebGraphQlInterceptor {
    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (isNull(authHeader)) {
            return chain.next(request);
        }
        return chain.next(request)
                .contextWrite(ctx -> ctx.put("authHeader", authHeader));
    }
}