package org.maatram.service;

import org.maatram.exception.ApiException;
import org.maatram.model.User;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final DataStore dataStore;

    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Map<String, Object> login(String email, String password) {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";

        User user = dataStore.users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(cleanEmail) && Boolean.TRUE.equals(u.getIs_active()))
                .findFirst()
                .orElse(null);

        if (user == null || !"admin123".equals(password)) {
            throw new ApiException(401, "Wrong email or password");
        }

        Map<String, Object> userData = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "user_type", user.getUser_type(),
                "name", dataStore.personName(user.getPerson_id())
        );

        return Map.of(
                "token", "bearer-" + UUID.randomUUID(),
                "user", userData
        );
    }
}
