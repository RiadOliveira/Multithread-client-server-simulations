package simulations.starTopology;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import simulations.ValidProcessesNames;
import src.process.client.ClientData;
import src.process.client.ClientProcess;
import src.process.server.ServerData;

public class P2 {
  public static void main(String[] args) {
    try {
      String serversIp = InetAddress.getLocalHost().getHostAddress();

      List<ServerData> serversToConnect = new ArrayList<>();
      serversToConnect.add(new ServerData("P1", serversIp, 11111));
      
      ClientData data = new ClientData("P2", serversToConnect);
      ClientProcess.init(data, ValidProcessesNames.names);
      ClientProcess.run();
    } catch (Exception exception) {
      System.out.println("Erro ao iniciar o cliente!");
    }
  }
}
