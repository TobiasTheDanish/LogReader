package dk.skancode.controller;

import dk.skancode.auth.TicketManager;
import dk.skancode.dto.ApiResponse;
import dk.skancode.dto.AuthApiResponse;
import io.javalin.http.Context;

import java.util.UUID;

public class AuthController {
    private final TicketManager ticketManager = TicketManager.getInstance();

    // TODO: Implement ticket based authentication https://devcenter.heroku.com/articles/websocket-security
    public void handleAuthRequest(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        String password = System.getenv("password");

        String clientPw = ctx.formParam("password");

        if (clientPw == null || !clientPw.equals(password)) {
            ctx.status(401).json(new ApiResponse(401, "Invalid password", false), ApiResponse.class);
            return;
        }

        String userIp = ctx.ip();

        UUID ticketId = ticketManager.createTicket(userIp);

        ctx.status(200).json(new AuthApiResponse(200, "Sign in successful", true, ticketId.toString()), ApiResponse.class);
    }
}
