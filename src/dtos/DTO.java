package src.dtos;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.UUID;

import src.utils.ConsoleOperationMessageOverwriter;

public class DTO implements Serializable {
  private UUID id;
  private String message;
  private String sender, receiver;

  public DTO(
    String message, String sender, String receiver
  ) {
    this.id = UUID.randomUUID();
    this.message = message;
    this.sender = sender;
    this.receiver = receiver;
  }

  public void print() {
    System.out.println("Dados do DTO recebido:");
    printProperties();
    ConsoleOperationMessageOverwriter.print("");
  }

  private void printProperties() {
    Field[] fields = DTO.class.getDeclaredFields();

    try {
      for(Field field : fields) {
        Object value = field.get(this).toString();
        System.out.println("  " + field.getName() + ": " + value);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public UUID getId() {
    return id;
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
