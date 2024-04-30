package com.controller.Classes;

import com.controller.Utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliente_id")
    private int idCliente;
    private String codigo;
    @Column(name = "razao_social")
    private String razaoSocial;
    private String endereco;
    @Column(name = "atividade_principal")
    private String atividadePrincipal;
    @Column(name = "regime_apuracao")
    private String regimeApuracao;
    @Column(name = "situacao")
    private String situacaoCadastral;
    @Column(name = "inscricao_estadual")
    private String inscricaoEstadual;
    @Column(name = "data_source")
    private String source;
    @Column(name = "error_message")
    private String errorMessage = null;
    @Column(name = "last_update")
    private Instant lastUpdate = Instant.now();
    private String ccp;
    @Column(name = "inscricao_municipal")
    private String inscricaoMunicipal;
    @Column(name = "last_update_prefeitura")
    private ZonedDateTime lastUpdatePrefeitura = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"));
    @Column(name = "natureza_juridica")
    private String naturezaJuridica;
    @Column(name = "porte")
    private String porte;
    @Column(name = "uf")
    private String uf;
    @Column(name = "municipio")
    private String municipio;
    @Transient
    private boolean isPopulated = false;

    public Client() {}

    public Client(String code) {
        this.codigo = code;
    }

    public Client(String codigo, String errorMessage, String source) {
        this.codigo = codigo;
        this.errorMessage = errorMessage;
        this.source = source;
    }

    public Client(
                   String codigo,
                   String razaoSocial,
                   String inscricaoEstadual,
                   String endereco,
                   String atividadePrincipal,
                   String situacaoCadastral,
                   String source,
                   String naturezaJuridica,
                   String porte,
                   String uf,
                   String municipio) {
        this.codigo = codigo;
        this.razaoSocial = Objects.nonNull(razaoSocial) ? razaoSocial.toUpperCase() : null;
        this.inscricaoEstadual = Objects.nonNull(inscricaoEstadual) ?
                Utils.sanitize(inscricaoEstadual).toUpperCase() : null;
        this.endereco = Objects.nonNull(endereco) ? endereco.toUpperCase() : null;
        this.atividadePrincipal =
                Objects.nonNull(atividadePrincipal) ? atividadePrincipal.toUpperCase() : null;
        this.situacaoCadastral = situacaoCadastral;
        this.source = source;
        this.naturezaJuridica = naturezaJuridica;
        this.porte = porte;
        this.uf = uf;
        this.municipio = municipio;
        this.isPopulated = true;
    }

    public Client(String codigo, JsonNode json) {
        this(codigo,
            getNonNullNode(json, "nome"),
            null,
            getEnderecoFromNode(json),
            getAtividadePrincipalFromNode(json),
            getNonNullNode(json, "situacao"),
            "WS",
            getNonNullNode(json, "natureza_juridica"),
            getNonNullNode(json, "porte"),
            getNonNullNode(json, "uf"),
            getNonNullNode(json, "municipio"));
    }

    private static String getNonNullNode(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if(Objects.nonNull(fieldNode)) {
            return  fieldNode.asText();
        }
        return null;
    }

    private static String getEnderecoFromNode(JsonNode node) {
        if(Objects.isNull(node.get("logradouro"))) {
            return null;
        }
        String logradouro = node.get("logradouro").asText();
        String numero = node.get("numero").asText();
        String complemento = node.get("complemento").asText();
        String bairro = node.get("bairro").asText();

        return String.format("%s %s, nº %s, %s",
                logradouro, complemento, numero, bairro);
    }

    private static String getAtividadePrincipalFromNode(JsonNode node) {
        JsonNode atividadePrincipal = node.get("atividade_principal");
        if(Objects.nonNull(atividadePrincipal)) {
            return String.format("%s - %s",
                    atividadePrincipal.get(0).get("code").asText(),
                    atividadePrincipal.get(0).get("text").asText());
        }
        return null;
    }

    public void update(Client newClientData, Prefeitura prefeitura,
                       String inscricaoEstadual, String regimeApuracao) {
        setInscricaoEstadual(inscricaoEstadual);
        setRegimeApuracao(regimeApuracao);
        if(Objects.nonNull(newClientData.getRazaoSocial())) {
            setRazaoSocial(newClientData.getRazaoSocial());
            setEndereco(newClientData.getEndereco());
            setAtividadePrincipal(newClientData.getAtividadePrincipal());
            setSource(newClientData.getSource());
            setLastUpdate(newClientData.getLastUpdate());
            setSituacaoCadastral(newClientData.getSituacaoCadastral());
            setUf(newClientData.getUf());
            setMunicipio(newClientData.getMunicipio());
            setPorte(newClientData.getPorte());
            setNaturezaJuridica(newClientData.getNaturezaJuridica());
            setErrorMessage(newClientData.getErrorMessage());
            setLastUpdatePrefeitura(null);
        }
        if(regimeApuracao.contains("Error")) {
            setRegimeApuracao(null);
            setErrorMessage(regimeApuracao);
        }
        if(Objects.nonNull(prefeitura)) {
            setCcp(prefeitura.getCcp());
            setInscricaoMunicipal(prefeitura.getInscricao());
            setLastUpdatePrefeitura(prefeitura.getLastUpdate());
        }
    }

    public void setEndereco(String endereco) {
        if(Objects.nonNull(endereco)) {
            this.endereco = endereco.toUpperCase().trim();
            return;
        }
        this.endereco = null;
    }
}
