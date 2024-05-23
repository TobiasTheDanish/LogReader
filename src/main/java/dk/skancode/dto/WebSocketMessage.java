package dk.skancode.dto;

public record WebSocketMessage(String command, String ticket, String filePath) {
}
