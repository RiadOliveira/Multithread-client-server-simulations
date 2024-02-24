package simulations.ringTopology;

import java.net.InetAddress;

import src.process.server.ServerData;
import src.process.server.ServerProcess;

public class P3 {
  public static void main(String[] args) {
    try {
      ServerData data = new ServerData(
        "P3", InetAddress.getLocalHost().getHostAddress(),
        33333, 2
      );

      ServerProcess.init(data);
      ServerProcess.run();
    } catch (Exception exception) {
      System.out.println("Erro ao iniciar o servidor!");
    }
  }
}
