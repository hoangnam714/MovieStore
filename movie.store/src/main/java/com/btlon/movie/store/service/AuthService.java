package com.btlon.movie.store.service;

import com.btlon.movie.store.entity.AuthToken;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.repository.AuthTokenRepository;
import com.btlon.movie.store.security.Tokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Issues and verifies opaque bearer tokens; the raw token is only ever returned
 * to the client.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {
    private final AuthTokenRepository tokens;
    private final long tokenHours;

    public AuthService(AuthTokenRepository tokens, @Value("${moviestore.auth.token-hours:12}") long tokenHours) {
        this.tokens = tokens;
        this.tokenHours = tokenHours;
    }

    @Transactional
    public String issue(User user) {
        LocalDateTime now = LocalDateTime.now();
        tokens.deleteExpired(now);
        String token = Tokens.newToken();
        tokens.save(new AuthToken(Tokens.sha256(token), user, now.plusHours(tokenHours)));
        return token;
    }

    public Optional<User> authenticate(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return tokens.findByTokenHash(Tokens.sha256(token))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(AuthToken::getUser);
    }

    @Transactional
    public void revoke(String token) {
        if (token != null && !token.isBlank()) tokens.deleteByTokenHash(Tokens.sha256(token));
    }

    /**
     * Logs the user out everywhere except the session making the request (null
     * keeps none).
     */
    @Transactional
    public void revokeOtherSessions(Long userId, String keepToken) {
        if (keepToken == null) tokens.deleteByUserId(userId);
        else
            tokens.deleteByUserIdAndTokenHashNot(userId, Tokens.sha256(keepToken));
    }
}
