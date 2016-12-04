package model.data;

import control.GameCycle;
import control.ServerThread;
import control.client.PlayerClientThread;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import model.database.DBInterface;
import model.physics.AirHockeyState;

public class GameData {
    public boolean isActive = true;
    public PlayerClientThread player_1, player_2;
    public int client_count = 0;
    
    public AirHockeyState currentState;
    public AirHockeyState incomingState;  
    
    public GameCycle gameCycle;
    public DBInterface dbInterface;
    public Thread gameThread;
    
    public GameData(JTextArea log) {
        currentState = new AirHockeyState();
        incomingState = new AirHockeyState();
        
        gameCycle = new GameCycle(currentState);
        dbInterface = new DBInterface(log);
        try {
            dbInterface.connect();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
