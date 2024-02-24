package simulations.ringTopology;

import java.net.InetAddress;

import src.process.server.ServerData;
import src.process.server.ServerProcess;

public class P1 {
  public static void main(String[] args) {
    try {
      ServerData data = new ServerData(
        "P1", InetAddress.getLocalHost().getHostAddress(),
        11111, 1
      );

      ServerProcess.init(data);
      ServerProcess.run();
    } catch (Exception exception) {
      System.out.println("Erro ao iniciar o servidor!");
    }
  }
}
