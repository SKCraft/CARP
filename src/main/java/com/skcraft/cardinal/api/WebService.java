/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.cardinal.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebService {
    private static final String USER_AGENT = "SKCraft Cardinal/0.1";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String url;
    private final String apiKey;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Inject
    public WebService(@Named("api.url") String url, @Named("api.key") String apiKey) {
        checkNotNull(url, "url");
        checkNotNull(apiKey, "apiKey");
        this.url = url;
        this.apiKey = apiKey;
    }

    public <T> T get(String path, Class<T> clazz) throws IOException, ServiceError {
        return request("GET", path, clazz, null);
    }

    public <T> T request(String method, String path, Class<T> clazz, Object data) throws IOException, ServiceError {
        Builder builder = new Builder();

        if (method.equals("GET")) {
            builder = builder.get();
        } else {
            RequestBody body = RequestBody.create(JSON, mapper.writeValueAsString(data));
            builder = builder.method(method, body);
        }

        Request request = builder
                .header("User-Agent", USER_AGENT)
                .url(url + path + "?apiKey=" + apiKey)
                .build();

        Response response = client.newCall(request).execute();

        switch (response.code()) {
            case 200:
                try (ResponseBody body = response.body()) {
                    return mapper.readValue(body.string(), clazz);
                }
            case 400:
                try (ResponseBody body = response.body()) {
                    ErrorResponse r = mapper.readValue(body.string(), ErrorResponse.class);
                    throw new ServiceError(r.errors);
                }
            default:
                throw new IOException("Unexpected response code " + response.code() + ": " + response.toString());
        }
    }

}
