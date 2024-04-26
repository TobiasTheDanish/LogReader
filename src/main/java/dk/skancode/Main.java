package dk.skancode;

import dk.skancode.controller.AuthController;
import dk.skancode.controller.WebSocketController;
import dk.skancode.watcher.FileListenerFactory;
import dk.skancode.watcher.FileWatcher;
import io.javalin.Javalin;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var app = Javalin.create();
        app.get("/", (ctx) -> ctx.result("Hello world"));
        app.ws("/ws", (wsConfig -> {
            WebSocketController controller = WebSocketController.getInstance();
            wsConfig.onConnect(controller::handleConnect);
            wsConfig.onMessage(controller::handleMessage);
            wsConfig.onClose(controller::handleClose);
        }));

        AuthController authController = new AuthController();
        app.post("/auth", authController::handleAuthRequest);

        app.start(7070);
//        try {
//            String testPath = args[0];
//            FileWatcher watcher = FileWatcher.getInstance();
//            watcher.addListener(FileListenerFactory.getListener(testPath));
//
//            watcher.register(testPath);
//            Thread t = new Thread(() -> {
//                while (true) {
//                    try {
//                        watcher.watch();
//                    } catch (InterruptedException | IOException e) {
//                        System.err.println("Exception thrown on watcher thread: " + e.getMessage());
//                        break;
//                    }
//                }
//            });
//            t.start();
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
    }
}
