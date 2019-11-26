package fck.friend.rent.integration;

import fck.friend.rent.dto.UserDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProvider {
    private final RestTemplate vkRestTemplate;

    private final String followersUrl;

    public UserProvider(@Qualifier("vkRestTemplate")
                                RestTemplate vkRestTemplate,
                        @Value("${spring.security.oauth2.client.registration.login-client.api.followers}")
                                String followersUrl) {
        this.vkRestTemplate = vkRestTemplate;
        this.followersUrl = followersUrl;
    }

    public UserDTO get() {
        ResponseEntity<UserDTO> exchange = vkRestTemplate.getForEntity(followersUrl, UserDTO.class);
        return exchange.getBody();
    }
}