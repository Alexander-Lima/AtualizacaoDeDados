package com.controller.Services;

import com.controller.Classes.Client;
import com.controller.Classes.Prefeitura;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class ClientRunnerService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ClientPrefeituraService clientPrefeituraService;
    private String authToken = null;
    private static final Logger logger = LoggerFactory.getLogger(ClientRunnerService.class);
    private static final int MAX_RETRY = 3;
    private static int RETRY_TIMES = 0;

    private void update(List<Client> clients) {
        ObjectMapper mapper = new ObjectMapper();
        List<Client> errorList = new ArrayList<>();
        int counter = 1;

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            if(Objects.isNull(authToken)) {
                authToken = clientPrefeituraService.getAuthToken(httpClient, mapper);
            }
            for(Client currentClient : clients) {
                logger.info(String.format("%s de %s", counter, clients.size()));
                counter++;
                String idCode = currentClient.getCodigo().trim();
                Prefeitura prefeitura =
                        clientPrefeituraService.getData(httpClient, mapper, idCode, authToken);
                String regime =
                        ClientServiceSimplesNacional.getOptanteSimplesNacional(httpClient, idCode);
                Client updatedClient = ClientServiceWSPublica.getData(idCode, httpClient, mapper);
                String inscricaoEstadual = ClientServiceSintegra.getData(idCode, httpClient, mapper);
                currentClient.update(updatedClient, prefeitura, inscricaoEstadual, regime);

                if(Objects.nonNull(currentClient.getErrorMessage())) {
                    errorList.add(currentClient);
                    continue;
                }
                entityManager.persist(currentClient);
            }
            if(!errorList.isEmpty() && ++RETRY_TIMES < MAX_RETRY) {
                update(errorList);
            } else {
                errorList.forEach(client -> {
                    client.setErrorMessage(String.format("%s (retry: %s)", client.getErrorMessage(), RETRY_TIMES));
                    entityManager.persist(client);
                });
                RETRY_TIMES = 0;
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            authToken = null;
        }
    }

    public void updateAll() {
//        List<Client> clients =
//                clientService.getAll().stream().filter(c -> c.getIdCliente() == 527)
//                        .sorted(Comparator.comparingInt(Client::getIdCliente)).toList();
        List<Client> clients = clientService.getAll();
        clients.sort(Comparator.comparingInt(Client::getIdCliente));
        update(clients);
    }

    public boolean updateClient(String clientCode) {
        Optional<Client> client = clientService.findByCode(clientCode);
        if(client.isPresent()) {
            update(List.of(client.get()));
            return true;
        }
        return false;
    }

    public boolean addClient(String clientCode) {
        Optional<Client> client = clientService.findByCode(clientCode);
        if(client.isEmpty()) {
            update(List.of(new Client(clientCode)));
            return true;
        }
        return false;
    }
}
