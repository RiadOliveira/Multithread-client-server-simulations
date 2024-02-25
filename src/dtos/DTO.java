package src.dtos;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.UUID;

import src.utils.ConsolePrinter;

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

  public DTO(DTO dto) {
    this.id = dto.getId();
    this.message = dto.getMessage();
    this.sender = dto.getSender();
    this.receiver = dto.getReceiver();
  }

  public String getPrintableString() {
    String printableString = "";
    Field[] fields = DTO.class.getDeclaredFields();

    try {
      for(Field field : fields) {
        Object value = field.get(this).toString();
        printableString += "  " + field.getName() + ": " + value + "\n";
      }

      return printableString;
    } catch (Exception exception) {
      ConsolePrinter.print("Falha ao exibir dados do DTO!");
      return null;
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
