package control;

import control.ui.DrawPanel;
import control.client.ClientThread;
import control.client.PlayerClientThread;
import control.connect.ConnectionController;
import control.connect.IConnectionController;
import control.logic.GameController;
import control.logic.IGameController;
import model.physics.Physics;
import model.physics.AirHockeyState;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                
                ClientThread.ClientType type = getClientType(inputStream);
                if (ClientThread.ClientType.PLAYER.equals(type)) {
                    createPlayerClient(clientSocket, inputStream, outputStream);
                } else if (ClientThread.ClientType.ANDROID.equals(type)) {
                    createAndroidClient(clientSocket, inputStream, outputStream);
                }
                //ct.sendMessage(m);
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
    
    private ClientThread.ClientType getClientType(ObjectInputStream inputStream) throws IOException {
        String type;
        while (inputStream.available() == 0) {}
        type = inputStream.readUTF();
        
        if (type.equals("player")) {
            return ClientThread.ClientType.PLAYER;
        } else if (type.equals("android")) {
            return ClientThread.ClientType.ANDROID;
        }
        return null;
    }
    
    private void createPlayerClient(Socket clientSocket, ObjectInputStream inputStream, ObjectOutputStream outputStream) throws SQLException {
        AirHockeyState gameState = new AirHockeyState();
        PlayerClientThread newClient = new PlayerClientThread(this, clientSocket, inputStream, outputStream);

        if (gameData.dbInterface.findUserByName(newClient.getPlayerName()) == 0) {
            log.append("Player not found. Added " + newClient.getPlayerName() + " to game DB.\n");
            gameData.dbInterface.addUser(newClient.getPlayerName());
        }

        if (gameData.player_1 == null) {
            gameData.player_1 = newClient;
            addNewPlayer(newClient, 1, gameState);
        } else if (gameData.player_2 == null) {
            gameData.player_2 = newClient;
            addNewPlayer(newClient, 2, gameState);
        }  
        
        log.append("Player " + newClient.getPlayerName() + " connected to the game.\n");
    }
    
    private void createAndroidClient(Socket clientSocket, ObjectInputStream inputStream, ObjectOutputStream outputStream) throws SQLException {
        AndroidClient newClient = new AndroidClient(this, clientSocket, inputStream, outputStream);
        gameData.androidClients.add(newClient);
        log.append("Android client " + newClient.userName + " connected to the DB.\n");
    }
}
