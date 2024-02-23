package simulations.ringTopology;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import src.client.ClientData;
import src.client.ClientProcess;
import src.server.ServerData;

public class P4 {
  public static void main(String[] args) {
    try {
      String serversIp = InetAddress.getLocalHost().getHostAddress();

      List<ServerData> serversToConnect = new ArrayList<>();
      serversToConnect.add(new ServerData("P3", serversIp, 33333));
      serversToConnect.add(new ServerData("P1", serversIp, 11111));
      
      ClientData data = new ClientData("P4", serversToConnect);
      ClientProcess.init(data);
      ClientProcess.run();
    } catch (Exception exception) {
      System.out.println("Erro ao iniciar o cliente!");
    }
  }
}
