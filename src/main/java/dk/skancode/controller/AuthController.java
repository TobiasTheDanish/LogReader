package dk.skancode.controller;

import io.javalin.http.Context;

public class AuthController {

    // TODO: Implement ticket based authentication https://devcenter.heroku.com/articles/websocket-security
    public void handleAuthRequest(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        String password = System.getenv("password");

        String clientPw = ctx.formParam("password");

        if (clientPw == null || !clientPw.equals(password)) {
            ctx.status(401).result("Error");
            return;
        }

        ctx.result("Authenticated");
    }
}
