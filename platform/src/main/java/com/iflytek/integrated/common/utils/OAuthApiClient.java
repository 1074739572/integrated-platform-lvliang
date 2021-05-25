package com.iflytek.integrated.common.utils;

import java.util.List;
import java.util.Map;

import org.apache.nifi.api.toolkit.ApiClient;
import org.apache.nifi.api.toolkit.Pair;
import org.apache.nifi.api.toolkit.auth.OAuth;

public class OAuthApiClient extends ApiClient {

    private OAuth auth = new OAuth();

    @Override
    public void setAccessToken(String accessToken) {
        auth.setAccessToken(accessToken);
    }

    public void updateParamsForAuth(String[] authNames, List<Pair> queryParams, Map<String, String> headerParams) {
        auth.applyToParams(queryParams, headerParams);
    }
}
