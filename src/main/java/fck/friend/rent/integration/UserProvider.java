package fck.friend.rent.integration;

import fck.friend.rent.dto.user.UserDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProvider {
    private final RestTemplate vkRestTemplate;

    private final String friendsUrl;

    public UserProvider(@Qualifier("vkRestTemplate")
                                RestTemplate vkRestTemplate,
                        @Value("${spring.security.oauth2.client.registration.login-client.api.friends}")
                                String friendsUrl) {
        this.vkRestTemplate = vkRestTemplate;
        this.friendsUrl = friendsUrl;
    }

    public UserDto get() {
        ResponseEntity<UserDto> exchange = vkRestTemplate.getForEntity(friendsUrl, UserDto.class);

        return exchange.getBody();
    }
}