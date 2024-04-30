package com.controller.Services;

import com.controller.Classes.Prefeitura;
import com.controller.Consts.Consts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ClientPrefeituraService {
    @Autowired
    TokenService tokenService;

    public Prefeitura getData(CloseableHttpClient httpClient, ObjectMapper mapper, String id, String token) throws IOException {
        final String BODY = String.format("{\"cpfCNPJ\": \"%s\"}", id);
        ClassicHttpRequest httpPost = ClassicRequestBuilder
                .create("POST")
                .setUri(Consts.PREFEITURA_URL)
                .setHeader("X-Auth-Token", token)
                .setEntity(BODY, ContentType.APPLICATION_JSON)
                .build();

        return httpClient.execute(httpPost, response -> {
            String responseString = EntityUtils.toString(response.getEntity());
            if(response.getCode() == 200 && !responseString.isEmpty()) {
                List<Prefeitura> prefeituraList =
                        Arrays.stream(mapper.readValue(responseString, Prefeitura[].class)).toList();
                if(prefeituraList.isEmpty()) return new Prefeitura();
                return prefeituraList
                                .stream()
                                .filter(pref -> Objects.nonNull(pref.getInscricao()))
                                .findFirst()
                                .orElse(prefeituraList.get(0));
            }
            return new Prefeitura();
        });
    }

    public String getAuthToken(CloseableHttpClient httpClient, ObjectMapper mapper) throws IOException {
        final String TOKEN_NAME = "modulo_prefeitura_auth_token";
        final String LOGIN_TOKEN = tokenService.getToken(TOKEN_NAME).orElseThrow();
        final String BODY = String.format("{\"token\": \"%s\"}", LOGIN_TOKEN);

        ClassicHttpRequest httpPost = ClassicRequestBuilder
                .create("POST")
                .setUri(Consts.X_AUTH_TOKEN_URL)
                .setEntity(BODY, ContentType.APPLICATION_JSON)
                .build();

        return httpClient.execute(httpPost, response ->
                mapper.readTree(response
                                .getEntity()
                                .getContent())
                                .get("token")
                                .asText());
    }
}
