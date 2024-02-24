package src.process.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import src.process.AppProcess;
import src.process.server.ServerData;
import src.utils.ConsolePrinter;

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
      
      ConsolePrinter.print(
        "Cliente " + data.getName() +
        " iniciado, tentando conectar-se aos servidores..."
      );
      ConsolePrinter.updatedPrintingLocks(data.getServersToConnect().size());

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
      ConsolePrinter.print("Erro interno do cliente!");
    }
  }

  private static Socket connectToServerWithRetry(ServerData serverData) {
    String serverName = serverData.getName();
    ConsolePrinter.print(
      "Tentando conectar-se ao servidor " + serverName + "..."
    );

    Socket serverSocket = null;
    while(serverSocket == null) {
      try {
        serverSocket = new Socket(serverData.getIp(), serverData.getPort());
      } catch(Exception exception) {
        waitToReconnect(serverName);
      }
    }
    
    ConsolePrinter.print(
      "Servidor " + serverName + " conectado com sucesso!\n"
    );
    return serverSocket;
  }

  private static void waitToReconnect(String serverName) {
    ConsolePrinter.print(
      "Falha ao conectar-se ao servidor " + serverName +
      ", tentando novamente em " + WAIT_TIME_TO_TRY_RECONNECTION +
      " segundos..."
    );

    try {
      TimeUnit.SECONDS.sleep(WAIT_TIME_TO_TRY_RECONNECTION);
    } catch (Exception exception) {
      ConsolePrinter.print(
        "Falha ao esperar para tentar conectar-se ao servidor!"
      );
    }
  }

  public static ClientData getData() {
    return data;
  }
}
