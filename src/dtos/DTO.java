package src.dtos;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.UUID;

import src.utils.ConsoleOperationMessageOverwriter;

public class DTO implements Serializable {
  private UUID id;
  private DTOType type;
  private String message;
  private String sender, receiver = null;

  public DTO(
    DTOType type, String message,
    String sender, String receiver
  ) {
    this.id = UUID.randomUUID();
    this.type = type;
    this.message = message;
    this.sender = sender;
    this.receiver = receiver;
  }

  public void print() {
    ConsoleOperationMessageOverwriter.print("Dados do DTO recebido:");
    printProperties();
  }

  private void printProperties() {
    Field[] fields = DTO.class.getDeclaredFields();

    try {
      for(Field field : fields) {
        Object value = getParsedFieldValue(field.get(this));

        ConsoleOperationMessageOverwriter.print(
          "  " + field.getName() + ": " + value
        );
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private String getParsedFieldValue(Object value) {
    if(value instanceof String) return (String) value;
    if(value instanceof UUID) return value.toString();
    return ((DTOType) value).name();
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
