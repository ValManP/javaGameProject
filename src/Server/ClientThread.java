/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Client.ClientFrame;
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
    
    int player_num = -1; 
    String player_name;
    
    JTextArea log;
    
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    
    private Client.State incomingState;   
    
    public UUID getUUID()
    {
        return id;
    }
    
    public void setPlayerNum(int player_num)
    {
        this.player_num = player_num;
    }
    
    public ClientThread(ServerThread st, Socket cs)
    {
        
        incomingState = new Client.State();
        
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
                            incomingState = (Client.State)inputStream.readObject();
                            
                            if (incomingState.getDisconnectedPlayer() != 0) {
                                Disconnect();
                                st.disconnect(player_num);
                            }
                        } catch (IOException | ClassNotFoundException ex) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }.start();
    } 
    
    public Client.State getMes()
    {
       return incomingState;
    }
    
    public synchronized void sendMessage(Client.State m)
    {
        try {
            outputStream.reset();
            outputStream.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void Disconnect() 
    {
        try 
        {
            //sendMessage(new Client.State(true));
            cs.close();
        } catch(Exception ex) {
            log.append("Failed to disconnect. \n");
        }
    }
}
