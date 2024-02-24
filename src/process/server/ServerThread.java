package src.process.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import src.dtos.DTO;
import src.error.AppException;
import src.process.AppThread;
import src.utils.ConsoleOperationMessageOverwriter;

public class ServerThread extends AppThread {
  private static List<String> connectedClients;
  private int connectedClientIndex;

  public ServerThread(Socket clientSocket) {
    super(clientSocket, true);

    if(connectedClients == null) {
      connectedClients = new ArrayList<String>(
        ServerProcess.getServerData().getquantityOfClientsToConnect()
      );
    };
  }

  @Override
  protected void handleRecognitionCommunication() throws AppException {
    ConsoleOperationMessageOverwriter.print(
      "Thread aguardando mensagem de reconhecimento..."
    );

    try {
      DTO recognitionDTO = (DTO) inputStream.readObject();

      connectedClientIndex = connectedClients.size();
      connectedClients.add(recognitionDTO.getMessage());

      ConsoleOperationMessageOverwriter.print(
        "Cliente " + connectedClients.get(connectedClientIndex) +
        " reconhecido com sucesso!"
      );
    } catch (Exception exception) {
      exception.printStackTrace();
      throw new AppException("Erro ao ler mensagem de reconhecimento!");
    }
  }

  @Override
  protected boolean canSendUnicastToReceiver(String receiverClient) {
    int receiverIndex = connectedClients.indexOf(receiverClient);
    boolean receiverIsConnectedClient = receiverIndex == connectedClientIndex;
    boolean noThreadConnectedToClient = receiverIndex == -1;

    return receiverIsConnectedClient || noThreadConnectedToClient;
  }

  @Override
  protected boolean processFinished() {
    synchronized(this) {
      return ServerProcess.getServerData().isClosed();
    }
  }

  @Override
  protected String getConnectedProcess() {
    return connectedClients.get(connectedClientIndex);
  }
}
