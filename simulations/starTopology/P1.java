package simulations.starTopology;

import java.net.InetAddress;

import simulations.ValidProcessesNames;
import src.process.server.ServerData;
import src.process.server.ServerProcess;

public class P1 {
  public static void main(String[] args) {
    try {
      ServerData data = new ServerData(
        "P1", InetAddress.getLocalHost().getHostAddress(),
        11111, 3
      );

      ServerProcess.init(data, ValidProcessesNames.names);
      ServerProcess.run();
    } catch (Exception exception) {
      System.out.println("Erro ao iniciar o servidor!");
    }
  }
}
