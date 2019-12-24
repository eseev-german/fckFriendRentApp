package fck.friend.rent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fck.friend.rent.converter.TestFilter;
import fck.friend.rent.dto.post.PostBunchDto;
import fck.friend.rent.dto.user.UserDto;
import fck.friend.rent.serializer.PostBunchDtoDeserializer;
import fck.friend.rent.serializer.UserDtoDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableWebFlux
public class VkOAuth2Configuration {

    @Value("${spring.security.oauth2.client.registration.vk-login-client.v}")
    private String apiVersion;
    @Value("${spring.security.oauth2.client.registration.vk-login-client.api.baseurl}")
    private String baseUrl;

    //    @Bean
//    protected DefaultOAuth2UserService defaultOAuth2UserService() {
//        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
//        defaultOAuth2UserService.setRequestEntityConverter(new VkOAuth2UserRequestEntityConverter());
//        return defaultOAuth2UserService;
//    }
    @Bean(name = "vkWebClient")
    public WebClient vkWebClient(ReactiveClientRegistrationRepository clientRegistrations,
                                 ServerOAuth2AuthorizedClientRepository authorizedClients) {
        ExchangeStrategies strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer ->{
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper(), MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper(), MediaType.APPLICATION_JSON));

                })
                .build();

        TestFilter oauth =
                new TestFilter(clientRegistrations, authorizedClients);


        return WebClient.builder()
                        .baseUrl(baseUrl)
                        .exchangeStrategies(strategies)
                        .build();
    }

/*    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }*/
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PostBunchDto.class, new PostBunchDtoDeserializer());
        module.addDeserializer(UserDto.class, new UserDtoDeserializer());
        mapper.registerModule(module);
        return mapper;
    }
/*
    private Map<String, String> getVkUrlParams() {

        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            return getVkUrlParams();
        }
        return Collections.emptyMap();
    }

    private HttpRequest addVkParams(OAuth2AuthorizedClientService clientService, HttpRequest request, OAuth2AuthenticationToken authentication) throws IOException {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
        Objects.requireNonNull(client, "Authorized client is null.");
        String tokenValue = client.getAccessToken()
                                  .getTokenValue();
        Map<String, String> vkParams = new HashMap<>();
        vkParams.put("access_token", tokenValue);
        vkParams.put("v", apiVersion);
        return vkParams;
    }*/
}