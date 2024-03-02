package src.utils;

import java.lang.Math;
import src.constants.Constants;
import src.dtos.DTO;
import src.process.client.ClientProcess;
import src.process.server.ServerProcess;

public class ConsolePrinter {
  private static final String CLEAR_CONSOLE = "\033[H\033[2J";
  private static final String MOVE_TO_PREVIOUS_LINE = "\033[1A";
  private static final String CLEAR_CURRENT_LINE = "\033[2K";
  private static final String OVERWRITE_OPERATION_MESSAGE = 
    MOVE_TO_PREVIOUS_LINE + CLEAR_CURRENT_LINE +
    MOVE_TO_PREVIOUS_LINE + CLEAR_CURRENT_LINE;

  private static String operationMessageWithProcessName = null;
  private static int printingLocks = 0;

  public static synchronized void print(String content) {
    System.out.print(content);
  }

  public static synchronized void println(String content) {
    System.out.println(content);
  }

  public static synchronized void moveToPreviousLine() {
    print(MOVE_TO_PREVIOUS_LINE);
  }

  public static synchronized void printReinsertingOperationMessage(String content) {
    initOperationMessagePropertiesWithProcessName();
    print(
      "\n" + OVERWRITE_OPERATION_MESSAGE + content +
      "\n" + operationMessageWithProcessName
    );
  }

  public static synchronized void printDTO(DTO dto, String intermediaryProcess, boolean isSending) {
    if(dto == null) return;

    String receiver = dto.getReceiver();
    String transmissionWord = isSending ? "enviado" : "recebido";
    String intermediaryProcessString = !intermediaryProcess.equals(receiver) ?
      " por meio de " + intermediaryProcess : "";

    String transmissionString = 
      transmissionWord + intermediaryProcessString + " (" + 
      dto.getSender() + " -> " + receiver + ")";

    print(
      "\nDados do DTO " + transmissionString +
      ":\n" + dto.getPrintableString()
    );
  }

  public static synchronized void clearConsole() {
    print(CLEAR_CONSOLE);  
    System.out.flush();  
  }

  public static synchronized void updatePrintingLocks(int updateValue) {
    if(printingLocks == 0 && updateValue > 0) print(OVERWRITE_OPERATION_MESSAGE);
  
    boolean hadLocksBeforeUpdate = printingHasLocks();
    printingLocks = Math.max(0, printingLocks + updateValue);
    if(hadLocksBeforeUpdate && !printingHasLocks()) printOperationMessage();
  }

  public static synchronized boolean printingHasLocks() {
    return printingLocks > 0;
  }

  public static void printOperationMessage() {
    initOperationMessagePropertiesWithProcessName();
    print(operationMessageWithProcessName);
  }

  private static void initOperationMessagePropertiesWithProcessName() {
    if(operationMessageWithProcessName != null) return;

    String processName;
    try {
      processName = ServerProcess.getData().getName();
    } catch (Exception exception) {
      processName = ClientProcess.getData().getName();
    }

    operationMessageWithProcessName = 
      "\n(" + processName + ") " + Constants.OPERATION_MESSAGE;
  }
}
