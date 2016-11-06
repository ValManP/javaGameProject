package model;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import model.physics.AirHockeyState;
import model.physics.Physics;
import view.ClientFrame;

public class GameData {
    // Game data
    public Point mallet;
    public Image fieldImg, yourMalletImg, puckImg, enemyMalletImg;
    public final Rectangle gameArea;
    
    // Game objects
    public AirHockeyState currentState;
    public AirHockeyState incomingState;
    public int playerNum = -1;
    
    public GameData() {
        try {
            fieldImg = ImageIO.read(new File("src/image/field.jpg"));
            yourMalletImg = ImageIO.read(new File("src/image/yourMallet.png"));
            enemyMalletImg = ImageIO.read(new File("src/image/enemyMallet.png"));
            puckImg = ImageIO.read(new File("src/image/puck.png"));
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        currentState = new AirHockeyState();
        incomingState = new AirHockeyState();
        gameArea = new Rectangle(Physics.Field);
    }
}