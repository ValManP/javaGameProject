package control.client;

import control.ServerThread;
import model.physics.AirHockeyState;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class PlayerClientThread extends ClientThread {    
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
    
    public PlayerClientThread(ServerThread serverThread, Socket clientSocket) {
        super(serverThread, clientSocket);
        try {
            incomingState = new AirHockeyState();

            do {
                incomingState = (AirHockeyState)inputStream.readObject();
                player_name = incomingState.getPlayerName();
            } while (incomingState.getPlayerName() == null);
            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.start();
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
                            serverThread.getConnectionController().disconnect(player_num);
                        }

                        if (checkReadiness(incomingState)) {
                            serverThread.getGameController().game();
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
            clientSocket.close();
        } catch(IOException ex) {
            log.append("Failed to disconnect.\n");
        }
    }
    
    public void confirmDisconnect() {
        AirHockeyState disconnectState = new AirHockeyState();
        disconnectState.setDisconnected(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        sendMessage(disconnectState);
    }
    
    public boolean checkReadiness(AirHockeyState gameState) {
        if (player_num == 1) {
            serverThread.getCurrentState().isFirstReady = gameState.isFirstReady; 
        } else {
            serverThread.getCurrentState().isSecondReady = gameState.isSecondReady;
        }
        
        return serverThread.getCurrentState().isFirstReady && serverThread.getCurrentState().isSecondReady;
    }
}
