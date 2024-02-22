package src.process;

import java.io.ObjectInputStream;

import src.dtos.DTO;
import src.utils.ConsoleOperationMessageOverwriter;

public class MessageReceiverThread implements Runnable {
  private final String connectedProcess;
  private final ObjectInputStream inputStream;
  private DTO currentDTORead = null;

  public MessageReceiverThread(
    String connectedProcess, ObjectInputStream inputStream
  ) {
    this.connectedProcess = connectedProcess;
    this.inputStream = inputStream;
  }

  @Override
  public void run() {
    Thread.currentThread().setDaemon(true);

    while(true) {
      try {
        currentDTORead = (DTO) inputStream.readObject();
      } catch (Exception e) {
        ConsoleOperationMessageOverwriter.print(
          "Falha na leitura da mensagem de " +
          connectedProcess + " tentando novamente..."
        );
      }
    }
  }

  public DTO readDTO() {
    return currentDTORead;
  }
}
