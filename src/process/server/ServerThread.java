package src.process.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import src.dtos.DTO;
import src.error.AppException;
import src.process.AppThread;
import src.utils.ConsolePrinter;

public class ServerThread extends AppThread {
  private static List<String> connectedClients;
  private int connectedClientIndex;

  public ServerThread(Socket clientSocket) {
    super(clientSocket, true);

    if(connectedClients == null) {
      connectedClients = new ArrayList<String>(
        ServerProcess.getData().getQuantityOfClientsToConnect()
      );
    };
  }

  @Override
  protected void handleRecognitionCommunication() throws AppException {
    ConsolePrinter.println("Thread aguardando mensagem de reconhecimento...");

    try {
      DTO recognitionDTO = (DTO) inputStream.readObject();

      connectedClientIndex = connectedClients.size();
      connectedClients.add(recognitionDTO.getSender());

      ConsolePrinter.println(
        "Cliente " + getConnectedProcess() + " reconhecido com sucesso!\n"
      );
    } catch (Exception exception) {
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
    return ServerProcess.getData().isClosed();
  }

  @Override
  protected String getProcessName() {
    return ServerProcess.getData().getName();
  }

  @Override
  protected String getConnectedProcess() {
    return connectedClients.get(connectedClientIndex);
  }
}
