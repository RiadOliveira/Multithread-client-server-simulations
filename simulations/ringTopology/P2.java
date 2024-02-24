package simulations.ringTopology;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import src.process.client.ClientData;
import src.process.client.ClientProcess;
import src.process.server.ServerData;

public class P2 {
  public static void main(String[] args) {
    try {
      String serversIp = InetAddress.getLocalHost().getHostAddress();

      List<ServerData> serversToConnect = new ArrayList<>();
      serversToConnect.add(new ServerData("P1", serversIp, 11111));
      serversToConnect.add(new ServerData("P3", serversIp, 33333));
      
      ClientData data = new ClientData("P2", serversToConnect);
      ClientProcess.init(data);
      ClientProcess.run();
    } catch (Exception exception) {
      System.out.println("Erro ao iniciar o cliente!");
    }
  }
}
