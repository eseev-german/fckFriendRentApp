package fck.friend.rent.integration;

import fck.friend.rent.dto.post.PostBunchDto;
import fck.friend.rent.dto.post.PostDto;
import fck.friend.rent.dto.user.UserDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class PostsProvider {
    private final RestTemplate vkRestTemplate;
    private WebClient webClient;

    private final String postsUrl;

    public PostsProvider(@Qualifier("vkRestTemplate")
                                RestTemplate vkRestTemplate,
                        @Value("${spring.security.oauth2.client.registration.login-client.api.posts}")
                                String postsUrl) {
        this.vkRestTemplate = vkRestTemplate;
        this.postsUrl = postsUrl;
    }

    public PostBunchDto get() {
//webClient.mutate()

        ResponseEntity<PostBunchDto> exchange = vkRestTemplate.getForEntity(postsUrl+"?users=5841316", PostBunchDto.class);

//        WebClient.create().mutate().exchangeFunction()
        return exchange.getBody();
    }
}
