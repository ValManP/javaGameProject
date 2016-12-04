package control;

import control.client.ClientThread;
import control.client.PlayerClientThread;
import control.connect.ConnectionController;
import control.connect.IConnectionController;
import control.logic.GameController;
import control.logic.IGameController;
import model.physics.Physics;
import model.physics.AirHockeyState;
import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import model.data.ServerConnectionData;
import model.data.GameData;

public class ServerThread extends Thread {
    JTextArea log;
    
    IConnectionController connectionController;
    IGameController gameController;
    
    GameData gameData;
    ServerConnectionData connectionData;
    
    public ServerThread(JTextArea log, JFrame mainPanel) {
        this.log = log;
        
        gameData = new GameData(log);
        connectionData = new ServerConnectionData();
        
        connectionController = new ConnectionController(gameData, log);
        gameController = new GameController(gameData, connectionController, log);
        
        DrawPanel panel = new DrawPanel(gameData);
        panel.setBackground(Color.white);
        panel.setSize(Physics.Field);
        panel.setLocation(20, 20);
        mainPanel.add(panel);
        mainPanel.pack();
        mainPanel.setVisible(true);    
    }

    @Override
    public void run() {
        try {
            connectionData.serverSocket = new ServerSocket(connectionData.port, 0, connectionData.ip);
        
            while (gameData.isActive == true) {
                Socket clientSocket = connectionData.serverSocket.accept();
                AirHockeyState gameState = new AirHockeyState();

                PlayerClientThread newClient = new PlayerClientThread(this, clientSocket);

                if (gameData.dbInterface.findUserByName(newClient.getPlayerName()) == 0) {
                    log.append("\nPlayer not found. Added " + newClient.getPlayerName() + " to game DB.");
                    gameData.dbInterface.addUser(newClient.getPlayerName());
                }

                if (gameData.player_1 == null) {
                    gameData.player_1 = newClient;
                    addNewPlayer(newClient, 1, gameState);
                } else if (gameData.player_2 == null) {
                    gameData.player_2 = newClient;
                    addNewPlayer(newClient, 2, gameState);
                }  
                
                //ct.sendMessage(m);

                log.append("\nPlayer " + newClient.getPlayerName() + " connected to the game.");
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public synchronized void stopServer() {
        gameData.isActive = false;
        gameData.currentState.setDisconnected(true);
        
        if (gameData.player_1 != null) {
            disconnectPlayer(gameData.player_1, 1, gameData.currentState);
        }
        if (gameData.player_2 != null) {
            disconnectPlayer(gameData.player_2, 2, gameData.currentState);
        }
        try {
            connectionData.serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        interrupt();
    }
    
    public synchronized AirHockeyState getCurrentState() {
        return gameData.currentState;
    }
    
    public void setConnectionData(InetAddress _ip, int _port) {
        connectionData.ip = _ip;
        connectionData.port = _port;
    }
    
    private void addNewPlayer(PlayerClientThread newPlayer, int playerNum, AirHockeyState gameState) {
        gameState.setPlayerNum(playerNum);
        newPlayer.setPlayerNum(playerNum);
        newPlayer.sendMessage(gameState);
    }
    
    private void disconnectPlayer(PlayerClientThread gamePlayer, int playerNum, AirHockeyState gameState) {
        gamePlayer.sendMessage(gameState);
        gamePlayer.Disconnect();
        connectionController.disconnect(playerNum);
    }  
    
    public IConnectionController getConnectionController() {
        return connectionController;
    }

    public IGameController getGameController() {
        return gameController;
    }
}
