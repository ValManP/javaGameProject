package control.connect;

import control.logic.IGameController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import model.ConnectionData;
import model.GameData;
import model.physics.AirHockeyState;
import view.ClientFrame;

public class IncomingReader implements Runnable {
    
    IGameController gameController;
    IConnectionController connectionController;
    ConnectionData connectionData;
    GameData gameData;
    JTextArea log;
    
    public IncomingReader(IGameController gameController, IConnectionController connectionController, ConnectionData connectionData, GameData gameData, JTextArea log) {
        this.gameController = gameController;
        this.connectionController = connectionController;
        this.connectionData = connectionData;
        this.gameData = gameData;
        this.log = log;
    }
    
    @Override
    public void run() {
        try {
            while (connectionData.inputStream != null) {

                gameData.incomingState = (AirHockeyState)connectionData.inputStream.readObject();

                if (gameData.incomingState.getMallet1() != null) {
                    gameData.currentState.setMallet1(gameData.incomingState.getMallet1());
                }
                if (gameData.incomingState.getMallet2() != null) {
                    gameData.currentState.setMallet2(gameData.incomingState.getMallet2());
                }
                if (gameData.incomingState.getPuck() != null) {
                    gameData.currentState.setPuck(gameData.incomingState.getPuck());
                }

                if (gameData.incomingState.isGame && !gameData.currentState.isGame) {
                    gameController.startGame();
                    gameData.currentState.isGame = gameData.incomingState.isGame;
                }

                if (!gameData.incomingState.isGame && gameData.currentState.isGame && gameData.incomingState.isGameOver) {
                    log.append(gameData.incomingState.message);
                    log.append("Click READY for new game\n");
                    gameController.resetGame();
                    gameController.changeGameStatus(false);
                    connectionController.sendMessage();
                    gameData.incomingState.isGame = false;
                    gameData.currentState.isGame = false;
                }


                gameController.handleScore(gameData.incomingState);

                if (gameData.incomingState.isDisconnected()) {
                    break;
                }
            }

            connectionController.disconnect();
         } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}