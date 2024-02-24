package src.utils;

import java.lang.Math;
import src.constants.Constants;
import src.dtos.DTO;
import src.process.client.ClientProcess;
import src.process.server.ServerProcess;

public class ConsolePrinter {
  private static final String clearConsoleString = "\033[H\033[2J";

  private static String operationMessageWithProcessName = null;
  private static String clearCurrentLineString = null;
  private static int printingLocks = 0;

  public static synchronized void print(String content) {
    System.out.println(content);
  }

  public static synchronized void printReinsertingOperationMessage(String content) {
    initOperationMessagePropertiesWithProcessName();
    print(
      clearCurrentLineString + content +
      "\n" + operationMessageWithProcessName
    );
  }

  public static synchronized void printSendingDTO(DTO dto, String intermediaryProcess) {
    if(dto == null) return;

    String receiver = dto.getReceiver();
    String intermediaryProcessString = !intermediaryProcess.equals(receiver) ?
    " por meio de " + intermediaryProcess : "";

    String transmissionString = 
      "enviado" + intermediaryProcessString + " (" + 
      dto.getSender() + " -> " + receiver + ")";
    printDTO(dto, transmissionString);
  }

  public static synchronized void printReceivingDTO(DTO dto) {
    if(dto == null) return;

    String transmissionString = 
      "recebido (" + dto.getReceiver() + " <- " + dto.getSender() + ")";
    printDTO(dto, transmissionString);
  }

  private static void printDTO(DTO dto, String transmissionString) {
    System.out.print(
      "\nDados do DTO " + transmissionString +
      ":\n" + dto.getPrintableString()
    );
  }

  public static synchronized void clearConsole() {
    System.out.print(clearConsoleString);  
    System.out.flush();  
  }

  public static synchronized void updatedPrintingLocks(int updateValue) {
    if(printingLocks == 0 && updateValue > 0) {
      initOperationMessagePropertiesWithProcessName();
      System.out.print(clearCurrentLineString);
    }

    printingLocks = Math.max(0, printingLocks + updateValue);
    if(!printingHasLocks()) printOperationMessage();
  }

  public static synchronized boolean printingHasLocks() {
    return printingLocks > 0;
  }

  public static void printOperationMessage() {
    initOperationMessagePropertiesWithProcessName();
    System.out.print(operationMessageWithProcessName);
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

    clearCurrentLineString = 
      "\r" + " ".repeat(operationMessageWithProcessName.length()) + "\r";
  }
}
