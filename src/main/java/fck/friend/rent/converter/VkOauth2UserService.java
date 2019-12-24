package fck.friend.rent.converter;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class VkOauth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";
    private static final String MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri";
    private static final String MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE = "missing_user_name_attribute";
    @Value("${spring.security.oauth2.client.registration.vk-login-client.v}")
    private String apiVersion;

    private WebClient webClient = WebClient.create();

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        return Mono.defer(() -> {
            Assert.notNull(userRequest, "userRequest cannot be null");

            String baseUserInfoUri = userRequest.getClientRegistration()
                                            .getProviderDetails()
                                            .getUserInfoEndpoint()
                                            .getUri();
            checkBaseUserInfoUri(baseUserInfoUri);

            String userNameAttributeName = userRequest.getClientRegistration()
                                                      .getProviderDetails()
                                                      .getUserInfoEndpoint()
                                                      .getUserNameAttributeName();

            if (!StringUtils.hasText(userNameAttributeName)) {
                OAuth2Error oauth2Error = new OAuth2Error(
                        MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE,
                        "Missing required \"user name\" attribute name in UserInfoEndpoint for Client Registration: "
                                + userRequest.getClientRegistration()
                                             .getRegistrationId(),
                        null);
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }

            ParameterizedTypeReference<Map<String, Object>> typeReference = new ParameterizedTypeReference<Map<String, Object>>() {
            };

            URI uri = UriComponentsBuilder.fromUriString(baseUserInfoUri)
                                          .queryParam("access_token", userRequest.getAccessToken().getTokenValue())
                                          .queryParam("v", apiVersion)
                                          .build()
                                          .toUri();
            WebClient.RequestHeadersSpec<?> userInfoRequestHeadersSpec = this.webClient.get()
                                                                               .uri(uri)
                                                                               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            Mono<Map<String, Object>> userAttributes = userInfoRequestHeadersSpec
                    .retrieve()
                    .onStatus(s -> s != HttpStatus.OK, response -> parse(response).map(userInfoErrorResponse -> {
                        String description = userInfoErrorResponse.getErrorObject()
                                                                  .getDescription();
                        OAuth2Error oauth2Error = new OAuth2Error(
                                INVALID_USER_INFO_RESPONSE_ERROR_CODE, description,
                                null);
                        throw new OAuth2AuthenticationException(oauth2Error,
                                oauth2Error.toString());
                    }))
                    .bodyToMono(typeReference);

            return userAttributes.map(attrs -> {
                GrantedAuthority authority = new OAuth2UserAuthority(attrs);
                Set<GrantedAuthority> authorities = new HashSet<>();
                authorities.add(authority);
                OAuth2AccessToken token = userRequest.getAccessToken();
                for (String scope : token.getScopes()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }

                return new DefaultOAuth2User(authorities, attrs, userNameAttributeName);
            })
                                 .onErrorMap(e -> e instanceof IOException, t -> new AuthenticationServiceException("Unable to access the userInfoEndpoint " + baseUserInfoUri, t))
                                 .onErrorMap(t -> !(t instanceof AuthenticationServiceException), t -> {
                                     OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, "An error occurred reading the UserInfo Success response: " + t.getMessage(), null);
                                     return new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), t);
                                 });
        });
    }

    private void checkBaseUserInfoUri(String userInfoUri) {
        if (!StringUtils.hasText(
                userInfoUri)) {
            OAuth2Error oauth2Error = new OAuth2Error(
                    MISSING_USER_INFO_URI_ERROR_CODE,
                    "Missing required UserInfo Uri in UserInfoEndpoint for vk Oauth2",
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
    }

    /**
     * Sets the {@link WebClient} used for retrieving the user endpoint
     *
     * @param webClient the client to use
     */
    public void setWebClient(WebClient webClient) {
        Assert.notNull(webClient, "webClient cannot be null");
        this.webClient = webClient;
    }

    private static Mono<UserInfoErrorResponse> parse(ClientResponse httpResponse) {

        String wwwAuth = httpResponse.headers()
                                     .asHttpHeaders()
                                     .getFirst(HttpHeaders.WWW_AUTHENTICATE);

        if (!StringUtils.isEmpty(wwwAuth)) {
            // Bearer token error?
            return Mono.fromCallable(() -> UserInfoErrorResponse.parse(wwwAuth));
        }

        ParameterizedTypeReference<Map<String, String>> typeReference =
                new ParameterizedTypeReference<Map<String, String>>() {
                };
        // Other error?
        return httpResponse
                .bodyToMono(typeReference)
                .map(body -> new UserInfoErrorResponse(ErrorObject.parse(new JSONObject(body))));
    }
}
