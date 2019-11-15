package fck.friend.rent.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

public class VkOAuth2AccessTokenRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {

    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request) {
        ClientRegistration clientRegistration = request.getClientRegistration();
        OAuth2AuthorizationExchange authorizationExchange = request.getAuthorizationExchange();

        URI uri = UriComponentsBuilder.fromUriString(getTokenUri(clientRegistration))
                                      .queryParam(OAuth2ParameterNames.CODE, getCode(authorizationExchange))
                                      .queryParam(OAuth2ParameterNames.REDIRECT_URI, getRedirectUri(authorizationExchange))
                                      .queryParam(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId())
                                      .queryParam(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret())
                                      .build()
                                      .toUri();

        return new RequestEntity<>(getDefaultHttpHeaders(), HttpMethod.GET, uri);
    }

    private String getRedirectUri(OAuth2AuthorizationExchange authorizationExchange) {
        return authorizationExchange.getAuthorizationRequest()
                                    .getRedirectUri();
    }

    private String getCode(OAuth2AuthorizationExchange authorizationExchange) {
        return authorizationExchange.getAuthorizationResponse()
                                    .getCode();
    }

    private String getTokenUri(ClientRegistration clientRegistration) {
        return clientRegistration.getProviderDetails()
                                 .getTokenUri();
    }

    private HttpHeaders getDefaultHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}