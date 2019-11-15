package fck.friend.rent.config;

import fck.friend.rent.converter.VkOAuth2AccessTokenRequestEntityConverter;
import fck.friend.rent.converter.VkOAuth2AccessTokenResponseConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
public class VkOAuth2WebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> client = getOAuth2AccessTokenResponseClient();

        http.authorizeRequests(requests -> requests.anyRequest()
                                                   .authenticated());
        http.oauth2Login(configurer -> configurer.redirectionEndpoint()
                                                 .baseUri("/login")
                                                 .and()
                                                 .tokenEndpoint()
                                                 .accessTokenResponseClient(client));
        http.oauth2Client()
            .authorizationCodeGrant(configurer -> configurer.accessTokenResponseClient(client));
    }

    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> getOAuth2AccessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        client.setRequestEntityConverter(new VkOAuth2AccessTokenRequestEntityConverter());

        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setTokenResponseConverter(new VkOAuth2AccessTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        client.setRestOperations(restTemplate);
        return client;
    }
}