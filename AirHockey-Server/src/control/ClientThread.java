package control;

import model.physics.AirHockeyState;
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
    
    private AirHockeyState incomingState;   
    private int player_num = 0; 
    private String player_name;
    
    JTextArea log;
    
    public void setPlayerNum(int player_num) {
        this.player_num = player_num;
    }
    
    public String getPlayerName() {
        return this.player_name;
    }
    
    public ClientThread(ServerThread st, Socket cs) {
        incomingState = new AirHockeyState();
        
        this.st = st;
        this.cs = cs;
        
        try {
            inputStream = new ObjectInputStream(cs.getInputStream());
            outputStream = new ObjectOutputStream(cs.getOutputStream());
            
            do {
                incomingState = (AirHockeyState)inputStream.readObject();
                player_name = incomingState.getPlayerName();
            } while (incomingState.getPlayerName() == null);
            
            this.start();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        new Thread() {
            @Override
            public void run() {
                boolean f = true;
                while(f) {
                    try {
                        incomingState = (AirHockeyState)inputStream.readObject();

                        if (incomingState.isDisconnected()) {
                            confirmDisconnect();
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
    
    public AirHockeyState getMes() {
       return incomingState;
    }
    
    public void sendMessage(AirHockeyState m) {
        try {
            outputStream.reset();
            outputStream.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void Disconnect() {
        try {
            cs.close();
        } catch(Exception ex) {
            log.append("Failed to disconnect.\n");
        }
    }
    
    public void confirmDisconnect() {
        AirHockeyState disconnectState = new AirHockeyState();
        disconnectState.setDisconnected(true);
        sendMessage(disconnectState);
    }
    
    public boolean checkReadiness(AirHockeyState m) {
        if (player_num == 1) {
            st.currentState.isFirstReady = m.isFirstReady; 
        } else {
            st.currentState.isSecondReady = m.isSecondReady;
        }
        
        return st.currentState.isFirstReady && st.currentState.isSecondReady;
    }
}
