package com.controller.Services;

import com.controller.Classes.Client;
import com.controller.Consts.Consts;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Objects;


@Service
public class ClientServiceWSPublica {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceWSPublica.class);

    public static Client getData(String codigo, CloseableHttpClient httpClient, ObjectMapper mapper) {
        logger.info(String.format("Buscando dados da empresa na WS: %s\n", codigo));
        try {
            ClassicHttpRequest httpPost =
                    ClassicRequestBuilder
                            .create("GET")
                            .setUri(Consts.WS_URL + codigo)
                            .build();
            return httpClient.execute(httpPost, response -> {
                int responseCode = response.getCode();
                String html = EntityUtils.toString(response.getEntity());
                if(responseCode == 404) {
                    JsonNode cacheError = mapper.readTree(html).get("message");
                    if(Objects.nonNull(cacheError) && cacheError.asText().contains("not in cache")) {
                        return new Client();
                    }
                }
                if(responseCode == 429) {
                    logger.info("Too many requests...");
                    runCooldown();
                    return getData(codigo, httpClient, mapper);
                }
                JsonNode jsonNode = mapper.readTree(html);
                if(Objects.isNull(jsonNode)) {
                    return new Client();
                }
                return new Client(codigo, jsonNode);
            });
        } catch (IOException e) {
            logger.info(String.format("Erro ao processar empresa (WS) " + codigo + "-->" + e.getMessage()));
            runCooldown();
            return getData(codigo, httpClient, mapper);
        }
    }

    private static void runCooldown() {
        logger.info("Running cooldown for 20 seconds...\n");
        final long WAIT_TIME_MILISECONDS = 20000L;
        try {
            Thread.sleep(WAIT_TIME_MILISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
