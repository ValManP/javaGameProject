package model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionData {
    public InetAddress ip = null;
    public int port = 2222;
    public boolean isConnected = false;
    public Socket socket = null;
    public Thread incomingReader;   
    public ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    
    public ConnectionData() {
        
    }
}