package com.controller.Resources;
import com.controller.Classes.Client;
import com.controller.Services.ClientRunnerService;
import com.controller.Services.ClientService;
import com.controller.Services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@RestController
@EnableScheduling
public class ClientResource {
    @Autowired
    private ClientRunnerService clientRunnerService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    TokenService tokenService;

    @Scheduled(cron = "0 0 5 * * *", zone = "America/Sao_Paulo")
    @GetMapping("/update-all-clients")
    public ResponseEntity updateAllClients() {
        try {
            executorService.execute(() -> clientRunnerService.updateAll());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/update-client/{code}")
    public ResponseEntity updateClient(@PathVariable(name = "code") String code) {
        try {
            boolean success = clientRunnerService.updateClient(code);
            if(success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity
                    .status(400)
                    .body("{\"erro\": \"Cliente não está cadastrado.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity getClient(@PathVariable(name = "code") String code) {
        try {
            Optional<Client> clientOptional = clientService.findByCode(code);
            if(clientOptional.isPresent()) {
                return ResponseEntity.ok(clientOptional.get());
            }
            return ResponseEntity
                    .status(400)
                    .body("{\"erro\": \"Cliente não encontrado!\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/{code}")
    public ResponseEntity addClient(@PathVariable(name = "code") String code) {
        try {
            boolean success = clientRunnerService.addClient(code);
            if(success) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity
                        .status(400)
                        .body("{\"erro\": \"Cliente já cadastrado.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity deleteClient(@PathVariable(name = "code") String code) {
        try {
            clientService.deleteByCode(code);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
