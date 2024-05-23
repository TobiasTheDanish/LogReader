package dk.skancode.auth;

import java.util.*;

public class TicketManager {
    private final long ticketExpiryTime = 86400000;
    private static TicketManager instance = null;
    private final Map<UUID, Ticket> tickets;

    private TicketManager() {
        tickets = new HashMap<>();
    }

    public static TicketManager getInstance() {
        if (instance == null) {
            instance = new TicketManager();
        }
        return instance;
    }

    public UUID createTicket(String ip) {
        Ticket newTicket = new Ticket(UUID.randomUUID(), ip, System.currentTimeMillis());

        tickets.put(newTicket.id(), newTicket);
        return newTicket.id();
    }

    public Optional<Ticket> getTicket(String uuid) {
        Ticket t = tickets.get(UUID.fromString(uuid));
        if (t == null) { return Optional.empty(); }

        if (t.createdAt() + ticketExpiryTime <  System.currentTimeMillis()) {
            tickets.remove(UUID.fromString(uuid));
            return Optional.empty();
        }

        return Optional.of(t);
    }
}
