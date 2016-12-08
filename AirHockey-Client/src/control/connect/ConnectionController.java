package control.connect;

import control.logic.IGameController;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import model.ConnectionData;
import model.GameData;
import model.physics.AirHockeyState;
import view.ClientFrame;

public class ConnectionController implements IConnectionController {

    IGameController gameController;

    ConnectionData connectionData;
    GameData gameData;
    JTextArea log;

    public ConnectionController(ConnectionData connectionData, GameData gameData, IGameController gameController, JTextArea logArea) {
        this.connectionData = connectionData;
        this.gameData = gameData;
        this.gameController = gameController;
        this.log = logArea;
    }

    @Override
    public void connect(String ipAddress, String port, String playerName) {
        if (!connectionData.isConnected) {
            try {
                if (ipAddress.equals("localhost")) {
                    connectionData.ip = InetAddress.getLocalHost();
                } else {
                    connectionData.ip = InetAddress.getByName(ipAddress);
                }
                connectionData.port = Integer.valueOf(port);

                connectionData.socket = new Socket(connectionData.ip, connectionData.port);

                connectionData.outputStream = new ObjectOutputStream(connectionData.socket.getOutputStream());
                connectionData.inputStream = new ObjectInputStream(connectionData.socket.getInputStream());
                sendType();
                sendName(playerName);

                if (gameData.playerNum == -1) {
                    gameData.playerNum = ((AirHockeyState) connectionData.inputStream.readObject()).getPlayerNum();
                }

                connectionData.isConnected = true;

                log.append("Connected to the server.\n");
                log.append("You are player #" + gameData.playerNum + ".\n");
            } catch (Exception ex) {
                log.append("Cannot Connect! Try Again.\n");
            }

            runListenThread();
        } else {
            if (connectionData.isConnected == true) {
                log.append("You are already connected.\n");
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            connectionData.isConnected = false;
            gameController.resetGame();
            connectionData.inputStream.close();
            connectionData.outputStream.close();
            connectionData.incomingReader.interrupt();
            log.append("Disconnected.\n");
            connectionData.socket.close();
        } catch (Exception ex) {
            log.append("Failed to disconnect.\n");
        }
    }

    @Override
    public void sendDisconnect() {
        gameController.changeGameStatus(false);
        gameData.currentState.setDisconnected(true);
        sendMessage();
    }

    @Override
    public void sendMessage() {
        if (connectionData.outputStream != null && !connectionData.socket.isClosed()) {
            try {
                connectionData.outputStream.reset();
                connectionData.outputStream.writeObject(gameData.currentState);
            } catch (IOException ex) {
                Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void runListenThread() {
        connectionData.incomingReader = new Thread(new IncomingReader(gameController, this, connectionData, gameData, log));
        connectionData.incomingReader.start();
    }

    @Override
    public void sendName(String playerName) {
        try {
            gameData.currentState.setPlayerName(playerName);
            connectionData.outputStream.reset();
            connectionData.outputStream.writeObject(gameData.currentState);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendType() {
        try {
            connectionData.outputStream.reset();
            connectionData.outputStream.writeUTF("player");
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
