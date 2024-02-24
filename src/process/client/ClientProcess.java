package src.process.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import src.process.AppProcess;
import src.process.server.ServerData;
import src.utils.ConsoleOperationMessageOverwriter;

public class ClientProcess extends AppProcess {
  private static ClientData data;
  private static final int WAIT_TIME_TO_TRY_RECONNECTION = 5;
  
  public static void init(ClientData data) {
    AppProcess.init(data.getName(), ClientThread::setCurrentDTOTOSend);
    ClientProcess.data = data;
  }

  public static void run() {
    try {
      List<Socket> serverSockets = new ArrayList<>(
        data.getServersToConnect().size()
      );

      for(ServerData serverData : data.getServersToConnect()) {
        Socket socket = connectToServerWithRetry(serverData);
        Thread clientThread = new Thread(
          new ClientThread(serverData.getName(), socket)
        );

        serverSockets.add(socket);
        clientThread.start();
      }

      handleOperationInput();

      for(Socket socket : serverSockets) socket.close();
      scanner.close();
      data.setFinished(true);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private static Socket connectToServerWithRetry(ServerData serverData) {
    String serverName = serverData.getName();
    ConsoleOperationMessageOverwriter.print(
      "Tentando conectar-se ao servidor " + serverName + " ..."
    );

    Socket serverSocket = null;
    while(serverSocket == null) {
      try {
        serverSocket = new Socket(serverData.getIp(), serverData.getPort());
      } catch(Exception exception) {
        waitToReconnect(serverName);
      }
    }
    
    ConsoleOperationMessageOverwriter.print(
      "Servidor " + serverName + " conectado com sucesso!"
    );
    return serverSocket;
  }

  private static void waitToReconnect(String serverName) {
    ConsoleOperationMessageOverwriter.print(
      "Falha ao conectar-se ao servidor " + serverName +
      ", tentando novamente em " + WAIT_TIME_TO_TRY_RECONNECTION +
      " segundos ..."
    );

    try {
      TimeUnit.SECONDS.sleep(WAIT_TIME_TO_TRY_RECONNECTION);
    } catch (Exception exception) {
      ConsoleOperationMessageOverwriter.print(
        "Falha ao esperar para tentar conectar-se ao servidor!"
      );
    }
  }

  public static ClientData getClientData() {
    return data;
  }
}
