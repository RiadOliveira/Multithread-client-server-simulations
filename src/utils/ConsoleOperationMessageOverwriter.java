package src.utils;

import src.constants.Constants;

public class ConsoleOperationMessageOverwriter {
  private static final String clearCurrentLineString =
    "\r" + " ".repeat(Constants.LENGTH_OF_OPERATION_MESSAGE) + "\r";

  public static void print(String content) {
    System.out.print(clearCurrentLineString);
    if(!content.isBlank()) System.out.println(content);
    System.out.print(Constants.OPERATION_MESSAGE);
  }
}
