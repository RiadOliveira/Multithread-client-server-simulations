package src.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import src.constants.Constants;
import src.dtos.DTO;
import src.dtos.DTOType;
import src.error.AppException;
import src.server.ServerData;
import src.utils.ConsoleOperationMessageOverwriter;

public class ClientProcess {
  private static final Scanner scanner = new Scanner(System.in);
  private static final int WAIT_TIME_TO_TRY_RECONNECTION = 5;
  private static ClientData data;
  
  public static void init(ClientData data) {
    ClientProcess.data = data;
  }

  public static ClientData getClientData() {
    return data;
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

  private static void handleOperationInput() {
    ConsoleOperationMessageOverwriter.print("");
    String operationData = scanner.nextLine();
    if(operationData.equalsIgnoreCase(Constants.EXIT_OPTION)) return;

    try {
      executeReceivedOperation(operationData);
    } catch (AppException exception) {
      System.out.println(exception.getMessage());
    } finally {
      handleOperationInput();
    }
  }

  private static void executeReceivedOperation(
    String operationData
  ) throws AppException {
    String splittedOperationData[] = operationData.split(";");
    DTOType type = extractTypeFromSplittedOperationData(
      splittedOperationData[0]
    );
    boolean isUnicast = type.equals(DTOType.UNICAST);
    
    try {
      String receiver = isUnicast ? splittedOperationData[1] : null;
      String message = splittedOperationData[isUnicast ? 2 : 1];
      
      ClientThread.currentDTOToSend = new DTO(
        type, message, data.getName(), receiver
      );
    } catch (Exception exception) {
      if(exception instanceof AppException) throw exception;
      throw new AppException("Erro na entrada de dados. Tente outra vez!");
    }
  }

  private static DTOType extractTypeFromSplittedOperationData(
    String typeAsString
  ) throws AppException {
    String parsedType = typeAsString.trim().toUpperCase();

    if(parsedType.startsWith("U")) return DTOType.UNICAST;
    if(parsedType.startsWith("B")) return DTOType.BROADCAST;
    throw new AppException("Tipo de mensagem inválido!");
  }
}
