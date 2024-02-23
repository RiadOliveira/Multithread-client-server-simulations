package src.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import src.dtos.DTO;
import src.dtos.DTOType;
import src.error.AppException;
import src.process.MessageReceiverThread;
import src.server.ServerData;
import src.utils.ConsoleOperationMessageOverwriter;

public class ClientThread implements Runnable {
  public static DTO currentDTOToSend;
  private UUID idOfPreviousDTOToSend = null, idOfPreviousDTOReceived = null;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final String connectedServer;
  private final Socket serverSocket;

  public ClientThread(String connectedServer, Socket serverSocket) {
    this.connectedServer = connectedServer;
    this.serverSocket = serverSocket;
  }

  public void run() {
    try {
      inputStream = new ObjectInputStream(serverSocket.getInputStream());
      outputStream = new ObjectOutputStream(serverSocket.getOutputStream());

      MessageReceiverThread messageReceiverThread = new MessageReceiverThread(
        connectedServer, inputStream
      );
      (new Thread(messageReceiverThread)).start();

      handleRecognitionCommunication();
      while(!ClientProcess.getClientData().isFinished()) {
        handleMessageSending();
        handleMessageReceiving(messageReceiverThread.readDTO());
      }

      inputStream.close();
      outputStream.close();
    } catch(Exception exception) {
      ConsoleOperationMessageOverwriter.print(
        exception instanceof AppException ?
        exception.getMessage() : "Erro interno do cliente!"
      );
    }
  }

  private synchronized void handleRecognitionCommunication() throws AppException {
    ConsoleOperationMessageOverwriter.print(
      "Thread enviando mensagem de reconhecimento..."
    );

    try {
      String clientName = ClientProcess.getClientData().getName(); 
      DTO recognitionDTO = new DTO(
        DTOType.UNICAST, clientName, clientName, connectedServer
      );

      outputStream.writeObject(recognitionDTO);
      ConsoleOperationMessageOverwriter.print(
        "Servidor " + connectedServer + " reconheceu o cliente com sucesso!"
      );
    } catch (Exception exception) {
      throw new AppException("Erro ao enviar mensagem de reconhecimento!");
    }
  }

  private void handleMessageSending() {
    if(alreadyUsedDTO(idOfPreviousDTOToSend, currentDTOToSend)) return;
    idOfPreviousDTOToSend = currentDTOToSend.getId();
    
    try {
      if(currentDTOToSend.getType().equals(DTOType.BROADCAST)) {
        outputStream.writeObject(currentDTOToSend);
        return;
      }

      String receiverServer = currentDTOToSend.getReceiver();
      boolean receiverIsConnectedServer = connectedServer.equals(receiverServer);
      boolean noThreadConnectedToServer = noThreadConnectedToReceiverServer(
        receiverServer
      );

      if(receiverIsConnectedServer || noThreadConnectedToServer) sendUnicastDTO();
    } catch (Exception exception) {
      ConsoleOperationMessageOverwriter.print(
        "Falha ao enviar a mensagem para " + 
        currentDTOToSend.getReceiver() + "!"
      );
    }
  }

  private boolean noThreadConnectedToReceiverServer(String receiverServer) {
    for(ServerData data : ClientProcess.getClientData().getServersToConnect()) {
      if(data.getName().equals(receiverServer)) return false;
    }

    return true;
  }

  private synchronized void sendUnicastDTO() throws Exception {
    if(currentDTOToSend == null) return;

    outputStream.writeObject(currentDTOToSend);
    currentDTOToSend = null;
  }

  private void handleMessageReceiving(DTO receivedMessage) {
    if(alreadyUsedDTO(idOfPreviousDTOReceived, receivedMessage)) return;
    idOfPreviousDTOReceived = receivedMessage.getId();

    boolean isMessageForThisClient = receivedMessage.getReceiver().equals(
      ClientProcess.getClientData().getName()
    );
    if(isMessageForThisClient) {
      receivedMessage.print();
      return;
    }

    handleMessageRedirect(receivedMessage);
  }

  private void handleMessageRedirect(DTO receivedMessage) {
    boolean canRedirectMessage = receivedMessage.getSender().equals(
      connectedServer
    );
    if(canRedirectMessage) return;

    try {
      outputStream.writeObject(receivedMessage);
    } catch (IOException e) {
      ConsoleOperationMessageOverwriter.print(
        "Falha ao redirecionar a mensagem para " + 
        receivedMessage.getReceiver() + "!"
      );
    }
  }

  private boolean alreadyUsedDTO(UUID idOfPreviousDTO, DTO currentDTO) {
    return idOfPreviousDTO != null &&
      idOfPreviousDTO.equals(currentDTO.getId());
  }
}
