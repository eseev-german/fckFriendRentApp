package fck.friend.rent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fck.friend.rent.converter.VkOAuth2UserRequestEntityConverter;
import fck.friend.rent.dto.post.PostBunchDto;
import fck.friend.rent.dto.user.UserDto;
import fck.friend.rent.serializer.PostBunchDtoDeserializer;
import fck.friend.rent.serializer.UserDtoDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

@Configuration
public class VkOAuth2Configuration {

    @Value("${spring.security.oauth2.client.registration.login-client.v}")
    private String apiVersion;
    @Value("${spring.security.oauth2.client.registration.login-client.api.baseurl}")
    private String baseUrl;

    @Bean
    protected DefaultOAuth2UserService defaultOAuth2UserService() {
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        defaultOAuth2UserService.setRequestEntityConverter(new VkOAuth2UserRequestEntityConverter());
        return defaultOAuth2UserService;
    }

    @Bean(name = "vkRestTemplate")
    public RestTemplate vkRestTemplate(OAuth2AuthorizedClientService clientService) {
        return new RestTemplateBuilder()
                .interceptors(addVkParams(clientService))
                .uriTemplateHandler(new DefaultUriBuilderFactory(baseUrl))
                .build();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PostBunchDto.class, new PostBunchDtoDeserializer());
        module.addDeserializer(UserDto.class, new UserDtoDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    private ClientHttpRequestInterceptor addVkParams(OAuth2AuthorizedClientService clientService) {
        return (request, body, execution) -> {
            Authentication authentication = SecurityContextHolder.getContext()
                                                                 .getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken) {
                request = addVkParams(clientService, request, (OAuth2AuthenticationToken) authentication);
            }
            return execution.execute(request, body);
        };
    }

    private HttpRequest addVkParams(OAuth2AuthorizedClientService clientService, HttpRequest request, OAuth2AuthenticationToken authentication) throws IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
        Objects.requireNonNull(client, "Authorized client is null.");
        String tokenValue = client.getAccessToken()
                                  .getTokenValue();
        URI uri = UriComponentsBuilder.fromUri(request.getURI())
                                      .queryParam("access_token", tokenValue)
                                      .queryParam("v", apiVersion)
                                      .build()
                                      .toUri();
        return new SimpleClientHttpRequestFactory().createRequest(uri, HttpMethod.GET);
    }
}