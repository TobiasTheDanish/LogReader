package dk.skancode.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ApiResponse {
    private final int responseCode;
    private final String responseMessage;
    private final boolean ok;

    public ApiResponse(int responseCode, String responseMessage, boolean ok) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.ok = ok;
    }
}
