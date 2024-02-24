package src.process.client;

import java.net.Socket;

import src.dtos.DTO;
import src.error.AppException;
import src.process.AppThread;
import src.process.server.ServerData;
import src.utils.ConsoleOperationMessageOverwriter;

public class ClientThread extends AppThread {
  private final String connectedServer;

  public ClientThread(String connectedServer, Socket serverSocket) {
    super(serverSocket, false);
    this.connectedServer = connectedServer;
  }

  @Override
  protected void handleRecognitionCommunication() throws AppException {
    ConsoleOperationMessageOverwriter.print(
      "Thread enviando mensagem de reconhecimento..."
    );

    try {
      String clientName = ClientProcess.getClientData().getName(); 
      DTO recognitionDTO = new DTO(clientName, clientName, connectedServer);

      outputStream.writeObject(recognitionDTO);
      ConsoleOperationMessageOverwriter.print(
        "Servidor " + connectedServer + " reconheceu o cliente com sucesso!"
      );
    } catch (Exception exception) {
      throw new AppException("Erro ao enviar mensagem de reconhecimento!");
    }
  }

  @Override
  protected boolean canSendUnicastToReceiver(String receiverServer) {
    boolean receiverIsConnectedServer = connectedServer.equals(receiverServer);
    boolean noThreadConnectedToServer = noThreadConnectedToReceiverServer(
      receiverServer
    );

    return receiverIsConnectedServer || noThreadConnectedToServer;
  }

  private boolean noThreadConnectedToReceiverServer(String receiverServer) {
    for(ServerData data : ClientProcess.getClientData().getServersToConnect()) {
      if(data.getName().equals(receiverServer)) return false;
    }

    return true;
  }

  @Override
  protected boolean processFinished() {
    synchronized(this) {
      return ClientProcess.getClientData().isFinished();
    }
  }

  @Override
  protected String getConnectedProcess() {
    return connectedServer;
  }
}
