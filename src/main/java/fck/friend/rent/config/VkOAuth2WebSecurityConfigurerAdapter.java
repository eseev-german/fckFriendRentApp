package fck.friend.rent.config;

import fck.friend.rent.converter.VkOAuth2AccessTokenRequestEntityConverter;
import fck.friend.rent.converter.VkOAuth2AccessTokenResponseBodyExtractor;
import fck.friend.rent.converter.VkOAuth2AccessTokenResponseConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;

import static org.springframework.security.oauth2.core.web.reactive.function.OAuth2BodyExtractors.oauth2AccessTokenResponse;

@Configuration
@EnableWebFluxSecurity
public class VkOAuth2WebSecurityConfigurerAdapter /*extends WebSecurityConfigurerAdapter*/ {

    private WebClient webClient = WebClient.builder()
                                   .build();
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) throws Exception {
//        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> client = getOAuth2AccessTokenResponseClient();
//        http.authorizeRequests(requests -> requests.anyRequest()
//                                                   .authenticated());
//        http.oauth2Login(configurer -> configurer.redirectionEndpoint()
//                                                 .baseUri("/login")
//                                                 .and()
//                                                 .tokenEndpoint()
//                                                 .accessTokenResponseClient(client));
//        http.oauth2Client()
//            .authorizationCodeGrant(configurer -> configurer.accessTokenResponseClient(client));

            http.authorizeExchange().anyExchange().authenticated();
            http.oauth2Login();

            http.oauth2Client(configurer->configurer.authenticationManager(new OAuth2AuthorizationCodeReactiveAuthenticationManager(vkAccessTokenResponseClient())));
            return http.build();

    }
//
//    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> getOAuth2AccessTokenResponseClient() {
//        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
//        client.setRequestEntityConverter(new VkOAuth2AccessTokenRequestEntityConverter());
//
//        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
//                new OAuth2AccessTokenResponseHttpMessageConverter();
//        tokenResponseHttpMessageConverter.setTokenResponseConverter(new VkOAuth2AccessTokenResponseConverter());
//
//        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
//                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
//        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
//        client.setRestOperations(restTemplate);
//        return client;
//    }

    @Bean
    public ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> vkAccessTokenResponseClient() {
        return authorizationGrantRequest -> Mono.defer(() -> {
            ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
            OAuth2AuthorizationExchange authorizationExchange = authorizationGrantRequest.getAuthorizationExchange();
            String tokenUri = clientRegistration.getProviderDetails().getTokenUri();

            URI uri = UriComponentsBuilder.fromUriString(tokenUri)
                                          .queryParam(OAuth2ParameterNames.CODE, getCode(authorizationExchange))
                                          .queryParam(OAuth2ParameterNames.REDIRECT_URI, getRedirectUri(authorizationExchange))
                                          .queryParam(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId())
                                          .queryParam(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret())
                                          .build()
                                          .toUri();

            return this.webClient.get()
                                 .uri(uri)
                                 .accept(MediaType.APPLICATION_JSON)
                                 .exchange()
                                 .flatMap(response -> response.body(new VkOAuth2AccessTokenResponseBodyExtractor()))
                                 .map(response -> {
                                     if (response.getAccessToken().getScopes().isEmpty()) {
                                         response = OAuth2AccessTokenResponse.withResponse(response)
                                                                             .scopes(authorizationExchange.getAuthorizationRequest().getScopes())
                                                                             .build();
                                     }
                                     return response;
                                 });
        });
    }

    private String getCode(OAuth2AuthorizationExchange authorizationExchange) {
        return authorizationExchange.getAuthorizationResponse().getCode();
    }

    private String getRedirectUri(OAuth2AuthorizationExchange authorizationExchange) {
        return authorizationExchange.getAuthorizationRequest().getRedirectUri();
    }
}