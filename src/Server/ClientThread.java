/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Client.ClientFrame;
import Client.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class ClientThread extends Thread {
    ServerThread st;
    Socket cs;
    UUID id = UUID.randomUUID();
    
    int NUM_PLAYER = -1; 
    
    JTextArea log;
    
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    
    Message message;   
    
    public UUID getUUID()
    {
        return id;
    }
    
    public ClientThread(ServerThread st, Socket cs)
    {
        
        message = new Message();
        
        this.st = st;
        this.cs = cs;
        try {
            inputStream = new ObjectInputStream(cs.getInputStream());
            outputStream = new ObjectOutputStream(cs.getOutputStream());
            
            this.start();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void run() {
        
        new Thread()
            {
                @Override
                public void run() {
                    boolean f = true;
                    while(f)
                    {
                        try {
                            message = (Message)inputStream.readObject();
                        } catch (IOException | ClassNotFoundException ex) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }.start();
    } 
    
    public Message getMes()
    {
       return message;
    }
    
    public void sendMessage(Message m)
    {
        try {
            outputStream.reset();
            outputStream.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
