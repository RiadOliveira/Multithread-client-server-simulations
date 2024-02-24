package src.process;

import java.util.Scanner;
import java.util.function.Consumer;

import src.constants.Constants;
import src.dtos.DTO;
import src.error.AppException;
import src.utils.ConsolePrinter;

public abstract class AppProcess {
  protected static final Scanner scanner = new Scanner(System.in);
  protected static Consumer<DTO> setCurrentDTOTOSend;
  protected static String processName;

  protected static void init(
    String processName, Consumer<DTO> setCurrentDTOTOSend
  ) {
    AppProcess.processName = processName;
    AppProcess.setCurrentDTOTOSend = setCurrentDTOTOSend;
  }

  protected static void handleOperationInput() {
    while(ConsolePrinter.printingIsLocked());

    String operationData = scanner.nextLine();
    if(operationData.equalsIgnoreCase(Constants.EXIT_OPTION)) return;

    try {
      boolean isClearOption = operationData.equalsIgnoreCase(
        Constants.CLEAR_CONSOLE_OPTION
      );

      if(isClearOption) {
        ConsolePrinter.clearConsole();
        ConsolePrinter.printOperationMessage();
      } else executeSendingOperation(operationData);
    } catch (AppException exception) {
      ConsolePrinter.print(exception.getMessage());
    } finally {
      handleOperationInput();
    }
  }

  private static void executeSendingOperation(
    String operationData
  ) throws AppException {
    String splittedOperationData[] = operationData.split(";");
    
    try {
      String receiver = getParsedReceiver(splittedOperationData[0]);
      String message = splittedOperationData[1];
      
      ConsolePrinter.updatedPrintingLocks(true);
      setCurrentDTOTOSend.accept(new DTO(message, processName, receiver));
    } catch (Exception exception) {
      if(exception instanceof AppException) throw exception;
      throw new AppException("Erro na entrada de dados. Tente outra vez!");
    }
  }

  private static String getParsedReceiver(String receiver) {
    String parsedReceiver = receiver.toUpperCase().trim();
    boolean isBroadcastReceiver = parsedReceiver.startsWith("B");

    return isBroadcastReceiver ? Constants.BROADCAST_RECEIVER : parsedReceiver;
  }
}
