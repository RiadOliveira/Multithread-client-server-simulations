package src.utils;

import java.lang.Math;
import src.constants.Constants;
import src.dtos.DTO;

public class ConsolePrinter {
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
  
  public static void printOperationMessage() {
    System.out.print("\n" + Constants.OPERATION_MESSAGE);
  }

  public static synchronized void updatedPrintingLocks(boolean increaseLocks) {
    if(printingLocks == 0 && increaseLocks) {
      System.out.print(clearCurrentLineString);
    }

    printingLocks = Math.max(0, printingLocks + (increaseLocks ? 1 : -1));
  }

  public static synchronized boolean printingIsLocked() {
    return printingLocks > 0;
  }
}
