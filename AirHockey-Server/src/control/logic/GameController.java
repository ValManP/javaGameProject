package control.logic;

import control.GameCycle;
import control.ServerThread;
import control.connect.IConnectionController;
import java.awt.Point;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import model.data.GameData;

public class GameController implements IGameController {
    IConnectionController connectionController;
    GameData gameData;
    JTextArea log;
    
    public GameController(GameData gameData, IConnectionController connectionController, JTextArea log) {
        this.gameData = gameData;
        this.connectionController = connectionController;
        this.log = log;
    }

    @Override
    public void game() {
        if (gameData.gameThread == null) {
            addToLog("New game");
            gameData.gameThread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        Point m1 = null, m2 = null;

                        if (gameData.player_1 != null) {
                            m1 = gameData.player_1.getMes().getMallet1();

                            gameData.currentState.isFirstReady = gameData.player_1.getMes().isFirstReady;
                            if (m1 != null) {
                                gameData.incomingState.setMallet1(m1);
                           }
                        }

                        if (gameData.player_2 != null) {
                            m2 = gameData.player_2.getMes().getMallet2(); 

                            gameData.currentState.isSecondReady = gameData.player_2.getMes().isSecondReady;
                            if (m2 != null) {
                                gameData.incomingState.setMallet2(m2);
                            }
                        }

                        if (m1 != null && m2 != null) {
                            if (gameData.currentState.isFirstReady && gameData.currentState.isSecondReady) {
                                gameData.currentState.isGame = true;
                                gameData.currentState = gameData.gameCycle.calculate(gameData.incomingState);
                                handleGameOver();
                                connectionController.sendToAll(gameData.currentState);
                            } else {
                                gameData.currentState.isGame = false;
                            }
/*
                            float elapsedMilliTime = gameCycle.getElapsedNanoTime() / 1000.0f;
                            float toSleep = 17.0f - elapsedMilliTime;
                            if (toSleep > 0.0f) {
                                try {
                                    Thread.sleep((long)toSleep);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }*/
                        }
                    }
                }
            };
            gameData.gameThread.start();
       }
    }

    @Override
    public void handleGameOver() {
        if (gameData.currentState.firstScore == GameCycle.MAX_SCORE 
                || gameData.currentState.secondScore == GameCycle.MAX_SCORE) {
            saveGame();
            if (gameData.gameThread != null) {
                gameData.gameThread.interrupt();
                gameData.gameThread = null;
            }
            if (gameData.currentState.firstScore == GameCycle.MAX_SCORE) {
                connectionController.sendGameOver("You win\n", gameData.player_1);
                connectionController.sendGameOver("You lose\n", gameData.player_2);
            } else if (gameData.currentState.secondScore == GameCycle.MAX_SCORE) {
                connectionController.sendGameOver("You lose\n", gameData.player_1);
                connectionController.sendGameOver("You win\n", gameData.player_2);
            }
            connectionController.reset();
        }
    }

    @Override
    public void saveGame() {
        try {
            gameData.dbInterface.addGame(gameData.player_1.getPlayerName(),
                    gameData.player_2.getPlayerName(),
                    gameData.currentState.firstScore, gameData.currentState.secondScore);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void addToLog(String s) {
        log.append(s + "\n");
    }
}
