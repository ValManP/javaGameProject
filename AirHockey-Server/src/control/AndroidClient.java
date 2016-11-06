package control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.database.DBInterface;

public class AndroidClient {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    public AndroidClient(DBInterface dbInterface, Socket cs) {     
        try {
            inputStream = new ObjectInputStream(cs.getInputStream());
            
            String userName = inputStream.readUTF();
            
            ResultSet userData = dbInterface.findUserGames(dbInterface.findUserByName(userName));
            
            outputStream = new ObjectOutputStream(cs.getOutputStream());
            
            while(userData.next()) {
                String gameRow = dbInterface.findUserById(userData.getInt("user1_id")) + " vs. " 
                        + dbInterface.findUserById(userData.getInt("user2_id")) + " "
                        + userData.getString("score1") + " - " + userData.getString("score2")
                        + String.format("dd.mm.yyyy", userData.getDate("date"));
                
                outputStream.reset();
                outputStream.writeUTF(gameRow);
            }
            
        } catch (IOException | SQLException ex) {
            Logger.getLogger(AndroidClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}