package src.process.server;

import java.net.ServerSocket;
import java.net.Socket;

import src.process.AppProcess;

public class ServerProcess extends AppProcess {
  private static ServerData data;
  
  public static void init(ServerData data) {
    AppProcess.init(data.getName(), ServerThread::setCurrentDTOTOSend);
    ServerProcess.data = data;
  }

  public static void run() {
    try {
      ServerSocket serverSocket = new ServerSocket(data.getPort());
      System.out.println("Servidor iniciado, aguardando clientes...");

      for(int ind=0; ind<data.getquantityOfClientsToConnect(); ind++) {
        Socket clientSocket = serverSocket.accept();
        Thread serverThread = new Thread(new ServerThread(clientSocket));
        serverThread.start();
      }

      handleOperationInput();

      serverSocket.close();
      scanner.close();
      data.setClosed(true);
    } catch (Exception exception) {
      System.out.println("Erro interno do servidor!");
    }
  }

  public static ServerData getServerData() {
    return data;
  }
}
