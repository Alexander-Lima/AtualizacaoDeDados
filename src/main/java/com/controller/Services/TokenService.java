package com.controller.Services;
import com.controller.Repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TokenService {
    @Autowired
    TokenRepository tokenRepository;

    public Optional<String> getToken(String tokenName) {
        return tokenRepository.getToken(tokenName);
    }
}
