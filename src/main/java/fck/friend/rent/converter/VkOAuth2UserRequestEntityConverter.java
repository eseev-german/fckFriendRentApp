package fck.friend.rent.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

public class VkOAuth2UserRequestEntityConverter implements Converter<OAuth2UserRequest, RequestEntity<?>> {

    /**
     * Returns the {@link RequestEntity} used for the UserInfo Request.
     *
     * @param userRequest the user request
     * @return the {@link RequestEntity} used for the UserInfo Request
     */
    @Override
    public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
        String uriString = userRequest.getClientRegistration()
                                      .getProviderDetails()
                                      .getUserInfoEndpoint()
                                      .getUri();
        String accessToken = userRequest.getAccessToken()
                                        .getTokenValue();
        URI uri = UriComponentsBuilder.fromUriString(uriString)
                                      .queryParam("access_token", accessToken)
                                      .queryParam("v", "5.103")
                                      .build()
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return new RequestEntity<>(headers, HttpMethod.GET, uri);
    }
}