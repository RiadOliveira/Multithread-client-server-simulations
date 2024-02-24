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
  protected static UUID idOfPreviousDTOReceived = null;
  protected UUID idOfPreviousDTOToSend = null;

  protected ObjectInputStream inputStream;
  protected ObjectOutputStream outputStream;

  protected final boolean isServerThread;
  protected final Socket connectedSocket;

  protected abstract void handleRecognitionCommunication() throws AppException;
  protected abstract boolean canSendUnicastToReceiver(String receiver);
  protected abstract boolean processFinished();
  protected abstract String getProcessName();
  protected abstract String getConnectedProcess();

  public AppThread(Socket connectedSocket, boolean isServerThread) {
    this.connectedSocket = connectedSocket;
    this.isServerThread = isServerThread;
  }

  public void run() {
    try {
      if(isServerThread) {
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

  protected void handleDTOSending() {
    if(alreadyUsedOrInvalidDTO(idOfPreviousDTOToSend, currentDTOToSend)) return;

    idOfPreviousDTOToSend = currentDTOToSend.getId();
    String parsedReceiver = currentDTOToSend.getReceiver().toUpperCase();
    
    try {
      sendCurrentDTO(parsedReceiver);
    } catch (Exception exception) {
      ConsolePrinter.print(
        exception instanceof AppException ? exception.getMessage() :
        "Falha ao enviar a mensagem para " + parsedReceiver + "!"
      );
      ConsolePrinter.updatedPrintingLocks(false);
    }
  }

  private synchronized void sendCurrentDTO(String parsedReceiver) throws Exception {
    if(currentDTOToSend == null) return;
    if(parsedReceiver.equals(getProcessName())) {
      throw new AppException("O remetente deve ser diferente do receptor!");
    }

    boolean isBroadcast = parsedReceiver.equals(Constants.BROADCAST_RECEIVER);
    boolean canSendUnicast = canSendUnicastToReceiver(parsedReceiver);

    if(isBroadcast || canSendUnicast) {
      outputStream.writeObject(currentDTOToSend);
      ConsolePrinter.printDTO(currentDTOToSend, true);
    }
    if(canSendUnicast) AppThread.currentDTOToSend = null;

    ConsolePrinter.updatedPrintingLocks(false);
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
