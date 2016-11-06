package control.connect;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public interface IConnectionController {
    
    public void connect(String ip, String port, String playerName);
    public void disconnect();
    public void sendDisconnect();
    public void sendMessage();
    void runListenThread();
    void sendName(String playerName);
    
}
