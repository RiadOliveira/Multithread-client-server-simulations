package src.process;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import src.constants.Constants;
import src.dtos.DTO;
import src.error.AppException;
import src.utils.ConsolePrinter;

public abstract class AppThread implements Runnable {
  protected static DTO currentDTOToSend = null;
  protected UUID idOfPreviousDTOToSend = null, idOfPreviousDTOReceived = null;

  protected ObjectInputStream inputStream;
  protected ObjectOutputStream outputStream;

  protected final boolean initializeInputStreamFirstOnRun;
  protected final Socket connectedSocket;

  protected abstract void handleRecognitionCommunication() throws AppException;
  protected abstract boolean canSendUnicastToReceiver(String receiver);
  protected abstract boolean processFinished();
  protected abstract String getProcessName();
  protected abstract String getConnectedProcess();

  public AppThread(
    Socket connectedSocket, boolean initializeInputStreamFirstOnRun
  ) {
    this.connectedSocket = connectedSocket;
    this.initializeInputStreamFirstOnRun = initializeInputStreamFirstOnRun;
  }

  public void run() {
    try {
      if(initializeInputStreamFirstOnRun) {
        inputStream = new ObjectInputStream(connectedSocket.getInputStream());
        outputStream = new ObjectOutputStream(connectedSocket.getOutputStream());
      } else {
        outputStream = new ObjectOutputStream(connectedSocket.getOutputStream());
        inputStream = new ObjectInputStream(connectedSocket.getInputStream());
      }

      synchronized(this) {
        handleRecognitionCommunication();
        ConsolePrinter.updatedPrintingLocks(false);
      };

      MessageReceiverThread messageReceiverThread = MessageReceiverThread.
        GenerateAndStartMessageReceiverThread(getConnectedProcess(), inputStream);

      while(!processFinished()) {
        handleDTOSending();
        handleDTOReceiving(messageReceiverThread.readDTO());
      }

      inputStream.close();
      outputStream.close();
    } catch(Exception exception) {
      ConsolePrinter.print(
        exception instanceof AppException ?
        exception.getMessage() : "Erro interno do processo!"
      );
    }
  }

  protected synchronized void handleDTOSending() {
    if(alreadyUsedOrInvalidDTO(idOfPreviousDTOToSend, currentDTOToSend)) return;

    idOfPreviousDTOToSend = currentDTOToSend.getId();
    String receiver = currentDTOToSend.getReceiver().toUpperCase();
    
    try {
      if(receiver.equals(getProcessName())) {
        throw new AppException("O remetente deve ser diferente do receptor!");
      }

      boolean isBroadcast = receiver.equals(Constants.BROADCAST_RECEIVER);
      boolean canSendUnicast = canSendUnicastToReceiver(receiver);

      if(isBroadcast || canSendUnicast) {
        outputStream.writeObject(currentDTOToSend);
        ConsolePrinter.printDTO(currentDTOToSend, true);
      }
      if(canSendUnicast) AppThread.currentDTOToSend = null;
    } catch (Exception exception) {
      ConsolePrinter.print(
        exception instanceof AppException ? exception.getMessage() :
        "Falha ao enviar a mensagem para " + receiver + "!"
      );
    } finally {
      ConsolePrinter.updatedPrintingLocks(false);
    }
  }

  protected void handleDTOReceiving(DTO receivedDTO) {
    if(alreadyUsedOrInvalidDTO(idOfPreviousDTOReceived, receivedDTO)) return;

    idOfPreviousDTOReceived = receivedDTO.getId();
    ConsolePrinter.printDTO(receivedDTO, false);
    
    boolean isMessageForThisProcess = getProcessName().equals(
      receivedDTO.getReceiver()
    );
    if(!isMessageForThisProcess) handleDTORedirect(receivedDTO);

    ConsolePrinter.updatedPrintingLocks(false);
  }

  private synchronized void handleDTORedirect(DTO receivedDTO) {
    idOfPreviousDTOToSend = receivedDTO.getId();
    currentDTOToSend = receivedDTO;

    ConsolePrinter.updatedPrintingLocks(true);
    ConsolePrinter.print(
      "\nDTO sendo redirecionado para " +
      receivedDTO.getReceiver() + "!"
    );
  }

  private boolean alreadyUsedOrInvalidDTO(UUID idOfPreviousDTO, DTO currentDTO) {
    if(currentDTO == null) return true;
    return idOfPreviousDTO != null &&idOfPreviousDTO.equals(currentDTO.getId());
  }

  public static void setCurrentDTOTOSend(DTO currentDTOTOSend) {
    AppThread.currentDTOToSend = currentDTOTOSend;
  }
}
