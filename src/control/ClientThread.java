package control;

import view.ClientFrame;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class ClientThread extends Thread {
    protected ServerThread st;
    private Socket cs;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    private model.physics.State incomingState;   
    private int player_num = -1; 
    private String player_name;
    
    JTextArea log;
    
    public void setPlayerNum(int player_num)
    {
        this.player_num = player_num;
    }
    
    public String getPlayerName()
    {
        return this.player_name;
    }
    
    public ClientThread(ServerThread st, Socket cs)
    {
        
        incomingState = new model.physics.State();
        
        this.st = st;
        this.cs = cs;
        
        try {
            inputStream = new ObjectInputStream(cs.getInputStream());
            outputStream = new ObjectOutputStream(cs.getOutputStream());
            
            do {
                incomingState = (model.physics.State)inputStream.readObject();
                player_name = incomingState.getPlayerName();
            } while (incomingState.getPlayerName() == null);
            
            this.start();
        } catch (IOException | ClassNotFoundException ex) {
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
                            incomingState = (model.physics.State)inputStream.readObject();
                            
                            if (incomingState.getDisconnectedPlayer() != 0) {
                                incomingState.setDisconnectedPlayer(0);
                                Disconnect();
                                st.disconnect(player_num);
                            }
                            
                            if (checkReadiness(incomingState)) {
                                st.game();
                            }
                            
                        } catch (IOException | ClassNotFoundException ex) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }.start();
    } 
    
    public model.physics.State getMes()
    {
       return incomingState;
    }
    
    public synchronized void sendMessage(model.physics.State m)
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
    
    public boolean checkReadiness(model.physics.State m) {
        if (player_num == 1) {
            st.currentState.isFirstReady = m.isFirstReady; 
        } else {
            st.currentState.isSecondReady = m.isSecondReady;
        }
        
        return st.currentState.isFirstReady && st.currentState.isSecondReady;
    }
}
