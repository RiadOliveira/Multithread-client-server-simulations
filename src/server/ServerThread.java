package src.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import src.dtos.DTO;
import src.dtos.DTOType;
import src.error.AppException;
import src.process.MessageReceiverThread;
import src.utils.ConsoleOperationMessageOverwriter;

public class ServerThread implements Runnable {
  private static List<String> connectedClients;
  private int connectedClientIndex;

  public static DTO currentDTOToSend;
  private UUID idOfPreviousDTOToSend = null, idOfPreviousDTOReceived = null;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final Socket clientSocket;

  public ServerThread(Socket clientSocket) {
    this.clientSocket = clientSocket;

    if(connectedClients == null) {
      connectedClients = new ArrayList<String>(
        ServerProcess.getServerData().getquantityOfClientsToConnect()
      );
    };
  }

  public void run() {
    try {
      inputStream = new ObjectInputStream(clientSocket.getInputStream());
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

      MessageReceiverThread messageReceiverThread = new MessageReceiverThread(
        connectedClients.get(connectedClientIndex), inputStream
      );
      (new Thread(messageReceiverThread)).start();

      handleRecognitionCommunication();
      while(!ServerProcess.getServerData().isClosed()) {
        handleMessageSending();
        handleMessageReceiving(messageReceiverThread.readDTO());
      }

      inputStream.close();
      outputStream.close();
    } catch(Exception exception) {
      ConsoleOperationMessageOverwriter.print(
        exception instanceof AppException ?
        exception.getMessage() : "Erro interno do servidor!"
      );
    }
  }

  private synchronized void handleRecognitionCommunication() throws AppException {
    ConsoleOperationMessageOverwriter.print(
      "Thread aguardando mensagem de reconhecimento..."
    );

    try {
      DTO recognitionDTO = (DTO) inputStream.readObject();

      connectedClients.add(recognitionDTO.getMessage());
      connectedClientIndex = connectedClients.size();

      ConsoleOperationMessageOverwriter.print(
        "Cliente " + connectedClients.get(connectedClientIndex) +
        " reconhecido com sucesso!"
      );
    } catch (Exception exception) {
      throw new AppException("Erro ao ler mensagem de reconhecimento!");
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

      int receiverIndex = connectedClients.indexOf(currentDTOToSend.getReceiver());
      boolean receiverIsConnectedClient = receiverIndex == connectedClientIndex;
      boolean noThreadConnectedToClient = receiverIndex == -1;

      if(receiverIsConnectedClient || noThreadConnectedToClient) sendUnicastDTO();
    } catch (Exception exception) {
      ConsoleOperationMessageOverwriter.print(
        "Falha ao enviar a mensagem para " + 
        currentDTOToSend.getReceiver() + "!"
      );
    }
  }

  private synchronized void sendUnicastDTO() throws Exception {
    if(currentDTOToSend == null) return;

    outputStream.writeObject(currentDTOToSend);
    currentDTOToSend = null;
  }

  private void handleMessageReceiving(DTO receivedMessage) {
    if(alreadyUsedDTO(idOfPreviousDTOReceived, receivedMessage)) return;
    idOfPreviousDTOReceived = receivedMessage.getId();

    boolean isMessageForThisServer = receivedMessage.getReceiver().equals(
      ServerProcess.getServerData().getName()
    );
    if(isMessageForThisServer) {
      receivedMessage.print();
      return;
    }

    handleMessageRedirect(receivedMessage);
  }

  private void handleMessageRedirect(DTO receivedMessage) {
    boolean canRedirectMessage = receivedMessage.getSender().equals(
      connectedClients.get(connectedClientIndex)
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
