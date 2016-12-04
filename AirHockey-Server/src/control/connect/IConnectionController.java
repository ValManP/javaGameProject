package control.connect;

import control.client.PlayerClientThread;
import model.physics.AirHockeyState;

public interface IConnectionController {
    void reset();
    
    public void connect(String ip, String port, String playerName);
    public void disconnect(int playerNumber);
    
    public void setConnectionData();
    public void sendDisconnect();
    public void sendMessage();
    void sendToAll(AirHockeyState message);
    public void sendGameOver(String message, PlayerClientThread player);
}
