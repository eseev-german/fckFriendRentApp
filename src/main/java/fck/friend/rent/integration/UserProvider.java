package fck.friend.rent.integration;

import fck.friend.rent.dto.user.UserDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserProvider {
    private final WebClient vkWebClient;

    private final String friendsUrl;

    public UserProvider(@Qualifier("vkWebClient")
                                WebClient vkWebClient,
                        @Value("${spring.security.oauth2.client.registration.vk-login-client.api.friends}")
                                String friendsUrl) {
        this.vkWebClient = vkWebClient;
        this.friendsUrl = friendsUrl;
    }

    public Mono<UserDto> get() {
        return vkWebClient.get()
                          .uri(friendsUrl)
                          .retrieve()
                          .bodyToMono(UserDto.class);
    }
}