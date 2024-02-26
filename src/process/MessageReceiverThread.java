package src.process;

import java.io.ObjectInputStream;

import src.dtos.DTO;
import src.error.AppException;
import src.utils.ConsolePrinter;

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

  public static MessageReceiverThread GenerateAndStartMessageReceiverThread(
    String connectedProcess, ObjectInputStream inputStream
  ) {
    MessageReceiverThread messageReceiverThread = new MessageReceiverThread(
      connectedProcess, inputStream
    );

    Thread receiverThread = new Thread(messageReceiverThread);
    receiverThread.setDaemon(true);
    receiverThread.start();

    return messageReceiverThread;
  }

  @Override
  public void run() {
    String disconnectMessage = "Processo desconectou-se de " + 
      connectedProcess + "!";

    try {
      while(inputStream != null) tryToReadInputStream();
      ConsolePrinter.printReinsertingOperationMessage(disconnectMessage);
    } catch (Exception exception) {
      String errorMessage = exception instanceof AppException ?
        exception.getMessage() : "Erro interno do processo!";

      ConsolePrinter.printReinsertingOperationMessage(
        errorMessage + "\n" + disconnectMessage
      );
    }
  }

  private void tryToReadInputStream() throws AppException {
    try {
      currentDTORead = (DTO) inputStream.readObject();
    } catch (Exception e) {
      throw new AppException(
        "Falha na leitura da mensagem de " +
        connectedProcess + "!"
      );
    }
  }

  public synchronized DTO readDTO() {
    if(currentDTORead == null) return null;

    DTO dtoRead = new DTO(currentDTORead);
    currentDTORead = null;

    return dtoRead;
  }
}
