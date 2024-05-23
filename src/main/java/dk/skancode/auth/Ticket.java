package dk.skancode.auth;

import java.util.UUID;

public record Ticket(UUID id, String userIp, long createdAt) {
}
