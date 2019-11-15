package fck.friend.rent.config;

import fck.friend.rent.converter.VkOAuth2UserRequestEntityConverter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Configuration
public class VkOAuth2Configuration {
    @Bean
    protected DefaultOAuth2UserService defaultOAuth2UserService() {
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        defaultOAuth2UserService.setRequestEntityConverter(new VkOAuth2UserRequestEntityConverter());
        return defaultOAuth2UserService;
    }

    @Bean
    public RestTemplate restTemplate(OAuth2AuthorizedClientService clientService) {
        return new RestTemplateBuilder()
                .interceptors(createAddAccessTokenToRequest(clientService))
                .build();
    }

    private ClientHttpRequestInterceptor createAddAccessTokenToRequest(OAuth2AuthorizedClientService clientService) {
        return (request, body, execution) -> {
            Authentication authentication = SecurityContextHolder.getContext()
                                                                 .getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken) {
                request = addAccessTokenToRequest(clientService, request, (OAuth2AuthenticationToken) authentication);
            }
            return execution.execute(request, body);
        };
    }

    private HttpRequest addAccessTokenToRequest(OAuth2AuthorizedClientService clientService, HttpRequest request, OAuth2AuthenticationToken authentication) throws IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
        String tokenValue = client.getAccessToken()
                                  .getTokenValue();
        URI uri = UriComponentsBuilder.fromUri(request.getURI())
                                      .queryParam("access_token", tokenValue)
                                      .build()
                                      .toUri();
        return new SimpleClientHttpRequestFactory().createRequest(uri, HttpMethod.GET);
    }
}