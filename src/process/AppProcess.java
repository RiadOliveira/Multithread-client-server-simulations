package src.process;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

import src.constants.Constants;
import src.dtos.DTO;
import src.error.AppException;
import src.utils.ConsolePrinter;

public abstract class AppProcess {
  private static String validProcessesNames[];
  protected static final Scanner scanner = new Scanner(System.in);

  protected static String processName;
  protected static Consumer<DTO> setCurrentDTOTOSend;
  protected static Supplier<Integer> getThreadsQuantity;

  protected static void init(
    String processName, String validProcessesNames[],
    Consumer<DTO> setCurrentDTOTOSend,
    Supplier<Integer> getThreadsQuantity
  ) {
    AppProcess.processName = processName;
    AppProcess.validProcessesNames = validProcessesNames;
    AppProcess.setCurrentDTOTOSend = setCurrentDTOTOSend;
    AppProcess.getThreadsQuantity = getThreadsQuantity;
  }

  protected static void handleInputReceiving() {
    boolean choseFinalizeOption = false;
    while(!choseFinalizeOption) {
      choseFinalizeOption = processUserInputCheckingIfFinalized();
    }
  }

  private static boolean processUserInputCheckingIfFinalized() {
    while(ConsolePrinter.printingHasLocks());

    String operationData = scanner.nextLine();
    ConsolePrinter.moveToPreviousLine();
    if(operationData.equalsIgnoreCase(Constants.FINALIZE_OPTION)) {
      return true;
    }

    try {
      boolean isClearOption = operationData.equalsIgnoreCase(
        Constants.CLEAR_CONSOLE_OPTION
      );

      if(isClearOption) {
        ConsolePrinter.clearConsole();
        ConsolePrinter.printOperationMessage();
      } else executeSendingOperation(operationData);
    } catch (AppException exception) {
      ConsolePrinter.println(exception.getMessage());
      ConsolePrinter.printOperationMessage();
    }

    return false;
  }

  private static void executeSendingOperation(
    String operationData
  ) throws AppException {
    String splittedOperationData[] = operationData.split(";");
    
    try {
      String receiver = getParsedReceiver(splittedOperationData[0]);
      String message = splittedOperationData[1];
      
      ConsolePrinter.updatePrintingLocksHandlingOperationMessage(
        receiver.equals(Constants.BROADCAST_RECEIVER) ?
        getThreadsQuantity.get() : 1
      );
      setCurrentDTOTOSend.accept(new DTO(message, processName, receiver));
    } catch (Exception exception) {
      throw new AppException("Erro na entrada de dados. Tente outra vez!");
    }
  }

  private static String getParsedReceiver(String receiver) throws Exception {
    String parsedReceiver = receiver.toUpperCase().trim();
    boolean isBroadcastReceiver = parsedReceiver.startsWith("B");

    if(!isBroadcastReceiver && !isValidProcessReceiver(parsedReceiver)) {
      throw new Exception();
    }

    return isBroadcastReceiver ? Constants.BROADCAST_RECEIVER : parsedReceiver;
  }

  private static boolean isValidProcessReceiver(String receiver) {
    for(String name : validProcessesNames) {
      if(name.equals(receiver)) return true;
    }

    return false;
  }
}
