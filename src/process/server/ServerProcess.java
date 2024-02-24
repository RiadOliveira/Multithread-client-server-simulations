package src.process.server;

import java.net.ServerSocket;
import java.net.Socket;

import src.process.AppProcess;
import src.utils.ConsolePrinter;

public class ServerProcess extends AppProcess {
  private static ServerData data;
  
  public static void init(ServerData data) {
    AppProcess.init(data.getName(), ServerThread::setCurrentDTOTOSend);
    ServerProcess.data = data;
  }

  public static void run() {
    try {
      ServerSocket serverSocket = new ServerSocket(data.getPort());
      ConsolePrinter.print(
        "Servidor " + data.getName() +
        " iniciado, aguardando clientes..."
      );

      for(int ind=0; ind<data.getquantityOfClientsToConnect(); ind++) {
        Socket clientSocket = serverSocket.accept();
        Thread serverThread = new Thread(new ServerThread(clientSocket));
        
        ConsolePrinter.updatedPrintingLocks(true);
        serverThread.start();
      }

      handleOperationInput();

      serverSocket.close();
      scanner.close();
      data.setClosed(true);
    } catch (Exception exception) {
      ConsolePrinter.print("Erro interno do servidor!");
    }
  }

  public static ServerData getData() {
    return data;
  }
}
