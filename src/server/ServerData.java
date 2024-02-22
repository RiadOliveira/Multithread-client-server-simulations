package src.server;

public class ServerData {
  private final String name;
  private final int port;
  private final int quantityOfProcessesToConnect;
  private boolean closed;

  public ServerData(
    String name, int port,
    int quantityOfProcessesToConnect
  ) {
    this.name = name;
    this.port = port;
    this.quantityOfProcessesToConnect = quantityOfProcessesToConnect;
  }

  public String getName() {
    return name;
  }

  public int getPort() {
    return port;
  }

  public int getquantityOfProcessesToConnect() {
    return quantityOfProcessesToConnect;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }
}
