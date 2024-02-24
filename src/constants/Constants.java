package src.constants;

public class Constants {
  public static final String EXIT_OPTION = "S";
  public static final String CLEAR_CONSOLE_OPTION = "L";

  public static final String OPERATION_MESSAGE = 
    "[Transmitir: (B - Broad | Processo - Uni);(Mensagem) | " +
    "Limpar: " + CLEAR_CONSOLE_OPTION + " | Sair: " + EXIT_OPTION + "]: ";

  public static final int LENGTH_OF_OPERATION_MESSAGE = 
    OPERATION_MESSAGE.length();

  public static final String BROADCAST_RECEIVER = "BROADCAST";
}
