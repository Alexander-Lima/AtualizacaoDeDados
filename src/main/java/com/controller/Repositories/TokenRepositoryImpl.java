package com.controller.Repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TokenRepositoryImpl implements TokenRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> getToken(String tokenName) {
        final String SQL = "SELECT token_value FROM tokens WHERE token_name = ?1";
        final Query QUERY =
                entityManager
                        .createNativeQuery(SQL, String.class)
                        .setParameter(1, tokenName);
        List<String> results = QUERY.getResultList();
        if(results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }
}
