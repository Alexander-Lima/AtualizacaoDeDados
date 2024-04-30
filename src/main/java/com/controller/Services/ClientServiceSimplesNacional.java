package com.controller.Services;
import com.controller.Consts.Consts;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

public class  ClientServiceSimplesNacional {
    private static int retry = 0;
    private static final int MAXIMUM_RETRY = 3;
    public static String getOptanteSimplesNacional(CloseableHttpClient httpClient, String idCode) {
        try {
            final int MINIMUM_CNPJ_LENGTH = 14;
            if(idCode.length() < MINIMUM_CNPJ_LENGTH) {
                return "NORMAL";
            }
            ClassicHttpRequest httpGet =
                    ClassicRequestBuilder
                            .create("GET")
                            .setUri(Consts.SIMPLES_NACIONAL_SERVICE_URL + idCode)
                            .build();
            return httpClient.execute(httpGet, response -> {
                int code = response.getCode();
                final String responseBody = EntityUtils.toString(response.getEntity());
                if(code == 400) {
                    if(retry <= MAXIMUM_RETRY) {
                        retry++;
                        return getOptanteSimplesNacional(httpClient, idCode);
                    }
                    return "Error (Simples): " + responseBody;
                }
                if(responseBody.contains("isOptante")) {
                    return responseBody.contains("true") ? "SIMPLES NACIONAL" : "NORMAL";
                }
                return "Error (Simples): isOptante not found";
            });
        } catch (Exception e) {
            return String.format("Error (Simples): %s", e.getMessage());
        }
    }
}
