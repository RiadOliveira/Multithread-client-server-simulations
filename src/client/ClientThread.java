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
  public static DTO currentDTOToSend = null;
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
      outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
      inputStream = new ObjectInputStream(serverSocket.getInputStream());

      handleRecognitionCommunication();
      MessageReceiverThread messageReceiverThread = MessageReceiverThread.
        GenerateAndStartMessageReceiverThread(connectedServer, inputStream);

      while(!clientFinished()) {
        handleMessageSending();
        handleMessageReceiving(messageReceiverThread.readDTO());
      }

      inputStream.close();
      outputStream.close();
    } catch(Exception exception) {
      exception.printStackTrace();
      ConsoleOperationMessageOverwriter.print(
        exception instanceof AppException ?
        exception.getMessage() : "Erro interno do cliente!"
      );
    }
  }

  private synchronized boolean clientFinished() {
    return ClientProcess.getClientData().isFinished();
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

  private synchronized void handleMessageSending() {
    if(alreadyUsedOrInvalidDTO(idOfPreviousDTOToSend, currentDTOToSend)) return;
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

      if(receiverIsConnectedServer || noThreadConnectedToServer) {
        outputStream.writeObject(currentDTOToSend);
        ClientThread.currentDTOToSend = null;
      }
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

  private void handleMessageReceiving(DTO receivedMessage) {
    if(alreadyUsedOrInvalidDTO(idOfPreviousDTOReceived, receivedMessage)) return;

    idOfPreviousDTOReceived = receivedMessage.getId();
    receivedMessage.print();
    
    boolean isMessageForThisClient = ClientProcess.getClientData().getName().equals(
      receivedMessage.getReceiver()
    );
    if(!isMessageForThisClient) handleMessageRedirect(receivedMessage);
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

  private boolean alreadyUsedOrInvalidDTO(UUID idOfPreviousDTO, DTO currentDTO) {
    if(currentDTO == null) return true;
    return idOfPreviousDTO != null &&idOfPreviousDTO.equals(currentDTO.getId());
  }
}
