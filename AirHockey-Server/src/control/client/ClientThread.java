package control.client;

import control.ServerThread;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {
    protected ServerThread serverThread;
    protected Socket clientSocket;
    protected ObjectInputStream inputStream;
    protected ObjectOutputStream outputStream;
    
    protected ClientThread (ServerThread serverThread, Socket clientSocket) {
        try {
            this.serverThread = serverThread;
            this.clientSocket = clientSocket;
            
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
