package fck.friend.rent.integration;

import fck.friend.rent.dto.user.UserDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PostsProvider {
    private final RestTemplate vkRestTemplate;

    private final String postsUrl;

    public PostsProvider(@Qualifier("vkRestTemplate")
                                RestTemplate vkRestTemplate,
                        @Value("${spring.security.oauth2.client.registration.login-client.api.posts}")
                                String postsUrl) {
        this.vkRestTemplate = vkRestTemplate;
        this.postsUrl = postsUrl;
    }

    public UserDto get() {
        ResponseEntity<UserDto> exchange = vkRestTemplate.getForEntity(postsUrl, UserDto.class);
        return exchange.getBody();
    }
}
