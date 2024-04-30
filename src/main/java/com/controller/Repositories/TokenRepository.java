package com.controller.Repositories;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository {
    Optional<String> getToken(String tokenName);
}
