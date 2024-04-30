package com.controller.Classes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Prefeitura {
    private String ccp;
    private String inscricao;
    private ZonedDateTime lastUpdate = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"));
}
