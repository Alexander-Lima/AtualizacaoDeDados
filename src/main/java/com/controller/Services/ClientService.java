package com.controller.Services;

import com.controller.Classes.Client;
import com.controller.Repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public void deleteByCode(String code) {
        clientRepository.deleteByCode(code);
    }

    public Optional<Client> findByCode(String code) {
        return clientRepository.findByCode(code);
    }
}
