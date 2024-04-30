package com.controller.Services;

import com.controller.Classes.FormDocType;
import com.controller.Consts.Consts;
import com.controller.Utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientServiceSintegra {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceSintegra.class);
    public static String getData(String codigo, CloseableHttpClient httpClient, ObjectMapper mapper)  {
        logger.info(String.format("Buscando inscrição estadual no Sintegra: %s\n", codigo));
        try {
            Map<String, String> formParams = new HashMap<>();
            formParams.put("tDoc", codigo);
            formParams.put("rTipoDoc", FormDocType.geDocType(codigo.length()));

            ClassicHttpRequest httpPost = ClassicRequestBuilder
                    .create("POST")
                    .setUri(Consts.SINTEGRA_URL)
                    .setHeader("Host", Consts.SINTEGRA_HOST)
                    .setHeader("Referer", Consts.SINTEGRA_REFERER)
                    .setEntity(getFormParams(formParams))
                    .build();
            return httpClient.execute(httpPost, response -> {
                if(response.getCode() != 200) {
                    return String.format("Error (Sintegra): Wrong response code (%s)", response.getCode());
                }
                String html = EntityUtils.toString(response.getEntity());
                Document document = Jsoup.parse(html);
                Elements inscricoes = document.select("a[onClick^='fSend']");
                if(!inscricoes.isEmpty()) {
                    String inscricao = inscricoes.get(0).text();
                    return getData(inscricao, httpClient, mapper);
                }
                Elements dataContents = document.select("[class$='title']");
                return Utils.sanitize(Utils.getFieldByInnerHtml("Inscrição Estadual", dataContents));
            });
        } catch (IOException e) {
            logger.info(String.format("Erro ao processar empresa (Sintegra)" + codigo + "-->" + e.getMessage()));
            return String.format("Error (Sintegra): %s", e.getMessage());
        }
    }

    private static UrlEncodedFormEntity getFormParams(Map<String, String> params) {
        List<NameValuePair> formParams = new ArrayList<>();
        for(Map.Entry<String, String> entry : params.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
    }
}
