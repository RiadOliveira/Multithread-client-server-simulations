package src.process.client;

import java.util.List;

import src.process.server.ServerData;

public class ClientData {
  private final String name;
  private boolean finished = false;

  private final List<ServerData> serversToConnect;

  public ClientData(String name, List<ServerData> serversToConnect) {
    this.name = name;
    this.serversToConnect = serversToConnect;
  }

  public String getName() {
    return name;
  }

  public boolean isFinished() {
    return finished;
  }

  public List<ServerData> getServersToConnect() {
    return serversToConnect;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }
}
