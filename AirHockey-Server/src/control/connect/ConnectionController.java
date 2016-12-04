package control.connect;

import control.client.ClientThread;
import control.GameCycle;
import control.client.PlayerClientThread;
import javax.swing.JTextArea;
import model.data.GameData;
import model.physics.AirHockeyState;

public class ConnectionController implements IConnectionController{
    GameData gameData;
    JTextArea log;
    
    public ConnectionController(GameData gameData, JTextArea log) {
        this.gameData = gameData;
        this.log = log;
    }
    
    @Override
    public void connect(String ip, String port, String playerName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disconnect(int playerNumber) {
        addToLog("Player " + playerNumber + " disconnected");
        if (gameData.gameThread != null) {
            gameData.gameThread.interrupt();
            gameData.gameThread = null;
        }
        switch (playerNumber) {
            case 1: {
                gameData.player_1.interrupt();
                gameData.player_1 = null;
                sendGameOver("Player 1 has disconnected\n", gameData.player_2);
                break;
            }
            case 2: {
                gameData.player_2.interrupt();
                gameData.player_2 = null;
                sendGameOver("Player 2 has disconnected\n", gameData.player_1);
                break;
            }
        }
        reset();
    }

    @Override
    public void reset() {
        gameData.currentState = new AirHockeyState();
        gameData.incomingState = new AirHockeyState();
        gameData.gameCycle = new GameCycle(gameData.currentState);
    }

    @Override
    public void setConnectionData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendDisconnect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public synchronized void addToLog(String s) {
        log.append(s + "\n");
    }
    
    @Override
    public void sendGameOver(String message, PlayerClientThread player) {
        AirHockeyState state = new AirHockeyState();
        state.message = message;
        state.isGameOver = true;
        player.sendMessage(state);
    }

    @Override
    public synchronized void sendToAll(AirHockeyState message) {
        if (gameData.player_1 != null && gameData.player_2 != null) {
            gameData.player_1.sendMessage(message);
            gameData.player_2.sendMessage(message);
        }
    }
}
