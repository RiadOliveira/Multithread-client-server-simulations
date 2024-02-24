package src.process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import src.constants.Constants;
import src.dtos.DTO;
import src.error.AppException;
import src.process.client.ClientProcess;
import src.utils.ConsoleOperationMessageOverwriter;

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

      synchronized(this) {handleRecognitionCommunication();};
      MessageReceiverThread messageReceiverThread = MessageReceiverThread.
        GenerateAndStartMessageReceiverThread(getConnectedProcess(), inputStream);

      while(!processFinished()) {
        handleDTOSending();
        handleDTOReceiving(messageReceiverThread.readDTO());
      }

      inputStream.close();
      outputStream.close();
    } catch(Exception exception) {
      ConsoleOperationMessageOverwriter.print(
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
      boolean isBroadcast = receiver.equals(Constants.BROADCAST_RECEIVER);
      boolean canSendUnicast = canSendUnicastToReceiver(receiver);

      if(isBroadcast || canSendUnicast) outputStream.writeObject(currentDTOToSend);
      if(canSendUnicast) AppThread.currentDTOToSend = null;
    } catch (Exception exception) {
      ConsoleOperationMessageOverwriter.print(
        "Falha ao enviar a mensagem para " + receiver + "!"
      );
    }
  }

  protected void handleDTOReceiving(DTO receivedDTO) {
    if(alreadyUsedOrInvalidDTO(idOfPreviousDTOReceived, receivedDTO)) return;

    idOfPreviousDTOReceived = receivedDTO.getId();
    receivedDTO.print();
    
    boolean isMessageForThisProcess = ClientProcess.getClientData().getName().equals(
      receivedDTO.getReceiver()
    );
    if(!isMessageForThisProcess) handleDTORedirect(receivedDTO);
  }

  private void handleDTORedirect(DTO receivedDTO) {
    boolean canRedirectMessage = receivedDTO.getSender().equals(
      getConnectedProcess()
    );
    if(canRedirectMessage) return;

    try {
      outputStream.writeObject(receivedDTO);
    } catch (IOException e) {
      ConsoleOperationMessageOverwriter.print(
        "Falha ao redirecionar a mensagem para " + 
        receivedDTO.getReceiver() + "!"
      );
    }
  }

  private boolean alreadyUsedOrInvalidDTO(UUID idOfPreviousDTO, DTO currentDTO) {
    if(currentDTO == null) return true;
    return idOfPreviousDTO != null &&idOfPreviousDTO.equals(currentDTO.getId());
  }

  public static void setCurrentDTOTOSend(DTO currentDTOTOSend) {
    AppThread.currentDTOToSend = currentDTOTOSend;
  }
}
