package src.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import src.constants.Constants;
import src.dtos.DTO;
import src.dtos.DTOType;
import src.error.AppException;

public class ServerProcess {
  private static final Scanner scanner = new Scanner(System.in);
  private static ServerData data;
  
  public static void init(ServerData data) {
    ServerProcess.data = data;
  }

  public static ServerData getServerData() {
    return data;
  }

  public static void run() {
    try {
      ServerSocket serverSocket = new ServerSocket(data.getPort());
      for(int ind=0; ind<data.getquantityOfProcessesToConnect(); ind++) {
        Socket clientSocket = serverSocket.accept();
        Thread serverThread = new Thread(new ServerThread(clientSocket));
        serverThread.start();
      }

      handleOperationInput();

      serverSocket.close();
      data.setClosed(true);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private static void handleOperationInput() {
    System.out.print(Constants.OPERATION_MESSAGE);
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
      
      ServerThread.currentDTOToSend = new DTO(type, message, data.getName(), receiver);
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
