package dk.skancode.controller;

import dk.skancode.dto.WebSocketMessage;
import dk.skancode.reader.Log;
import dk.skancode.watcher.FileListener;
import dk.skancode.watcher.FileListenerFactory;
import dk.skancode.watcher.FileWatcher;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;

import java.util.*;
import java.io.IOException;

public class WebSocketController {
    private static final String path = "./testDir";
    private static final FileListener listener = FileListenerFactory.getListener(path);
    private static final FileWatcher watcher = FileWatcher.getInstance();
    private static WebSocketController instance = null;
    private static Thread watcherThread = null;

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
        System.out.println("Connected");
        System.out.println(ctx.host());
        ctx.enableAutomaticPings();
    }

    public void handleClose(WsCloseContext ctx) {
        ctx.disableAutomaticPings();
        System.out.println("Connection closed");
    }

    public void handleMessage(WsMessageContext ctx) {
        WebSocketMessage message = ctx.messageAsClass(WebSocketMessage.class);

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
