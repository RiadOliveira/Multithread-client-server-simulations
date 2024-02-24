package src.utils;

import java.lang.Math;
import src.constants.Constants;
import src.dtos.DTO;
import src.process.client.ClientProcess;
import src.process.server.ServerProcess;

public class ConsolePrinter {
  private static String processName = null;

  private static final String clearConsoleString = "\033[H\033[2J";
  private static final String clearCurrentLineString =
    "\r" + " ".repeat(Constants.LENGTH_OF_OPERATION_MESSAGE) + "\r";

  private static int printingLocks = 0;

  public static synchronized void print(String content) {
    System.out.println(content);
  }

  public static synchronized void printReinsertingOperationMessage(
    String content
  ) {
    print(
      clearCurrentLineString + content +
      "\n" + Constants.OPERATION_MESSAGE
    );
  }

  public static synchronized void printDTO(DTO dto, boolean isSending) {
    if(dto == null) return;

    String sender = dto.getSender();
    String receiver = dto.getReceiver();
    String transmissionString = isSending ?
      ("enviado (" + sender + " -> " + receiver + ")") :
      ("recebido (" + receiver + " <- " + sender + ")");

    System.out.print(
      "\nDados do DTO " + transmissionString +
      ":\n" + dto.getPrintableString()
    );
  }

  public static void clearConsole() {
    System.out.print(clearConsoleString);  
    System.out.flush();  
  }

  public static synchronized void updatedPrintingLocks(boolean increaseLocks) {
    if(printingLocks == 0 && increaseLocks) {
      System.out.print(clearCurrentLineString);
    }

    printingLocks = Math.max(0, printingLocks + (increaseLocks ? 1 : -1));
    if(!printingIsLocked()) printOperationMessage();
  }

  public static synchronized boolean printingIsLocked() {
    return printingLocks > 0;
  }

  public static void printOperationMessage() {
    initProcessName();
    System.out.print("\n(" + processName + ") " + Constants.OPERATION_MESSAGE);
  }

  private static void initProcessName() {
    if(processName != null) return;

    try {
      processName = ServerProcess.getData().getName();
    } catch (Exception exception) {
      processName = ClientProcess.getData().getName();
    }
  }
}
