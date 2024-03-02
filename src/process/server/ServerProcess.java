package src.process.server;

import java.net.ServerSocket;
import java.net.Socket;

import src.process.AppProcess;
import src.utils.ConsolePrinter;

public class ServerProcess extends AppProcess {
  private static ServerData data;
  
  public static void init(ServerData data, String validProcessesNames[]) {
    AppProcess.init(
      data.getName(), validProcessesNames,
      ServerThread::setCurrentDTOTOSend,
      ServerProcess::getThreadsQuantity
    );
    ServerProcess.data = data;
  }

  public static void run() {
    try {
      ServerSocket serverSocket = new ServerSocket(data.getPort());
      ConsolePrinter.println(
        "Servidor " + data.getName() + " iniciado, aguardando clientes...\n"
      );
      ConsolePrinter.updatePrintingLocks(data.getQuantityOfClientsToConnect());

      for(int ind=0; ind<data.getQuantityOfClientsToConnect(); ind++) {
        Socket clientSocket = serverSocket.accept();
        Thread serverThread = new Thread(new ServerThread(clientSocket));
        serverThread.start();
      }

      handleOperationInput();

      data.setClosed(true);
      scanner.close();
      serverSocket.close();
    } catch (Exception exception) {
      ConsolePrinter.println("Erro interno do servidor!");
    }
  }

  public static ServerData getData() {
    return data;
  }

  protected static int getThreadsQuantity() {
    return data.getQuantityOfClientsToConnect();
  }
}
