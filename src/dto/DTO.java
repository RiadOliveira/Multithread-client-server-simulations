package src.dto;

import java.util.UUID;

public class DTO {
  private UUID id;
  private DTOType type;
  private String message;
  private String sender, receiver;

  public DTO(
    UUID id, DTOType type, String message,
    String sender, String receiver
  ) {
    this.id = id;
    this.type = type;
    this.message = message;
    this.sender = sender;
    this.receiver = receiver;
  }

  public UUID getId() {
    return id;
  }

  public DTOType getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public String getSender() {
    return sender;
  }

  public String getReceiver() {
    return receiver;
  }
}
