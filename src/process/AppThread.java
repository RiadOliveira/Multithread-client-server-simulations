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
  protected static UUID idOfPreviousDTOToSend = null, idOfPreviousDTOReceived = null;

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

      synchronized(AppThread.class) {
        handleRecognitionCommunication();
        ConsolePrinter.updatedPrintingLocks(-1);
      }

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
      synchronized(AppThread.class) {sendCurrentDTO(parsedReceiver);}
    } catch (Exception exception) {
      ConsolePrinter.print(
        exception instanceof AppException ? exception.getMessage() :
        "Falha ao enviar a mensagem para " + parsedReceiver + "!"
      );
      ConsolePrinter.updatedPrintingLocks(-1);
    }
  }

  private void sendCurrentDTO(String parsedReceiver) throws Exception {
    if(currentDTOToSend == null) return;
    if(parsedReceiver.equals(getProcessName())) {
      throw new AppException("O receptor deve ser outro processo!");
    }
    
    boolean isBroadcast = parsedReceiver.equals(Constants.BROADCAST_RECEIVER);
    boolean canSendUnicast = canSendUnicastToReceiver(parsedReceiver);
    if(isBroadcast || canSendUnicast) {
      outputStream.writeObject(currentDTOToSend);
      ConsolePrinter.printDTO(currentDTOToSend, getConnectedProcess(), true);
      currentDTOToSend = null;
    }

    ConsolePrinter.updatedPrintingLocks(-1);
  }
  
  protected void handleDTOReceiving(DTO receivedDTO) {
    boolean dtoAlreadyReceived = alreadyUsedOrInvalidDTO(idOfPreviousDTOReceived, receivedDTO);
    boolean dtoAlreadyRedirected = alreadyUsedOrInvalidDTO(idOfPreviousDTOToSend, receivedDTO);
    if(dtoAlreadyReceived || dtoAlreadyRedirected) return;

    idOfPreviousDTOReceived = receivedDTO.getId();
    ConsolePrinter.updatedPrintingLocks(1);
    ConsolePrinter.printDTO(receivedDTO, getConnectedProcess(), false);
    
    boolean isMessageForThisProcess = getProcessName().equals(
      receivedDTO.getReceiver()
    );
    
    synchronized(AppThread.class) {
      if(!isMessageForThisProcess) handleDTORedirect(receivedDTO);
      ConsolePrinter.updatedPrintingLocks(-1);
    }
  }

  private void handleDTORedirect(DTO receivedDTO) {
    currentDTOToSend = receivedDTO;

    ConsolePrinter.updatedPrintingLocks(1);
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
