package dk.skancode.dto;

import lombok.Getter;

@Getter
public class AuthApiResponse extends ApiResponse {
    private final String ticket;
    public AuthApiResponse(int responseCode, String responseMessage, boolean ok, String ticket) {
        super(responseCode, responseMessage, ok);
        this.ticket = ticket;
    }
}
