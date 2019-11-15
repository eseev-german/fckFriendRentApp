package fck.friend.rent.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VkOAuth2AccessTokenResponseConverter implements Converter<Map<String, String>, OAuth2AccessTokenResponse> {
    private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = new HashSet<>(Arrays.asList(
            OAuth2ParameterNames.ACCESS_TOKEN,
            OAuth2ParameterNames.TOKEN_TYPE,
            OAuth2ParameterNames.EXPIRES_IN,
            OAuth2ParameterNames.REFRESH_TOKEN,
            OAuth2ParameterNames.SCOPE
    ));

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, String> tokenResponseParameters) {
        return OAuth2AccessTokenResponse.withToken(tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN))
                                        .tokenType(OAuth2AccessToken.TokenType.BEARER)
                                        .expiresIn(getExpiresInParameter(tokenResponseParameters))
                                        .scopes(getScopesParameters(tokenResponseParameters))
                                        .refreshToken(tokenResponseParameters.get(OAuth2ParameterNames.REFRESH_TOKEN))
                                        .additionalParameters(getAdditionalParameters(tokenResponseParameters))
                                        .build();
    }

    private Map<String, Object> getAdditionalParameters(Map<String, String> tokenResponseParameters) {
        return tokenResponseParameters.entrySet()
                                      .stream()
                                      .filter(this::isNotTokenResponseParameter)
                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                              Map.Entry::getValue,
                                              (prev, cur) -> cur,
                                              LinkedHashMap::new));
    }

    private boolean isNotTokenResponseParameter(Map.Entry<String, String> entry) {
        return !TOKEN_RESPONSE_PARAMETER_NAMES.contains(entry.getKey());
    }

    private Set<String> getScopesParameters(Map<String, String> tokenResponseParameters) {
        if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
            String scope = tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
            return new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }
        return Collections.emptySet();
    }

    private long getExpiresInParameter(Map<String, String> tokenResponseParameters) {
        if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
            try {
                return Long.parseLong(tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN));
            } catch (NumberFormatException ignore) {
            }
        }
        return 0;
    }
}