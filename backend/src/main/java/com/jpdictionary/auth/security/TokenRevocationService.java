package com.jpdictionary.auth.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || expiresAt == null) {
            return;
        }
        Instant now = Instant.now();
        if (!expiresAt.isAfter(now)) {
            return;
        }
        purgeExpiredEntries(now);
        revokedTokens.put(hashToken(token), expiresAt);
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String tokenHash = hashToken(token);
        Instant expiresAt = revokedTokens.get(tokenHash);
        if (expiresAt == null) {
            return false;
        }
        if (!expiresAt.isAfter(Instant.now())) {
            revokedTokens.remove(tokenHash);
            return false;
        }
        return true;
    }

    private void purgeExpiredEntries(Instant now) {
        if (revokedTokens.size() > 1000) {
            revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}

