package dk.skancode.controller;

import dk.skancode.auth.Ticket;
import dk.skancode.auth.TicketManager;
import dk.skancode.dto.WSErrorMessage;
import dk.skancode.dto.WebSocketMessage;
import dk.skancode.reader.Log;
import dk.skancode.watcher.FileListener;
import dk.skancode.watcher.FileListenerFactory;
import dk.skancode.watcher.FileWatcher;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;

import java.net.SocketAddress;
import java.util.*;
import java.io.IOException;

public class WebSocketController {
    private static final String path = "./testDir";
    private static final FileListener listener = FileListenerFactory.getListener(path);
    private static final FileWatcher watcher = FileWatcher.getInstance();
    private static WebSocketController instance = null;
    private static Thread watcherThread = null;
    private final TicketManager ticketManager = TicketManager.getInstance();

    private WebSocketController() {
        try {
            watcher.addListener(listener);
            watcher.register(path);
            watcherThread = new Thread(() -> {
                while (true) {
                    try {
                        watcher.watch();
                    } catch (InterruptedException | IOException e) {
                        System.err.println("FileWatcher has stopped due to exception. Error message: " + e.getMessage());
                        break;
                    }
                }
            });
            watcherThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static WebSocketController getInstance() {
        if (instance == null) {
            instance = new WebSocketController();
        }

        return instance;
    }

    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    public void handleClose(WsCloseContext ctx) {
        ctx.disableAutomaticPings();
    }

    public void handleMessage(WsMessageContext ctx) {
        WebSocketMessage message = ctx.messageAsClass(WebSocketMessage.class);

        if (message == null || message.ticket() == null) {
            ctx.sendAsClass(new WSErrorMessage("Malformed message"), WSErrorMessage.class);
            return;
        }

        Optional<Ticket> ticketOpt = ticketManager.getTicket(message.ticket());
        if (ticketOpt.isEmpty()) {
            System.out.println("Ticket not found");
            ctx.sendAsClass(new WSErrorMessage("You are not authenticated"), WSErrorMessage.class);
            return;
        }

        Ticket ticket = ticketOpt.get();
        String ip = ctx.session.getRemoteAddress().toString();
        ip = "[" + ip.split("\\[")[1].split("]")[0] + "]";

        if (!ticket.userIp().equals(ip)) {
            ctx.sendAsClass(new WSErrorMessage("You are not authenticated"), WSErrorMessage.class);
            return;
        }

        if (message.command().equalsIgnoreCase("fullPing")) {
            var logMap = listener.getLogMap();
            ctx.sendAsClass(logMap, logMap.getClass());
        } else if (message.command().equalsIgnoreCase("specificPing")) {
            Map.Entry<String, List<Log>> entry = new AbstractMap.SimpleEntry<>(message.filePath(), listener.getLogsForFile(message.filePath()));
            ctx.sendAsClass(entry, Map.Entry.class);
        }
    }

    public void shutDown() {
        watcherThread.interrupt();
    }
}
