package fck.friend.rent.converter;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class VkOAuth2AccessTokenResponseBodyExtractor
		implements BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> {

	private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

	public VkOAuth2AccessTokenResponseBodyExtractor() {
	}

	@Override
	public Mono<OAuth2AccessTokenResponse> extract(ReactiveHttpInputMessage inputMessage,
												   Context context) {
		ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {
		};
		BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = BodyExtractors.toMono(type);
		return delegate.extract(inputMessage, context)
					   .map(VkOAuth2AccessTokenResponseBodyExtractor::parse)
					   .flatMap(VkOAuth2AccessTokenResponseBodyExtractor::oauth2AccessTokenResponse)
					   .map(VkOAuth2AccessTokenResponseBodyExtractor::oauth2AccessTokenResponse);
	}

	private static TokenResponse parse(Map<String, Object> json) {
		try {
			json.put("token_type", "Bearer");
			return TokenResponse.parse(new JSONObject(json));
		} catch (ParseException e) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
					"An error occurred parsing the Access Token response: " + e.getMessage(), null);
			throw new OAuth2AuthorizationException(oauth2Error, e);
		}
	}

	private static Mono<AccessTokenResponse> oauth2AccessTokenResponse(TokenResponse tokenResponse) {
		if (tokenResponse.indicatesSuccess()) {
			return Mono.just(tokenResponse)
					   .cast(AccessTokenResponse.class);
		}
		TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) tokenResponse;
		ErrorObject errorObject = tokenErrorResponse.getErrorObject();
		OAuth2Error oauth2Error;
		if (errorObject == null) {
			oauth2Error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR);
		} else {
			oauth2Error = new OAuth2Error(
					errorObject.getCode() != null ? errorObject.getCode() : OAuth2ErrorCodes.SERVER_ERROR,
					errorObject.getDescription(),
					errorObject.getURI() != null ? errorObject.getURI()
															  .toString() : null);
		}
		return Mono.error(new OAuth2AuthorizationException(oauth2Error));
	}

	private static OAuth2AccessTokenResponse oauth2AccessTokenResponse(AccessTokenResponse accessTokenResponse) {
		AccessToken accessToken = accessTokenResponse.getTokens()
													 .getAccessToken();
		OAuth2AccessToken.TokenType accessTokenType = null;
		if (OAuth2AccessToken.TokenType.BEARER.getValue()
											  .equalsIgnoreCase(accessToken.getType()
																		   .getValue())) {
			accessTokenType = OAuth2AccessToken.TokenType.BEARER;
		}
		long expiresIn = accessToken.getLifetime();

		Set<String> scopes = accessToken.getScope() == null ?
				Collections.emptySet() : new LinkedHashSet<>(accessToken.getScope()
																		.toStringList());

		String refreshToken = null;
		if (accessTokenResponse.getTokens()
							   .getRefreshToken() != null) {
			refreshToken = accessTokenResponse.getTokens()
											  .getRefreshToken()
											  .getValue();
		}

		Map<String, Object> additionalParameters = new LinkedHashMap<>(accessTokenResponse.getCustomParameters());

		return OAuth2AccessTokenResponse.withToken(accessToken.getValue())
										.tokenType(accessTokenType)
										.expiresIn(expiresIn)
										.scopes(scopes)
										.refreshToken(refreshToken)
										.additionalParameters(additionalParameters)
										.build();
	}
}