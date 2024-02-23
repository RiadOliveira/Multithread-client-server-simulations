package src.server;

public class ServerData {
  private final String name, ip;
  private final int port;
  private final int quantityOfClientsToConnect;
  private boolean closed = false;

  public ServerData(
    String name, String ip, int port,
    int quantityOfClientsToConnect
  ) {
    this.name = name;
    this.ip = ip;
    this.port = port;
    this.quantityOfClientsToConnect = quantityOfClientsToConnect;
  }

  public ServerData(String name, String ip, int port) {
    this.name = name;
    this.ip = ip;
    this.port = port;
    this.quantityOfClientsToConnect = 0;
  }

  public String getName() {
    return name;
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  public int getquantityOfClientsToConnect() {
    return quantityOfClientsToConnect;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }
}
