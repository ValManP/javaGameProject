package control;

import control.client.ClientThread;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.database.DBInterface;

public class AndroidClient extends ClientThread {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    public String userName;
    
    public AndroidClient(ServerThread serverThread, Socket clientSocket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        super(serverThread, clientSocket, inputStream, outputStream);
        try {
            while (inputStream.available() == 0) {}
            userName = inputStream.readUTF();
            
            ResultSet userData = serverThread.gameData.dbInterface.findUserGames(serverThread.gameData.dbInterface.findUserByName(userName));
            String gameRow = "";
            while(userData.next()) {
                gameRow += serverThread.gameData.dbInterface.findUserById(userData.getInt("player1_id")) + " vs " 
                        + serverThread.gameData.dbInterface.findUserById(userData.getInt("player2_id")) + " "
                        + userData.getString("player1_score") + " - " + userData.getString("player2_score") + "\n";
                
            }
            outputStream.writeUTF(gameRow);
            outputStream.flush();
            
        } catch (IOException | SQLException ex) {
            Logger.getLogger(AndroidClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}