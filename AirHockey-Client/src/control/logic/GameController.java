package control.logic;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import model.GameData;
import model.physics.AirHockeyState;
import model.physics.Physics;
import view.ClientFrame;

public class GameController implements IGameController {
    GameData gameData;
    JTextArea log;

    public GameController(GameData gameData, JTextArea log) {
        this.gameData = gameData;
        this.log = log;
    }
    
    @Override
    public void startGame() {
        try {
            log.append("Game will start in ");
            for (int i = 5; i > 0; i--) {
                log.append(i + "...");
                Thread.sleep(1000);
            }
            log.append("\nStart!\n");
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void resetGame() {
        gameData.currentState = new AirHockeyState();
        gameData.incomingState = new AirHockeyState();
        gameData.mallet = null;
    }

    @Override
    public void changeGameStatus(boolean status) {
        if (gameData.playerNum == 1) {
            gameData.mallet = new Point(gameData.gameArea.width / 2, (int)Physics.MalletRadius + 10);
            gameData.currentState.setMallet1(gameData.mallet);
            gameData.currentState.isFirstReady = status;
        } else {
            gameData.mallet = new Point(gameData.gameArea.width / 2, gameData.gameArea.height - (int)Physics.MalletRadius - 10);
            gameData.currentState.setMallet2(gameData.mallet);
            gameData.currentState.isSecondReady = status;
        }

        gameData.currentState.setPuck(new Point(gameData.gameArea.width / 2, gameData.gameArea.height / 2));
    }

    @Override
    public void handleScore(AirHockeyState incomingState) {
        gameData.currentState.firstScore = incomingState.firstScore;
        gameData.currentState.secondScore = incomingState.secondScore;
    }

    @Override
    public void disconnectGame() {
        gameData.currentState.isGame = false;
    }
}
