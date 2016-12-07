package control.client;

import control.ServerThread;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread{
    protected ServerThread serverThread;
    protected Socket clientSocket;
    protected ObjectInputStream inputStream;
    protected ObjectOutputStream outputStream;
    
    protected ClientThread.ClientType type;
    
    protected ClientThread (ServerThread serverThread, Socket clientSocket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.serverThread = serverThread;
        this.clientSocket = clientSocket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
    
    public ClientType getType() {
        return type;
    }

    public void setType(ClientType type) {
        this.type = type;
    }
    
    public enum ClientType {
        PLAYER,
        ANDROID
    }
}
